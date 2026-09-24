package com.waterbilling.service;

import com.waterbilling.entity.Unit;
import com.waterbilling.exception.ResourceNotFoundException;
import com.waterbilling.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitService {
    private final UnitRepository unitRepository;

    public List<Unit> getAllActiveUnits() {
        return unitRepository.findAllActiveWithBlock();
    }

    public List<Unit> getUnitsByBlock(Long blockId) {
        return unitRepository.findByBlockIdAndActiveTrue(blockId);
    }

    public Unit getUnitById(Long id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));
    }

    public Unit updateUnit(Long id, Unit unitDetails) {
        Unit unit = getUnitById(id);
        unit.setOwnerName(unitDetails.getOwnerName());
        unit.setContactNumber(unitDetails.getContactNumber());
        unit.setEmail(unitDetails.getEmail());
        return unitRepository.save(unit);
    }
}