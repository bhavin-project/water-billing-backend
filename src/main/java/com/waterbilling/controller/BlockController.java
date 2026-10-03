package com.waterbilling.controller;

import com.waterbilling.entity.Block;
import com.waterbilling.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @GetMapping
public ResponseEntity<List<Block>> getAllBlocks() {
    List<Block> blocks = blockService.getAllBlocks();

    // Print total count
    System.out.println("========== BLOCKS DATA ==========");
    System.out.println("Total blocks found: " + blocks.size());

    // Loop and print each block's details
    for (Block block : blocks) {
        System.out.println("ID: " + block.getId() 
                + " | Block Name: " + block.getBlockName() 
                + " | Total Units: " + block.getTotalUnits());
    }
    System.out.println("=================================");

    return ResponseEntity.ok(blocks);
}

    @GetMapping("/{id}")
    public ResponseEntity<Block> getBlock(@PathVariable Long id) {
        return ResponseEntity.ok(blockService.getBlockById(id));
    }
}
