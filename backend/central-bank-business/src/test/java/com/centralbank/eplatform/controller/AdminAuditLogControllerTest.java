package com.centralbank.eplatform.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import com.centralbank.eplatform.controller.admin.AdminAuditLogController;
import com.centralbank.eplatform.dto.AdminAuditLogListItem;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.service.AuditLogException;
import com.centralbank.eplatform.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminAuditLogControllerTest
{
    @Test
    void listEndpointReturnsContractPagination()
            throws Exception
    {
        AuditLogService service = mock(AuditLogService.class);
        when(service.list("系统", "IMPORT", "2026-05-01", "2026-05-31", 1, 20))
                .thenReturn(new PaginatedData<>(List.of(new AdminAuditLogListItem(
                        7001L, "系统管理员", "IMPORT", "FINANCIAL_PRODUCT", "金融产品",
                        "导入 112 条金融产品数据", "2026-05-30T15:16:00+08:00")), 1, 1, 20));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminAuditLogController(service)).build();

        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("operator_keyword", "系统")
                        .param("operation_type", "IMPORT")
                        .param("operated_from", "2026-05-01")
                        .param("operated_to", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[0].id").value(7001))
                .andExpect(jsonPath("$.data.items[0].operator_name").value("系统管理员"))
                .andExpect(jsonPath("$.data.items[0].operation_type").value("IMPORT"))
                .andExpect(jsonPath("$.data.items[0].object_type").value("FINANCIAL_PRODUCT"))
                .andExpect(jsonPath("$.data.items[0].object_name").value("金融产品"))
                .andExpect(jsonPath("$.data.items[0].description").value("导入 112 条金融产品数据"))
                .andExpect(jsonPath("$.data.items[0].operated_at").value("2026-05-30T15:16:00+08:00"));

        verify(service).list("系统", "IMPORT", "2026-05-01", "2026-05-31", 1, 20);
    }

    @Test
    void forbiddenEndpointReturnsContractJson()
            throws Exception
    {
        AuditLogService service = mock(AuditLogService.class);
        when(service.list(null, null, null, null, 1, 20))
                .thenThrow(new AuditLogException(403, "仅管理员可查看操作日志"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminAuditLogController(service)).build();

        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("仅管理员可查看操作日志"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
}
