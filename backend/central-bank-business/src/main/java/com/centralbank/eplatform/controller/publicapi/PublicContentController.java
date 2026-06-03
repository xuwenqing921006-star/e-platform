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
import com.centralbank.eplatform.dto.PublicContentDetailData;
import com.centralbank.eplatform.dto.PublicContentListItem;
import com.centralbank.eplatform.service.PublicContentService;

@RestController
@RequestMapping("/api/public/contents")
public class PublicContentController
{
    private final PublicContentService publicContentService;

    public PublicContentController(PublicContentService publicContentService)
    {
        this.publicContentService = publicContentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedData<PublicContentListItem>>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String scope,
            @RequestParam(name = "county_code", required = false) String countyCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(
                    publicContentService.list(category, scope, countyCode, page, pageSize)));
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublicContentDetailData>> detail(@PathVariable Long id)
    {
        return publicContentService.detail(id)
                .map(data -> ResponseEntity.ok(ApiResponse.success(data)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "内容不存在")));
    }
}
