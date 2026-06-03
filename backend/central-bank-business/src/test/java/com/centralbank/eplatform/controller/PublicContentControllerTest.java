package com.centralbank.eplatform.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.centralbank.eplatform.controller.publicapi.PublicContentController;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.dto.PublicContentListItem;
import com.centralbank.eplatform.service.PublicContentService;

class PublicContentControllerTest
{
    @Test
    void listEndpointReturnsPublicContentContractJson() throws Exception
    {
        PublicContentService service = mock(PublicContentService.class);
        when(service.list("SERVICE_GUIDE", "FINANCIAL", null, 1, 10))
                .thenReturn(new PaginatedData<>(List.of(new PublicContentListItem(
                        102L,
                        "大庆市征信代理查询网点地址及电话",
                        "SERVICE_GUIDE",
                        "征信管理科",
                        "2026-05-30T09:30:00+08:00")), 1, 1, 10));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PublicContentController(service)).build();

        mockMvc.perform(get("/api/public/contents")
                        .param("category", "SERVICE_GUIDE")
                        .param("scope", "FINANCIAL")
                        .param("page", "1")
                        .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.page_size").value(10))
                .andExpect(jsonPath("$.data.items[0].office_name").value("征信管理科"))
                .andExpect(jsonPath("$.data.items[0].published_at").value("2026-05-30T09:30:00+08:00"));

        verify(service).list("SERVICE_GUIDE", "FINANCIAL", null, 1, 10);
    }

    @Test
    void detailEndpointReturnsContracted404Json() throws Exception
    {
        PublicContentService service = mock(PublicContentService.class);
        when(service.detail(999L)).thenReturn(Optional.empty());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PublicContentController(service)).build();

        mockMvc.perform(get("/api/public/contents/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("内容不存在"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(service).detail(999L);
    }
}
