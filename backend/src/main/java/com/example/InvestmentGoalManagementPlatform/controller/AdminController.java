package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.AssetAdminRequestDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.AssetAdminResponseDTO;
import com.example.InvestmentGoalManagementPlatform.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/assets")
    public ResponseEntity<List<AssetAdminResponseDTO>> getAllAssets() {
        return ResponseEntity.ok(adminService.getAllAssets());
    }

    @PostMapping("/assets")
    public ResponseEntity<AssetAdminResponseDTO> createAsset(@Valid @RequestBody AssetAdminRequestDTO request) {
        return new ResponseEntity<>(adminService.createAsset(request), HttpStatus.CREATED);
    }
}