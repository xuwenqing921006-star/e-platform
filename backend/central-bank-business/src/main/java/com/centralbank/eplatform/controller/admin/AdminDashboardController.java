package com.centralbank.eplatform.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.centralbank.eplatform.dto.AdminDashboardSummaryData;
import com.centralbank.eplatform.dto.ApiResponse;
import com.centralbank.eplatform.service.AdminDashboardService;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController
{
    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService)
    {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<AdminDashboardSummaryData> summary()
    {
        return ApiResponse.success(dashboardService.summary());
    }
}
