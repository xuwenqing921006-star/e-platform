package com.centralbank.eplatform.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.centralbank.eplatform.controller.admin.AdminAttachmentController;
import com.centralbank.eplatform.dto.PublicAttachment;
import com.centralbank.eplatform.service.AttachmentException;
import com.centralbank.eplatform.service.AttachmentStorageService;

class AdminAttachmentControllerTest
{
    @Test
    void uploadEndpointReturnsContractFields() throws Exception
    {
        AttachmentStorageService service = mock(AttachmentStorageService.class);
        MockMultipartFile file = new MockMultipartFile("file", "县域征信服务网点信息表.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx-content".getBytes());
        when(service.upload(file)).thenReturn(new PublicAttachment(
                9001L,
                "县域征信服务网点信息表.xlsx",
                "EXCEL",
                12L,
                "/api/public/attachments/9001/download"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminAttachmentController(service)).build();

        mockMvc.perform(multipart("/api/admin/attachments").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(9001))
                .andExpect(jsonPath("$.data.file_name").value("县域征信服务网点信息表.xlsx"))
                .andExpect(jsonPath("$.data.file_type").value("EXCEL"))
                .andExpect(jsonPath("$.data.file_size").value(12))
                .andExpect(jsonPath("$.data.download_url").value("/api/public/attachments/9001/download"));

        verify(service).upload(file);
    }

    @Test
    void uploadEndpointReturnsClearValidationErrors() throws Exception
    {
        AttachmentStorageService service = mock(AttachmentStorageService.class);
        MockMultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf", "big".getBytes());
        when(service.upload(file)).thenThrow(new AttachmentException(413, "单个附件不能超过 20MB"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminAttachmentController(service)).build();

        mockMvc.perform(multipart("/api/admin/attachments").file(file))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value(413))
                .andExpect(jsonPath("$.message").value("单个附件不能超过 20MB"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void deleteEndpointReturnsContractData() throws Exception
    {
        AttachmentStorageService service = mock(AttachmentStorageService.class);
        when(service.delete(9001L)).thenReturn(true);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminAttachmentController(service)).build();

        mockMvc.perform(delete("/api/admin/attachments/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    void deleteEndpointReturnsContract404() throws Exception
    {
        AttachmentStorageService service = mock(AttachmentStorageService.class);
        when(service.delete(9001L)).thenReturn(false);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminAttachmentController(service)).build();

        mockMvc.perform(delete("/api/admin/attachments/9001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("附件不存在"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void controllerSourceUsesMultipartFile() throws Exception
    {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new AdminAttachmentController(mock(AttachmentStorageService.class))).build();

        mockMvc.perform(multipart("/api/admin/attachments"))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("Required part 'file' is not present")));
    }
}
