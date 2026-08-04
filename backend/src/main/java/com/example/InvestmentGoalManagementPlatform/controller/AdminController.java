package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.AdminDashboardResponseDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.AssetAdminRequestDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.AssetAdminResponseDTO;
import com.example.InvestmentGoalManagementPlatform.service.AdminDashboardService;
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
@CrossOrigin(origins = "http://localhost:4200")

public class AdminController {

    private final AdminService adminService;
    private final AdminDashboardService adminDashboardService;

    @Autowired
    public AdminController(AdminDashboardService adminDashboardService, AdminService adminService) {
        this.adminDashboardService = adminDashboardService;
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponseDTO>
    getDashboardStatistics() {

        return ResponseEntity.ok(
                adminDashboardService
                        .getDashboardStatistics()
        );
    }
    /*{
  "totalUsers": 25,
  "activeUsers": 22,
  "totalInvestments": 48,
  "activeInvestments": 43,
  "totalInvestmentAmount": 15250.0,
  "totalCurrentValue": 16420.0,
  "totalProfit": 1170.0
}*/

    @GetMapping("/assets")
    public ResponseEntity<List<AssetAdminResponseDTO>> getAllAssets() {
        return ResponseEntity.ok(adminService.getAllAssets());
    }

    @PostMapping("/assets")
    public ResponseEntity<AssetAdminResponseDTO> createAsset(@Valid @RequestBody AssetAdminRequestDTO request) {
        return new ResponseEntity<>(adminService.createAsset(request), HttpStatus.CREATED);
    }
}