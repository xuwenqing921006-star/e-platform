package com.centralbank.eplatform.controller.publicapi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.centralbank.eplatform.dto.ApiResponse;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.dto.PublicProductDetailData;
import com.centralbank.eplatform.dto.PublicProductListItem;
import com.centralbank.eplatform.service.PublicProductService;

@RestController
@RequestMapping("/api/public/products")
public class PublicProductController
{
    private final PublicProductService publicProductService;

    public PublicProductController(PublicProductService publicProductService)
    {
        this.publicProductService = publicProductService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedData<PublicProductListItem>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(publicProductService.list(page, pageSize)));
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublicProductDetailData>> detail(@PathVariable Long id)
    {
        return publicProductService.detail(id)
                .map(data -> ResponseEntity.ok(ApiResponse.success(data)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "产品不存在")));
    }
}
