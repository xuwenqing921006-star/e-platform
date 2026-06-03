package com.centralbank.eplatform.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import com.centralbank.eplatform.controller.admin.AdminAccountController;
import com.centralbank.eplatform.dto.AdminAccountCreateData;
import com.centralbank.eplatform.dto.AdminAccountDeleteData;
import com.centralbank.eplatform.dto.AdminAccountDetailData;
import com.centralbank.eplatform.dto.AdminAccountListItem;
import com.centralbank.eplatform.dto.AdminAccountResetPasswordData;
import com.centralbank.eplatform.dto.AdminAccountUpdateData;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.service.AdminAccountException;
import com.centralbank.eplatform.service.AdminAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminAccountControllerTest
{
    @Test
    void listEndpointReturnsContractPagination()
            throws Exception
    {
        AdminAccountService service = mock(AdminAccountService.class);
        when(service.list("zx", "CREDIT_REPORT", "OFFICE_USER", 1, 20))
                .thenReturn(new PaginatedData<>(List.of(new AdminAccountListItem(
                        10L, "zxglk", "张伟", "OFFICE_USER", "CREDIT_REPORT", "征信管理科", true)), 1, 1, 20));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminAccountController(service)).build();

        mockMvc.perform(get("/api/admin/accounts")
                        .param("keyword", "zx")
                        .param("office_code", "CREDIT_REPORT")
                        .param("role", "OFFICE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[0].id").value(10))
                .andExpect(jsonPath("$.data.items[0].username").value("zxglk"))
                .andExpect(jsonPath("$.data.items[0].display_name").value("张伟"))
                .andExpect(jsonPath("$.data.items[0].role").value("OFFICE_USER"))
                .andExpect(jsonPath("$.data.items[0].office_code").value("CREDIT_REPORT"))
                .andExpect(jsonPath("$.data.items[0].office_name").value("征信管理科"))
                .andExpect(jsonPath("$.data.items[0].enabled").value(true));

        verify(service).list("zx", "CREDIT_REPORT", "OFFICE_USER", 1, 20);
    }

    @Test
    void createEndpointReturnsAccountId()
            throws Exception
    {
        AdminAccountService service = mock(AdminAccountService.class);
        when(service.create(org.mockito.ArgumentMatchers.any())).thenReturn(new AdminAccountCreateData(12L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminAccountController(service)).build();

        mockMvc.perform(post("/api/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"zxglk2",
                                  "display_name":"张伟",
                                  "role":"OFFICE_USER",
                                  "office_code":"CREDIT_REPORT",
                                  "initial_password":"Initial123!",
                                  "enabled":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(12));
    }

    @Test
    void detailEndpointReturnsContractFields()
            throws Exception
    {
        AdminAccountService service = mock(AdminAccountService.class);
        when(service.detail(10L)).thenReturn(Optional.of(new AdminAccountDetailData(
                10L, "zxglk", "张伟", "OFFICE_USER", "CREDIT_REPORT", "征信管理科", true)));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminAccountController(service)).build();

        mockMvc.perform(get("/api/admin/accounts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("zxglk"))
                .andExpect(jsonPath("$.data.display_name").value("张伟"))
                .andExpect(jsonPath("$.data.office_name").value("征信管理科"))
                .andExpect(jsonPath("$.data.initial_password").doesNotExist());
    }

    @Test
    void updateDeleteAndResetReturnContractData()
            throws Exception
    {
        AdminAccountService service = mock(AdminAccountService.class);
        when(service.update(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new AdminAccountUpdateData(10L, true));
        when(service.delete(10L)).thenReturn(new AdminAccountDeleteData(true));
        when(service.resetPassword(10L, "ResetPassword1!")).thenReturn(new AdminAccountResetPasswordData(true));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminAccountController(service)).build();

        mockMvc.perform(put("/api/admin/accounts/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "display_name":"张伟",
                                  "role":"OFFICE_USER",
                                  "office_code":"CREDIT_REPORT",
                                  "enabled":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updated").value(true));

        mockMvc.perform(delete("/api/admin/accounts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deleted").value(true));

        mockMvc.perform(post("/api/admin/accounts/10/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "new_password":"ResetPassword1!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reset").value(true));
    }

    @Test
    void forbiddenEndpointReturnsContractJson()
            throws Exception
    {
        AdminAccountService service = mock(AdminAccountService.class);
        when(service.list(null, null, null, 1, 20)).thenThrow(new AdminAccountException(403, "仅管理员可管理账号"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminAccountController(service)).build();

        mockMvc.perform(get("/api/admin/accounts"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("仅管理员可管理账号"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
}
