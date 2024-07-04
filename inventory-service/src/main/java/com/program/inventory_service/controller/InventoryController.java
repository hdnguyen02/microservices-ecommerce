package com.program.inventory_service.controller;

import com.program.inventory_service.dto.InventoryResponse;
import com.program.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/inventory")
public class InventoryController {


    @Autowired
    private DiscoveryClient discoveryClient;

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> getInventories(@RequestParam List<String> skuCodes) {
        return inventoryService.getInventories(skuCodes);
    }
}
