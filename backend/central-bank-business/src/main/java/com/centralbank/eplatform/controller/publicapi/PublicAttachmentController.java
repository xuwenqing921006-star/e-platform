package com.centralbank.eplatform.controller.publicapi;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.centralbank.eplatform.dto.ApiResponse;
import com.centralbank.eplatform.service.AttachmentStorageService;

@RestController
@RequestMapping("/api/public/attachments")
public class PublicAttachmentController
{
    private final AttachmentStorageService attachmentStorageService;

    public PublicAttachmentController(AttachmentStorageService attachmentStorageService)
    {
        this.attachmentStorageService = attachmentStorageService;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable Long id) throws IOException
    {
        return attachmentStorageService.downloadFile(id)
                .<ResponseEntity<?>>map(downloadFile -> {
                    try
                    {
                        String encodedFileName = URLEncoder.encode(
                                downloadFile.attachment().getFileName(), StandardCharsets.UTF_8)
                                .replace("+", "%20");
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                        headers.setContentDisposition(ContentDisposition.attachment()
                                .filename(downloadFile.attachment().getFileName(), StandardCharsets.UTF_8)
                                .build());
                        headers.set("download-filename", encodedFileName);
                        headers.set("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
                        return ResponseEntity.ok()
                                .headers(headers)
                                .body(new InputStreamResource(java.nio.file.Files.newInputStream(downloadFile.path())));
                    }
                    catch (IOException e)
                    {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "附件不存在"));
                    }
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "附件不存在")));
    }
}
