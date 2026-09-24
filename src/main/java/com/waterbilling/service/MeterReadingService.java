package com.waterbilling.service;

import com.waterbilling.dto.*;
import com.waterbilling.entity.*;
import com.waterbilling.exception.ResourceNotFoundException;
import com.waterbilling.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final UnitRepository unitRepository;
    private final QuarterRepository quarterRepository;
    private final RateConfigService rateConfigService;

    // ========== Quarter Management ==========

    public Quarter getOrCreateQuarter(Integer year, Integer quarterNumber) {
        return quarterRepository.findByYearAndQuarterNumber(year, quarterNumber)
                .orElseGet(() -> {
                    Quarter q = new Quarter();
                    q.setYear(year);
                    q.setQuarterNumber(quarterNumber);

                    int startMonth = (quarterNumber - 1) * 3 + 1;
                    int endMonth = startMonth + 2;
                    q.setStartDate(LocalDate.of(year, startMonth, 1));
                    q.setEndDate(LocalDate.of(year, endMonth, 1).plusMonths(1).minusDays(1));

                    String[] qLabels = {"Jan-Mar", "Apr-Jun", "Jul-Sep", "Oct-Dec"};
                    q.setLabel("Q" + quarterNumber + "-" + year + " (" + qLabels[quarterNumber - 1] + ")");
                    q.setClosed(false);
                    return quarterRepository.save(q);
                });
    }

    public List<Quarter> getAllQuarters() {
        return quarterRepository.findAllByOrderByYearDescQuarterNumberDesc();
    }

    // ========== Get Reading Form Data (Auto-populate previous readings) ==========

    public ReadingFormDataDTO getReadingFormData(Long quarterId, Long blockId) {
        Quarter quarter = quarterRepository.findById(quarterId)
                .orElseThrow(() -> new ResourceNotFoundException("Quarter not found"));

        BigDecimal currentRate = rateConfigService.getRateForDate(quarter.getStartDate());

        // Get units based on block filter
        List<Unit> units;
        if (blockId != null && blockId > 0) {
            units = unitRepository.findByBlockIdAndActiveTrue(blockId);
        } else {
            units = unitRepository.findAllActiveWithBlock();
        }

        // Get existing readings for this quarter
        Map<Long, MeterReading> existingReadingsMap = meterReadingRepository
                .findByQuarterId(quarterId).stream()
                .collect(Collectors.toMap(mr -> mr.getUnit().getId(), mr -> mr));

        List<MeterReadingDTO> readingDTOs = new ArrayList<>();

        for (Unit unit : units) {
            MeterReadingDTO dto = new MeterReadingDTO();
            dto.setUnitId(unit.getId());
            dto.setUnitNumber(unit.getUnitNumber());
            dto.setBlockName(unit.getBlock().getBlockName());
            dto.setOwnerName(unit.getOwnerName());
            dto.setQuarterId(quarterId);
            dto.setQuarterLabel(quarter.getLabel());
            dto.setRatePerUnit(currentRate);

            MeterReading existing = existingReadingsMap.get(unit.getId());

            if (existing != null) {
                // Reading already exists for this quarter
                dto.setId(existing.getId());
                dto.setPreviousReading(existing.getPreviousReading());
                dto.setCurrentReading(existing.getCurrentReading());
                dto.setUnitsConsumed(existing.getUnitsConsumed());
                dto.setEstimatedAmount(currentRate.multiply(BigDecimal.valueOf(existing.getUnitsConsumed())));
                dto.setReadingDate(existing.getReadingDate());
                dto.setLocked(existing.isLocked());
                dto.setHasReading(true);
            } else {
                // No reading yet — auto-fetch previous reading
                Double previousReading = getLastCurrentReading(unit.getId());
                dto.setPreviousReading(previousReading);
                dto.setCurrentReading(previousReading); // default to same as previous
                dto.setUnitsConsumed(0.0);
                dto.setEstimatedAmount(BigDecimal.ZERO);
                dto.setLocked(false);
                dto.setHasReading(false);
            }

            readingDTOs.add(dto);
        }

        // Sort by block name then unit number
        readingDTOs.sort(Comparator.comparing(MeterReadingDTO::getBlockName)
                .thenComparing(MeterReadingDTO::getUnitNumber));

        int entered = (int) readingDTOs.stream().filter(MeterReadingDTO::isHasReading).count();

        return ReadingFormDataDTO.builder()
                .quarterId(quarterId)
                .quarterLabel(quarter.getLabel())
                .currentRate(currentRate)
                .readings(readingDTOs)
                .totalUnits(readingDTOs.size())
                .readingsEntered(entered)
                .readingsPending(readingDTOs.size() - entered)
                .build();
    }

    // ========== Get last current reading for a unit (becomes next previous reading) ==========

    public Double getLastCurrentReading(Long unitId) {
        return meterReadingRepository.findLatestByUnitId(unitId)
                .map(MeterReading::getCurrentReading)
                .orElse(0.0);
    }

    // ========== Save Single Reading ==========

    @Transactional
    public MeterReadingDTO saveSingleReading(ReadingEntryDTO dto) {
        Unit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + dto.getUnitId()));

        Quarter quarter = quarterRepository.findById(dto.getQuarterId())
                .orElseThrow(() -> new ResourceNotFoundException("Quarter not found: " + dto.getQuarterId()));

        // Auto-fetch previous reading
        Double previousReading = getLastCurrentReadingBeforeQuarter(unit.getId(), quarter);

        if (dto.getCurrentReading() < previousReading) {
            throw new IllegalArgumentException(
                    "Current reading (" + dto.getCurrentReading() +
                    ") cannot be less than previous reading (" + previousReading +
                    ") for unit " + unit.getUnitNumber());
        }

        // Check if reading already exists
        Optional<MeterReading> existingOpt = meterReadingRepository
                .findByUnitIdAndQuarterId(unit.getId(), quarter.getId());

        MeterReading reading;
        if (existingOpt.isPresent()) {
            reading = existingOpt.get();
            if (reading.isLocked()) {
                throw new IllegalArgumentException(
                        "Reading for " + unit.getUnitNumber() + " is locked (bill already generated)");
            }
            reading.setCurrentReading(dto.getCurrentReading());
            reading.setPreviousReading(previousReading);
        } else {
            reading = new MeterReading();
            reading.setUnit(unit);
            reading.setQuarter(quarter);
            reading.setPreviousReading(previousReading);
            reading.setCurrentReading(dto.getCurrentReading());
            reading.setReadingDate(LocalDate.now());
        }

        reading.calculateUnits();
        reading = meterReadingRepository.save(reading);

        BigDecimal rate = rateConfigService.getRateForDate(quarter.getStartDate());

        return MeterReadingDTO.builder()
                .id(reading.getId())
                .unitId(unit.getId())
                .unitNumber(unit.getUnitNumber())
                .blockName(unit.getBlock().getBlockName())
                .ownerName(unit.getOwnerName())
                .quarterId(quarter.getId())
                .quarterLabel(quarter.getLabel())
                .previousReading(reading.getPreviousReading())
                .currentReading(reading.getCurrentReading())
                .unitsConsumed(reading.getUnitsConsumed())
                .ratePerUnit(rate)
                .estimatedAmount(rate.multiply(BigDecimal.valueOf(reading.getUnitsConsumed())))
                .readingDate(reading.getReadingDate())
                .hasReading(true)
                .locked(reading.isLocked())
                .build();
    }

    // ========== Save Bulk Readings ==========

    @Transactional
    public List<MeterReadingDTO> saveBulkReadings(BulkReadingEntryDTO dto) {
        List<MeterReadingDTO> savedReadings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (ReadingEntryDTO entry : dto.getReadings()) {
            entry.setQuarterId(dto.getQuarterId());
            try {
                savedReadings.add(saveSingleReading(entry));
            } catch (Exception e) {
                errors.add("Unit " + entry.getUnitId() + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty() && savedReadings.isEmpty()) {
            throw new IllegalArgumentException("All readings failed: " + String.join("; ", errors));
        }

        return savedReadings;
    }

    // ========== Get previous reading considering quarter order ==========

    private Double getLastCurrentReadingBeforeQuarter(Long unitId, Quarter currentQuarter) {
        // Get the most recent reading for this unit
        // (could be from any previous quarter)
        return meterReadingRepository.findLatestByUnitId(unitId)
                .map(mr -> {
                    // If the latest reading IS for the current quarter, we need the one before that
                    if (mr.getQuarter().getId().equals(currentQuarter.getId())) {
                        // Find the reading before this one
                        List<MeterReading> allReadings = meterReadingRepository
                                .findByUnitIdOrderByQuarterDesc(unitId);
                        if (allReadings.size() > 1) {
                            return allReadings.get(1).getCurrentReading();
                        }
                        return mr.getPreviousReading(); // use its own previous
                    }
                    return mr.getCurrentReading();
                })
                .orElse(0.0);
    }

    // ========== View readings by quarter ==========

    public List<MeterReadingDTO> getReadingsByQuarter(Long quarterId) {
        BigDecimal rate = BigDecimal.ZERO;
        Optional<Quarter> qOpt = quarterRepository.findById(quarterId);
        if (qOpt.isPresent()) {
            rate = rateConfigService.getRateForDate(qOpt.get().getStartDate());
        }

        BigDecimal finalRate = rate;
        return meterReadingRepository.findByQuarterIdWithUnit(quarterId).stream()
                .map(mr -> toDTO(mr, finalRate)).toList();
    }

    public List<MeterReadingDTO> getReadingsByQuarterAndBlock(Long quarterId, Long blockId) {
        BigDecimal rate = BigDecimal.ZERO;
        Optional<Quarter> qOpt = quarterRepository.findById(quarterId);
        if (qOpt.isPresent()) {
            rate = rateConfigService.getRateForDate(qOpt.get().getStartDate());
        }

        BigDecimal finalRate = rate;
        return meterReadingRepository.findByQuarterIdAndBlockId(quarterId, blockId).stream()
                .map(mr -> toDTO(mr, finalRate)).toList();
    }

    // ========== View readings by unit (history) ==========

    public List<MeterReadingDTO> getReadingsByUnit(Long unitId) {
        return meterReadingRepository.findByUnitIdOrderByQuarterDesc(unitId).stream()
                .map(mr -> {
                    BigDecimal rate = rateConfigService.getRateForDate(mr.getQuarter().getStartDate());
                    return toDTO(mr, rate);
                }).toList();
    }

    // ========== DTO converter ==========

    private MeterReadingDTO toDTO(MeterReading mr, BigDecimal rate) {
        return MeterReadingDTO.builder()
                .id(mr.getId())
                .unitId(mr.getUnit().getId())
                .unitNumber(mr.getUnit().getUnitNumber())
                .blockName(mr.getUnit().getBlock().getBlockName())
                .ownerName(mr.getUnit().getOwnerName())
                .quarterId(mr.getQuarter().getId())
                .quarterLabel(mr.getQuarter().getLabel())
                .previousReading(mr.getPreviousReading())
                .currentReading(mr.getCurrentReading())
                .unitsConsumed(mr.getUnitsConsumed())
                .ratePerUnit(rate)
                .estimatedAmount(rate.multiply(BigDecimal.valueOf(mr.getUnitsConsumed())))
                .readingDate(mr.getReadingDate())
                .hasReading(true)
                .locked(mr.isLocked())
                .build();
    }
}