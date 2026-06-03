package com.centralbank.eplatform.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import com.centralbank.eplatform.domain.CbAccountExtension;
import com.centralbank.eplatform.domain.CbAdminAccount;
import com.centralbank.eplatform.domain.CbAuditLog;
import com.centralbank.eplatform.dto.AdminAuditLogListItem;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.mapper.CbAccountExtensionMapper;
import com.centralbank.eplatform.mapper.CbAdminAccountMapper;
import com.centralbank.eplatform.mapper.CbAuditLogMapper;

@Service
public class AuditLogService implements AuditLogRecorder
{
    private static final String ROLE_ADMIN = "ADMIN";
    private static final ZoneOffset CHINA_OFFSET = ZoneOffset.ofHours(8);
    private static final Map<String, Integer> BUSINESS_TYPES = Map.of(
            "CREATE", 1,
            "UPDATE", 2,
            "DELETE", 3,
            "IMPORT", 6,
            "ACCOUNT", 10,
            "PASSWORD", 11);
    private static final Pattern SENSITIVE_PAIR = Pattern.compile(
            "(?i)(password|initial_password|new_password|confirm_password|jwt_secret|jwt secret|secret|token)\\s*[:=]\\s*[^\\s,;，；]+");

    private final CbAuditLogMapper auditLogMapper;
    private final CbAccountExtensionMapper accountExtensionMapper;
    private final CbAdminAccountMapper adminAccountMapper;
    private final AdminOperatorContext operatorContext;

    public AuditLogService(CbAuditLogMapper auditLogMapper, CbAccountExtensionMapper accountExtensionMapper,
            CbAdminAccountMapper adminAccountMapper, AdminOperatorContext operatorContext)
    {
        this.auditLogMapper = auditLogMapper;
        this.accountExtensionMapper = accountExtensionMapper;
        this.adminAccountMapper = adminAccountMapper;
        this.operatorContext = operatorContext;
    }

    public PaginatedData<AdminAuditLogListItem> list(String operatorKeyword, String operationType,
            String operatedFrom, String operatedTo, int page, int pageSize)
    {
        assertAdmin();
        validatePage(page, pageSize);
        Integer businessType = businessType(operationType);
        LocalDateTime from = parseDateStart(operatedFrom);
        LocalDateTime to = parseDateEnd(operatedTo);
        int offset = (page - 1) * pageSize;
        int total = auditLogMapper.countAuditLogs(normalizeBlank(operatorKeyword), businessType, from, to);
        var items = auditLogMapper.selectAuditLogs(normalizeBlank(operatorKeyword), businessType, from, to, offset,
                        pageSize)
                .stream()
                .map(this::toListItem)
                .toList();
        return new PaginatedData<>(items, total, page, pageSize);
    }

    @Override
    public void record(String operationType, String objectType, String objectName, String description)
    {
        CbAuditLog log = new CbAuditLog();
        log.setOperatorName(currentOperatorName());
        log.setOperationType(normalizeOperationType(operationType));
        log.setBusinessType(businessType(log.getOperationType()));
        log.setObjectType(sanitize(objectType));
        log.setObjectName(sanitize(objectName));
        log.setDescription(sanitize(description));
        log.setOperatedAt(LocalDateTime.now());
        auditLogMapper.insertAuditLog(log);
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
        throw new AuditLogException(403, "仅管理员可查看操作日志");
    }

    private AdminAuditLogListItem toListItem(CbAuditLog log)
    {
        return new AdminAuditLogListItem(log.getId(), log.getOperatorName(), log.getOperationType(),
                log.getObjectType(), log.getObjectName(), log.getDescription(), format(log.getOperatedAt()));
    }

    private String currentOperatorName()
    {
        CbAdminAccount account = adminAccountMapper.selectAdminAccountByUserId(operatorContext.currentUserId());
        if (account == null)
        {
            return "未知用户";
        }
        if (!isBlank(account.getDisplayName()))
        {
            return account.getDisplayName();
        }
        return account.getUsername();
    }

    private Integer businessType(String operationType)
    {
        String normalized = normalizeBlank(operationType);
        if (normalized == null)
        {
            return null;
        }
        Integer businessType = BUSINESS_TYPES.get(normalized);
        if (businessType == null)
        {
            throw new AuditLogException(400, "操作类型不合法");
        }
        return businessType;
    }

    private String normalizeOperationType(String operationType)
    {
        return businessType(operationType) == null ? "UPDATE" : operationType.trim();
    }

    private LocalDateTime parseDateStart(String value)
    {
        String normalized = normalizeBlank(value);
        if (normalized == null)
        {
            return null;
        }
        try
        {
            return LocalDate.parse(normalized).atStartOfDay();
        }
        catch (DateTimeParseException e)
        {
            throw new AuditLogException(400, "操作开始日期不合法");
        }
    }

    private LocalDateTime parseDateEnd(String value)
    {
        String normalized = normalizeBlank(value);
        if (normalized == null)
        {
            return null;
        }
        try
        {
            return LocalDate.parse(normalized).atTime(23, 59, 59);
        }
        catch (DateTimeParseException e)
        {
            throw new AuditLogException(400, "操作结束日期不合法");
        }
    }

    private void validatePage(int page, int pageSize)
    {
        if (page < 1 || pageSize < 1 || pageSize > 100)
        {
            throw new AuditLogException(400, "分页参数不合法");
        }
    }

    private String sanitize(String value)
    {
        if (value == null)
        {
            return null;
        }
        return SENSITIVE_PAIR.matcher(value).replaceAll("$1=已脱敏");
    }

    private String format(LocalDateTime time)
    {
        return time == null ? null : time.atOffset(CHINA_OFFSET).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private String normalizeBlank(String value)
    {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }
}
