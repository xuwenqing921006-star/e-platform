package com.centralbank.eplatform.controller.admin;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.centralbank.eplatform.dto.AdminContentCreateData;
import com.centralbank.eplatform.dto.AdminContentDeleteData;
import com.centralbank.eplatform.dto.AdminContentDetailData;
import com.centralbank.eplatform.dto.AdminContentListItem;
import com.centralbank.eplatform.dto.AdminContentRequest;
import com.centralbank.eplatform.dto.AdminContentUpdateData;
import com.centralbank.eplatform.dto.ApiResponse;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.service.AdminContentException;
import com.centralbank.eplatform.service.AdminContentService;

@RestController
@RequestMapping("/api/admin/contents")
public class AdminContentController
{
    private final AdminContentService adminContentService;

    public AdminContentController(AdminContentService adminContentService)
    {
        this.adminContentService = adminContentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedData<AdminContentListItem>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(name = "office_code", required = false) String officeCode,
            @RequestParam(name = "published_from", required = false) String publishedFrom,
            @RequestParam(name = "published_to", required = false) String publishedTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(adminContentService.list(keyword, category, officeCode,
                    publishedFrom, publishedTo, page, pageSize)));
        }
        catch (AdminContentException e)
        {
            return error(e);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminContentCreateData>> create(@RequestBody AdminContentRequest request)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(adminContentService.create(request)));
        }
        catch (AdminContentException e)
        {
            return error(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminContentDetailData>> detail(@PathVariable Long id)
    {
        try
        {
            return adminContentService.detail(id)
                    .map(data -> ResponseEntity.ok(ApiResponse.success(data)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "内容不存在")));
        }
        catch (AdminContentException e)
        {
            return error(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminContentUpdateData>> update(@PathVariable Long id,
            @RequestBody AdminContentRequest request)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(adminContentService.update(id, request)));
        }
        catch (AdminContentException e)
        {
            return error(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminContentDeleteData>> delete(@PathVariable Long id)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(adminContentService.delete(id)));
        }
        catch (AdminContentException e)
        {
            return error(e);
        }
        catch (IOException e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "内容删除失败"));
        }
    }

    private <T> ResponseEntity<ApiResponse<T>> error(AdminContentException e)
    {
        return ResponseEntity.status(e.statusCode()).body(ApiResponse.error(e.statusCode(), e.getMessage()));
    }
}
