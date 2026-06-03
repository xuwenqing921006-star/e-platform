package com.centralbank.eplatform.controller.admin;

import java.util.Optional;
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
import com.centralbank.eplatform.dto.AdminAccountCreateData;
import com.centralbank.eplatform.dto.AdminAccountDeleteData;
import com.centralbank.eplatform.dto.AdminAccountDetailData;
import com.centralbank.eplatform.dto.AdminAccountListItem;
import com.centralbank.eplatform.dto.AdminAccountRequest;
import com.centralbank.eplatform.dto.AdminAccountResetPasswordData;
import com.centralbank.eplatform.dto.AdminAccountResetPasswordRequest;
import com.centralbank.eplatform.dto.AdminAccountUpdateData;
import com.centralbank.eplatform.dto.ApiResponse;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.service.AdminAccountException;
import com.centralbank.eplatform.service.AdminAccountService;

@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController
{
    private final AdminAccountService accountService;

    public AdminAccountController(AdminAccountService accountService)
    {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedData<AdminAccountListItem>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "office_code", required = false) String officeCode,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(accountService.list(keyword, officeCode, role, page,
                    pageSize)));
        }
        catch (AdminAccountException e)
        {
            return error(e);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminAccountCreateData>> create(@RequestBody AdminAccountRequest request)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(accountService.create(request)));
        }
        catch (AdminAccountException e)
        {
            return error(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminAccountDetailData>> detail(@PathVariable Long id)
    {
        try
        {
            Optional<AdminAccountDetailData> detail = accountService.detail(id);
            return detail.map(data -> ResponseEntity.ok(ApiResponse.success(data)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "账号不存在")));
        }
        catch (AdminAccountException e)
        {
            return error(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminAccountUpdateData>> update(@PathVariable Long id,
            @RequestBody AdminAccountRequest request)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(accountService.update(id, request)));
        }
        catch (AdminAccountException e)
        {
            return error(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminAccountDeleteData>> delete(@PathVariable Long id)
    {
        try
        {
            return ResponseEntity.ok(ApiResponse.success(accountService.delete(id)));
        }
        catch (AdminAccountException e)
        {
            return error(e);
        }
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<AdminAccountResetPasswordData>> resetPassword(@PathVariable Long id,
            @RequestBody AdminAccountResetPasswordRequest request)
    {
        try
        {
            String newPassword = request == null ? null : request.newPassword();
            return ResponseEntity.ok(ApiResponse.success(accountService.resetPassword(id, newPassword)));
        }
        catch (AdminAccountException e)
        {
            return error(e);
        }
    }

    private <T> ResponseEntity<ApiResponse<T>> error(AdminAccountException e)
    {
        return ResponseEntity.status(e.statusCode()).body(ApiResponse.error(e.statusCode(), e.getMessage()));
    }
}
