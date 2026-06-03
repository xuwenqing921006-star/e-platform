package com.centralbank.eplatform.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.centralbank.eplatform.dto.ApiResponse;
import com.centralbank.eplatform.dto.OptionsResponse;
import com.centralbank.eplatform.service.FixedOptionsService;

@RestController
@RequestMapping("/api/admin/options")
public class AdminOptionsController
{
    private final FixedOptionsService fixedOptionsService;

    public AdminOptionsController(FixedOptionsService fixedOptionsService)
    {
        this.fixedOptionsService = fixedOptionsService;
    }

    @GetMapping
    public ApiResponse<OptionsResponse> options()
    {
        return ApiResponse.success(fixedOptionsService.getOptions());
    }
}
