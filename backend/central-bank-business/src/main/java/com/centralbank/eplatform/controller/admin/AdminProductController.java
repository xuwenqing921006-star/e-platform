package com.centralbank.eplatform.controller.admin;

import java.io.IOException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;
import com.centralbank.eplatform.dto.AdminProductCreateData;
import com.centralbank.eplatform.dto.AdminProductDeleteData;
import com.centralbank.eplatform.dto.AdminProductDetailData;
import com.centralbank.eplatform.dto.AdminProductImportCommitData;
import com.centralbank.eplatform.dto.AdminProductImportCommitRequest;
import com.centralbank.eplatform.dto.AdminProductImportValidateData;
import com.centralbank.eplatform.dto.AdminProductListItem;
import com.centralbank.eplatform.dto.AdminProductRequest;
import com.centralbank.eplatform.dto.AdminProductUpdateData;
import com.centralbank.eplatform.dto.ApiResponse;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.service.AdminProductException;
import com.centralbank.eplatform.service.AdminProductService;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController
{
    private static final String TEMPLATE_FILENAME = "financial-product-import-template.xlsx";
    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService)
    {
        this.adminProductService = adminProductService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedData<AdminProductListItem>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "bank_code", required = false) String bankCode,
            @RequestParam(name = "product_type", required = false) String productType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(adminProductService.list(keyword, bankCode, productType,
                    page, pageSize)));
        }
        catch (AdminProductException e)
        {
            return error(e);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminProductCreateData>> create(@RequestBody AdminProductRequest request)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(adminProductService.create(request)));
        }
        catch (AdminProductException e)
        {
            return error(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminProductDetailData>> detail(@PathVariable Long id)
    {
        try
        {
            return adminProductService.detail(id)
                    .map(data -> ResponseEntity.ok(ApiResponse.success(data)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "产品不存在")));
        }
        catch (AdminProductException e)
        {
            return error(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminProductUpdateData>> update(@PathVariable Long id,
            @RequestBody AdminProductRequest request)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(adminProductService.update(id, request)));
        }
        catch (AdminProductException e)
        {
            return error(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminProductDeleteData>> delete(@PathVariable Long id)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(adminProductService.delete(id)));
        }
        catch (AdminProductException e)
        {
            return error(e);
        }
    }

    @GetMapping("/import-template/download")
    public ResponseEntity<?> downloadTemplate()
    {
        try
        {
            byte[] body = adminProductService.importTemplate();
            return ResponseEntity.ok()
                    .contentType(XLSX_MEDIA_TYPE)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(TEMPLATE_FILENAME).build().toString())
                    .body(body);
        }
        catch (AdminProductException e)
        {
            return error(e);
        }
        catch (IOException e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "模板下载失败"));
        }
    }

    @PostMapping("/import/validate")
    public ResponseEntity<ApiResponse<AdminProductImportValidateData>> validateImport(
            @RequestParam("file") MultipartFile file)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(adminProductService.validateImport(file)));
        }
        catch (AdminProductException e)
        {
            return error(e);
        }
    }

    @PostMapping("/import/commit")
    public ResponseEntity<ApiResponse<AdminProductImportCommitData>> commitImport(
            @RequestBody AdminProductImportCommitRequest request)
    {
        try
        {
            String importToken = request == null ? null : request.importToken();
            return ResponseEntity.ok(ApiResponse.success(adminProductService.commitImport(importToken)));
        }
        catch (AdminProductException e)
        {
            return error(e);
        }
    }

    private <T> ResponseEntity<ApiResponse<T>> error(AdminProductException e)
    {
        return ResponseEntity.status(e.statusCode()).body(ApiResponse.error(e.statusCode(), e.getMessage()));
    }
}
