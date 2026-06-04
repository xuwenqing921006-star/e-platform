package com.centralbank.eplatform.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.centralbank.eplatform.domain.CbAccountExtension;
import com.centralbank.eplatform.domain.CbAdminAccount;
import com.centralbank.eplatform.dto.AdminAccountCreateData;
import com.centralbank.eplatform.dto.AdminAccountDeleteData;
import com.centralbank.eplatform.dto.AdminAccountDetailData;
import com.centralbank.eplatform.dto.AdminAccountListItem;
import com.centralbank.eplatform.dto.AdminAccountRequest;
import com.centralbank.eplatform.dto.AdminAccountResetPasswordData;
import com.centralbank.eplatform.dto.AdminAccountUpdateData;
import com.centralbank.eplatform.dto.OptionItem;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.mapper.CbAccountExtensionMapper;
import com.centralbank.eplatform.mapper.CbAdminAccountMapper;
import com.ruoyi.common.utils.SecurityUtils;

@Service
public class AdminAccountService
{
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_OFFICE_USER = "OFFICE_USER";
    private static final Long CENTRAL_BANK_COMMON_ROLE_ID = 2L;

    private final CbAdminAccountMapper accountMapper;
    private final CbAccountExtensionMapper accountExtensionMapper;
    private final FixedOptionsService fixedOptionsService;
    private final AdminOperatorContext operatorContext;
    private final AuditLogRecorder auditLogRecorder;

    public AdminAccountService(CbAdminAccountMapper accountMapper, CbAccountExtensionMapper accountExtensionMapper,
            FixedOptionsService fixedOptionsService, AdminOperatorContext operatorContext)
    {
        this(accountMapper, accountExtensionMapper, fixedOptionsService, operatorContext, AuditLogRecorder.noop());
    }

    @Autowired
    public AdminAccountService(CbAdminAccountMapper accountMapper, CbAccountExtensionMapper accountExtensionMapper,
            FixedOptionsService fixedOptionsService, AdminOperatorContext operatorContext,
            AuditLogRecorder auditLogRecorder)
    {
        this.accountMapper = accountMapper;
        this.accountExtensionMapper = accountExtensionMapper;
        this.fixedOptionsService = fixedOptionsService;
        this.operatorContext = operatorContext;
        this.auditLogRecorder = auditLogRecorder;
    }

    public PaginatedData<AdminAccountListItem> list(String keyword, String officeCode, String role, int page,
            int pageSize)
    {
        assertAdmin();
        validatePage(page, pageSize);
        validateOfficeFilter(officeCode);
        validateRoleFilter(role);
        int offset = (page - 1) * pageSize;
        int total = accountMapper.countAdminAccounts(normalizeBlank(keyword), normalizeBlank(officeCode),
                normalizeBlank(role));
        List<AdminAccountListItem> items = accountMapper
                .selectAdminAccounts(normalizeBlank(keyword), normalizeBlank(officeCode), normalizeBlank(role), offset,
                        pageSize)
                .stream()
                .map(this::toListItem)
                .toList();
        return new PaginatedData<>(items, total, page, pageSize);
    }

    public AdminAccountCreateData create(AdminAccountRequest request)
    {
        assertAdmin();
        validateCreateRequest(request);
        if (accountMapper.selectUserIdByUsername(request.username().trim()) != null)
        {
            throw new AdminAccountException(409, "登录账号已存在");
        }
        AccountRoleOffice roleOffice = validateRoleOffice(request.role(), request.officeCode());
        CbAdminAccount account = new CbAdminAccount();
        account.setUsername(request.username().trim());
        account.setDisplayName(request.displayName().trim());
        account.setPassword(SecurityUtils.encryptPassword(request.initialPassword().trim()));
        boolean enabled = resolveEnabled(request.enabled());
        account.setStatus(enabled ? "0" : "1");
        accountMapper.insertSysUser(account);
        accountMapper.insertUserRole(account.getId(), CENTRAL_BANK_COMMON_ROLE_ID);
        accountMapper.insertAccountExtension(buildExtension(account.getId(), roleOffice, enabled));
        auditLogRecorder.record("ACCOUNT", "ACCOUNT", account.getUsername(), "创建后台账号");
        return new AdminAccountCreateData(account.getId());
    }

    public Optional<AdminAccountDetailData> detail(Long id)
    {
        assertAdmin();
        CbAdminAccount account = selectExisting(id);
        return account == null ? Optional.empty() : Optional.of(toDetailData(account));
    }

    public AdminAccountUpdateData update(Long id, AdminAccountRequest request)
    {
        assertAdmin();
        CbAdminAccount existing = selectExisting(id);
        if (existing == null)
        {
            throw new AdminAccountException(404, "账号不存在");
        }
        validateUpdateRequest(request);
        AccountRoleOffice roleOffice = validateRoleOffice(request.role(), request.officeCode());
        existing.setDisplayName(request.displayName().trim());
        boolean enabled = resolveEnabled(request.enabled());
        existing.setStatus(enabled ? "0" : "1");
        accountMapper.updateSysUser(existing);
        accountMapper.updateAccountExtension(buildExtension(existing.getId(), roleOffice, enabled));
        auditLogRecorder.record("ACCOUNT", "ACCOUNT", existing.getUsername(), "编辑后台账号");
        return new AdminAccountUpdateData(existing.getId(), true);
    }

