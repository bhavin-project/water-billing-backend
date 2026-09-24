package com.waterbilling.service;

import com.waterbilling.dto.MeterReadingDTO;
import com.waterbilling.entity.Unit;
import com.waterbilling.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final UnitRepository unitRepository;

    /**
     * Expected Excel columns:
     * Column 0: Unit Number (e.g., A-1, B-5)
     * Column 1: Previous Reading
     * Column 2: Current Reading
     * Column 3: Reading Date (optional)
     */
    public List<MeterReadingDTO> parseExcel(MultipartFile file) {
        List<MeterReadingDTO> readings = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String unitNumber = getCellStringValue(row.getCell(0));
                if (unitNumber == null || unitNumber.trim().isEmpty()) continue;

                Double prevReading = getCellNumericValue(row.getCell(1));
                Double currReading = getCellNumericValue(row.getCell(2));

                if (prevReading == null || currReading == null) continue;

                Optional<Unit> unitOpt = unitRepository.findByUnitNumber(unitNumber.trim());
                if (unitOpt.isEmpty()) continue;

                Unit unit = unitOpt.get();
                MeterReadingDTO dto = MeterReadingDTO.builder()
                        .unitId(unit.getId())
                        .unitNumber(unit.getUnitNumber())
                        .blockName(unit.getBlock().getBlockName())
                        .ownerName(unit.getOwnerName())
                        .previousReading(prevReading)
                        .currentReading(currReading)
                        .unitsConsumed(currReading - prevReading)
                        .readingDate(LocalDate.now())
                        .build();

                readings.add(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Excel file: " + e.getMessage(), e);
        }

        return readings;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int) cell.getNumericCellValue());
        return null;
    }

    private Double getCellNumericValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}