package com.centralbank.eplatform.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.centralbank.eplatform.dto.AdminAuditLogListItem;
import com.centralbank.eplatform.dto.ApiResponse;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.service.AuditLogException;
import com.centralbank.eplatform.service.AuditLogService;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditLogController
{
    private final AuditLogService auditLogService;

    public AdminAuditLogController(AuditLogService auditLogService)
    {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedData<AdminAuditLogListItem>>> list(
            @RequestParam(name = "operator_keyword", required = false) String operatorKeyword,
            @RequestParam(name = "operation_type", required = false) String operationType,
            @RequestParam(name = "operated_from", required = false) String operatedFrom,
            @RequestParam(name = "operated_to", required = false) String operatedTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(auditLogService.list(operatorKeyword, operationType,
                    operatedFrom, operatedTo, page, pageSize)));
        }
        catch (AuditLogException e)
        {
            return ResponseEntity.status(e.statusCode()).body(ApiResponse.error(e.statusCode(), e.getMessage()));
        }
    }
}