    public AdminAccountDeleteData delete(Long id)
    {
        assertAdmin();
        if (id != null && id.equals(operatorContext.currentUserId()))
        {
            throw new AdminAccountException(409, "不能删除当前登录账号");
        }
        CbAdminAccount existing = selectExisting(id);
        if (existing == null)
        {
            throw new AdminAccountException(404, "账号不存在");
        }
        accountMapper.deleteAccountExtensionByUserId(existing.getId());
        accountMapper.deleteUserRolesByUserId(existing.getId());
        accountMapper.deleteSysUserById(existing.getId());
        auditLogRecorder.record("ACCOUNT", "ACCOUNT", existing.getUsername(), "删除后台账号");
        return new AdminAccountDeleteData(true);
    }

    public AdminAccountResetPasswordData resetPassword(Long id, String newPassword)
    {
        assertAdmin();
        CbAdminAccount existing = selectExisting(id);
        if (existing == null)
        {
            throw new AdminAccountException(404, "账号不存在");
        }
        requireText(newPassword, "新密码不能为空");
        accountMapper.resetPassword(existing.getId(), SecurityUtils.encryptPassword(newPassword.trim()));
        auditLogRecorder.record("PASSWORD", "ACCOUNT", existing.getUsername(), "重置后台账号密码");
        return new AdminAccountResetPasswordData(true);
    }

    private void assertAdmin()
    {
        Long userId = operatorContext.currentUserId();
        if (userId != null && userId == 1L)
        {
            return;
        }
        CbAccountExtension extension = accountExtensionMapper.selectByUserId(userId);
        if (extension != null && Boolean.TRUE.equals(extension.getEnabled()) && ROLE_ADMIN.equals(extension.getRole()))
        {
            return;
        }
        throw new AdminAccountException(403, "仅管理员可管理账号");
    }

    private void validateCreateRequest(AdminAccountRequest request)
    {
        if (request == null)
        {
            throw new AdminAccountException(400, "请求体不能为空");
        }
        requireText(request.username(), "登录账号不能为空");
        requireText(request.displayName(), "姓名不能为空");
        requireText(request.initialPassword(), "初始密码不能为空");
        validateRole(request.role());
    }

    private void validateUpdateRequest(AdminAccountRequest request)
    {
        if (request == null)
        {
            throw new AdminAccountException(400, "请求体不能为空");
        }
        requireText(request.displayName(), "姓名不能为空");
        validateRole(request.role());
    }

    private AccountRoleOffice validateRoleOffice(String role, String officeCode)
    {
        if (ROLE_ADMIN.equals(role))
        {
            return new AccountRoleOffice(ROLE_ADMIN, null, null);
        }
        String normalizedOffice = normalizeBlank(officeCode);
        if (normalizedOffice == null)
        {
            throw new AdminAccountException(400, "普通账号必须绑定一个办公室");
        }
        OptionItem office = fixedOptionsService.findOffice(normalizedOffice)
                .orElseThrow(() -> new AdminAccountException(400, "所属机构不合法"));
        return new AccountRoleOffice(ROLE_OFFICE_USER, office.value(), office.label());
    }

    private CbAccountExtension buildExtension(Long userId, AccountRoleOffice roleOffice, boolean enabled)
    {
        LocalDateTime now = LocalDateTime.now();
        CbAccountExtension extension = new CbAccountExtension();
        extension.setId(nextExtensionId());
        extension.setUserId(userId);
        extension.setRole(roleOffice.role());
        extension.setOfficeCode(roleOffice.officeCode());
        extension.setOfficeName(roleOffice.officeName());
        extension.setEnabled(enabled);
        extension.setCreatedAt(now);
        extension.setUpdatedAt(now);
        return extension;
    }

    private CbAdminAccount selectExisting(Long id)
    {
        if (id == null || id <= 0)
        {
            return null;
        }
        return accountMapper.selectAdminAccountByUserId(id);
    }

    private void validateRoleFilter(String role)
    {
        if (!isBlank(role))
        {
            validateRole(role);
        }
    }

    private void validateRole(String role)
    {
        if (!ROLE_ADMIN.equals(role) && !ROLE_OFFICE_USER.equals(role))
        {
            throw new AdminAccountException(400, "账号角色不合法");
        }
    }

    private void validateOfficeFilter(String officeCode)
    {
        if (!isBlank(officeCode) && fixedOptionsService.findOffice(officeCode).isEmpty())
        {
            throw new AdminAccountException(400, "所属机构不合法");
        }
    }

    private void validatePage(int page, int pageSize)
    {
        if (page < 1 || pageSize < 1 || pageSize > 100)
        {
            throw new AdminAccountException(400, "分页参数不合法");
        }
    }

    private void requireText(String value, String message)
    {
        if (isBlank(value))
        {
            throw new AdminAccountException(400, message);
        }
    }

    private AdminAccountListItem toListItem(CbAdminAccount account)
    {
        return new AdminAccountListItem(account.getId(), account.getUsername(), account.getDisplayName(),
                account.getRole(), account.getOfficeCode(), account.getOfficeName(), Boolean.TRUE.equals(
                        account.getEnabled()));
    }

    private AdminAccountDetailData toDetailData(CbAdminAccount account)
    {
        return new AdminAccountDetailData(account.getId(), account.getUsername(), account.getDisplayName(),
                account.getRole(), account.getOfficeCode(), account.getOfficeName(), Boolean.TRUE.equals(
                        account.getEnabled()));
    }

    private Long nextExtensionId()
    {
        return System.currentTimeMillis() * 1000L + ThreadLocalRandom.current().nextInt(1000);
    }

    private boolean resolveEnabled(Boolean enabled)
    {
        return !Boolean.FALSE.equals(enabled);
    }

    private String normalizeBlank(String value)
    {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }

    private record AccountRoleOffice(String role, String officeCode, String officeName)
    {
    }
}
