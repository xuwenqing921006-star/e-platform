package com.centralbank.eplatform.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import com.centralbank.eplatform.controller.admin.AdminDashboardController;
import com.centralbank.eplatform.dto.AdminDashboardRecentContent;
import com.centralbank.eplatform.dto.AdminDashboardSummaryData;
import com.centralbank.eplatform.service.AdminDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminDashboardControllerTest
{
    @Test
    void summaryEndpointReturnsContractFields()
            throws Exception
    {
        AdminDashboardService service = mock(AdminDashboardService.class);
        when(service.summary()).thenReturn(new AdminDashboardSummaryData(
                48, 112, 9, 16,
                List.of(new AdminDashboardRecentContent(
                        101L, "中国人民银行公告〔2025〕第12号", "POLICY_PROMOTION",
                        "2026-05-30T16:42:00+08:00"))));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminDashboardController(service)).build();

        mockMvc.perform(get("/api/admin/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.published_content_count").value(48))
                .andExpect(jsonPath("$.data.product_count").value(112))
                .andExpect(jsonPath("$.data.account_count").value(9))
                .andExpect(jsonPath("$.data.today_operation_count").value(16))
                .andExpect(jsonPath("$.data.recent_contents[0].id").value(101))
                .andExpect(jsonPath("$.data.recent_contents[0].title").value("中国人民银行公告〔2025〕第12号"))
                .andExpect(jsonPath("$.data.recent_contents[0].category").value("POLICY_PROMOTION"))
                .andExpect(jsonPath("$.data.recent_contents[0].published_at").value("2026-05-30T16:42:00+08:00"));
    }
}
