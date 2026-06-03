package com.centralbank.eplatform.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.centralbank.eplatform.controller.publicapi.PublicAttachmentController;
import com.centralbank.eplatform.domain.CbAttachment;
import com.centralbank.eplatform.service.AttachmentStorageService;

class PublicAttachmentControllerTest
{
    @TempDir
    Path tempDir;

    @Test
    void downloadEndpointReturnsRealFileStream() throws Exception
    {
        Path filePath = tempDir.resolve("policy.xlsx");
        Files.writeString(filePath, "xlsx-content");
        CbAttachment attachment = attachment(9001L, "县域征信服务网点信息表.xlsx", "EXCEL", filePath);
        AttachmentStorageService service = mock(AttachmentStorageService.class);
        when(service.downloadFile(9001L)).thenReturn(Optional.of(new AttachmentStorageService.DownloadFile(
                attachment, filePath)));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PublicAttachmentController(service)).build();

        mockMvc.perform(get("/api/public/attachments/9001/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_OCTET_STREAM_VALUE)))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().bytes("xlsx-content".getBytes()));
    }

    @Test
    void downloadEndpointReturnsContract404() throws Exception
    {
        AttachmentStorageService service = mock(AttachmentStorageService.class);
        when(service.downloadFile(9001L)).thenReturn(Optional.empty());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PublicAttachmentController(service)).build();

        mockMvc.perform(get("/api/public/attachments/9001/download"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("附件不存在"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    private static CbAttachment attachment(Long id, String fileName, String fileType, Path path)
    {
        CbAttachment attachment = new CbAttachment();
        attachment.setId(id);
        attachment.setFileName(fileName);
        attachment.setFileType(fileType);
        attachment.setFileSize(12L);
        attachment.setStoragePath(path.toString());
        attachment.setCreatedAt(LocalDateTime.now());
        return attachment;
    }
}
