package com.centralbank.eplatform.controller.admin;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.centralbank.eplatform.dto.AdminAttachmentDeleteData;
import com.centralbank.eplatform.dto.ApiResponse;
import com.centralbank.eplatform.dto.PublicAttachment;
import com.centralbank.eplatform.service.AttachmentException;
import com.centralbank.eplatform.service.AttachmentStorageService;

@RestController
@RequestMapping("/api/admin/attachments")
public class AdminAttachmentController
{
    private final AttachmentStorageService attachmentStorageService;

    public AdminAttachmentController(AttachmentStorageService attachmentStorageService)
    {
        this.attachmentStorageService = attachmentStorageService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PublicAttachment>> upload(@RequestParam("file") MultipartFile file)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(attachmentStorageService.upload(file)));
        }
        catch (AttachmentException e)
        {
            return ResponseEntity.status(e.statusCode()).body(ApiResponse.error(e.statusCode(), e.getMessage()));
        }
        catch (IOException e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "附件保存失败"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminAttachmentDeleteData>> delete(@PathVariable Long id)
    {
        try
        {
            if (!attachmentStorageService.delete(id))
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "附件不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(new AdminAttachmentDeleteData(true)));
        }
        catch (IOException e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "附件删除失败"));
        }
    }
}
