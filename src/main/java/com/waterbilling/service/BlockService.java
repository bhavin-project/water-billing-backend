package com.waterbilling.service;

import com.waterbilling.entity.Block;
import com.waterbilling.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockService {
    private final BlockRepository blockRepository;

    public List<Block> getAllBlocks() {
        return blockRepository.findAll();
    }

    public Block getBlockById(Long id) {
        return blockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Block not found"));
    }
}