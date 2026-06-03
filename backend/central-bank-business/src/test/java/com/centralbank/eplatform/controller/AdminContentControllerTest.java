package com.centralbank.eplatform.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.centralbank.eplatform.controller.admin.AdminContentController;
import com.centralbank.eplatform.dto.AdminContentCreateData;
import com.centralbank.eplatform.dto.AdminContentDetailData;
import com.centralbank.eplatform.dto.AdminContentDeleteData;
import com.centralbank.eplatform.dto.AdminContentListItem;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.service.AdminContentException;
import com.centralbank.eplatform.service.AdminContentService;

class AdminContentControllerTest
{
    @Test
    void listEndpointReturnsContractPagination() throws Exception
    {
        AdminContentService service = mock(AdminContentService.class);
        when(service.list("征信", null, null, null, null, 1, 20))
                .thenReturn(new PaginatedData<>(List.of(new AdminContentListItem(
                        102L,
                        "大庆市征信代理查询网点地址及电话",
                        "SERVICE_GUIDE",
                        "CREDIT_REPORT",
                        "征信管理科",
                        "2026-05-30T09:30:00+08:00")), 1, 1, 20));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminContentController(service)).build();

        mockMvc.perform(get("/api/admin/contents").param("keyword", "征信"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[0].office_code").value("CREDIT_REPORT"))
                .andExpect(jsonPath("$.data.items[0].published_at").value("2026-05-30T09:30:00+08:00"))
                .andExpect(jsonPath("$.data.items[0].attachments").doesNotExist());

        verify(service).list("征信", null, null, null, null, 1, 20);
    }

    @Test
    void createEndpointReturnsContractData() throws Exception
    {
        AdminContentService service = mock(AdminContentService.class);
        when(service.create(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new AdminContentCreateData(1201L, "2026-06-03T14:30:00+08:00"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminContentController(service)).build();

        mockMvc.perform(post("/api/admin/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"征信服务办理指南",
                                  "category":"SERVICE_GUIDE",
                                  "office_code":"CREDIT_REPORT",
                                  "rich_text_html":"<p>正文</p>",
                                  "attachment_ids":[9001]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1201))
                .andExpect(jsonPath("$.data.published_at").value("2026-06-03T14:30:00+08:00"));
    }

    @Test
    void detailEndpointReturnsContracted404Json() throws Exception
    {
        AdminContentService service = mock(AdminContentService.class);
        when(service.detail(999L)).thenReturn(Optional.empty());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminContentController(service)).build();

        mockMvc.perform(get("/api/admin/contents/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("内容不存在"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void detailEndpointReturnsAttachmentsOnlyOnDetail() throws Exception
    {
        AdminContentService service = mock(AdminContentService.class);
        when(service.detail(102L)).thenReturn(Optional.of(new AdminContentDetailData(
                102L,
                "大庆市征信代理查询网点地址及电话",
                "SERVICE_GUIDE",
                "CREDIT_REPORT",
                "征信管理科",
                "<p>正文</p>",
                "2026-05-30T09:30:00+08:00",
                List.of())));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminContentController(service)).build();

        mockMvc.perform(get("/api/admin/contents/102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.office_code").value("CREDIT_REPORT"))
                .andExpect(jsonPath("$.data.rich_text_html").value("<p>正文</p>"))
                .andExpect(jsonPath("$.data.attachments").isArray());
    }

    @Test
    void deleteEndpointReturnsForbiddenContract() throws Exception
    {
        AdminContentService service = mock(AdminContentService.class);
        when(service.delete(103L)).thenThrow(new AdminContentException(403, "无权修改该内容"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminContentController(service)).build();

        mockMvc.perform(delete("/api/admin/contents/103"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权修改该内容"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void deleteEndpointReturnsContractData() throws Exception
    {
        AdminContentService service = mock(AdminContentService.class);
        when(service.delete(102L)).thenReturn(new AdminContentDeleteData(true));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminContentController(service)).build();

        mockMvc.perform(delete("/api/admin/contents/102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deleted").value(true));
    }
}
