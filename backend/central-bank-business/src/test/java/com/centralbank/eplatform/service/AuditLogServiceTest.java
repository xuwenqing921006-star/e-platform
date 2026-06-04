package com.centralbank.eplatform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import com.centralbank.eplatform.domain.CbAccountExtension;
import com.centralbank.eplatform.domain.CbAdminAccount;
import com.centralbank.eplatform.domain.CbAuditLog;
import com.centralbank.eplatform.mapper.CbAccountExtensionMapper;
import com.centralbank.eplatform.mapper.CbAdminAccountMapper;
import com.centralbank.eplatform.mapper.CbAuditLogMapper;
import org.junit.jupiter.api.Test;

class AuditLogServiceTest
{
    @Test
    void adminListsAuditLogsWithFiltersAndContractTime()
    {
        Fixture fixture = fixture(1L);

        var page = fixture.service.list("系统", "IMPORT", "2026-05-01", "2026-05-31", 1, 20);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).operatorName()).isEqualTo("系统管理员");
        assertThat(page.items().get(0).operationType()).isEqualTo("IMPORT");
        assertThat(page.items().get(0).objectType()).isEqualTo("FINANCIAL_PRODUCT");
        assertThat(page.items().get(0).objectName()).isEqualTo("金融产品");
        assertThat(page.items().get(0).description()).isEqualTo("导入 112 条金融产品数据");
        assertThat(page.items().get(0).operatedAt()).isEqualTo("2026-05-30T15:16:00+08:00");
    }

    @Test
    void nonAdminCannotViewAuditLogs()
    {
        Fixture fixture = fixture(10L);

        assertThatThrownBy(() -> fixture.service.list(null, null, null, null, 1, 20))
                .isInstanceOf(AuditLogException.class)
                .hasMessage("仅管理员可查看操作日志")
                .extracting("statusCode")
                .isEqualTo(403);
    }

    @Test
    void recordAuditLogSanitizesSensitiveValues()
    {
        Fixture fixture = fixture(1L);

        fixture.service.record("PASSWORD", "ACCOUNT", "zxglk",
                "new_password=ResetPassword1! JWT_SECRET=secret-value token=raw-token");

        CbAuditLog inserted = fixture.auditLogMapper.insertedLogs().get(0);
        assertThat(inserted.getOperatorName()).isEqualTo("系统管理员");
        assertThat(inserted.getOperationType()).isEqualTo("PASSWORD");
        assertThat(inserted.getDescription()).contains("已脱敏");
        assertThat(inserted.getDescription()).doesNotContain("ResetPassword1!", "secret-value", "raw-token");
    }

    private Fixture fixture(Long operatorId)
    {
        List<CbAuditLog> auditLogs = new ArrayList<>(List.of(
                log(7001L, "系统管理员", "IMPORT", "FINANCIAL_PRODUCT", "金融产品", "导入 112 条金融产品数据",
                        LocalDateTime.of(2026, 5, 30, 15, 16)),
                log(7002L, "张伟", "UPDATE", "CONTENT", "征信代理查询网点地址及电话", "更新附件与正文说明",
                        LocalDateTime.of(2026, 5, 29, 10, 8))));
        FakeAuditLogMapper auditLogMapper = new FakeAuditLogMapper(auditLogs);
        FakeAccountExtensionMapper extensionMapper = new FakeAccountExtensionMapper(List.of(
                extension(1L, "ADMIN", true),
                extension(10L, "OFFICE_USER", true)));
        FakeAdminAccountMapper accountMapper = new FakeAdminAccountMapper(List.of(
                user(1L, "admin", "系统管理员"),
                user(10L, "zxglk", "张伟")));
        AuditLogService service = new AuditLogService(auditLogMapper, extensionMapper, accountMapper, () -> operatorId);
        return new Fixture(service, auditLogMapper);
    }

    private static CbAuditLog log(Long id, String operatorName, String operationType, String objectType,
            String objectName, String description, LocalDateTime operatedAt)
    {
        CbAuditLog log = new CbAuditLog();
        log.setId(id);
        log.setOperatorName(operatorName);
        log.setOperationType(operationType);
        log.setObjectType(objectType);
        log.setObjectName(objectName);
        log.setDescription(description);
        log.setOperatedAt(operatedAt);
        return log;
    }

    private static CbAccountExtension extension(Long userId, String role, boolean enabled)
    {
        CbAccountExtension extension = new CbAccountExtension();
        extension.setUserId(userId);
        extension.setRole(role);
        extension.setEnabled(enabled);
        return extension;
    }

    private static CbAdminAccount user(Long id, String username, String displayName)
    {
        CbAdminAccount account = new CbAdminAccount();
        account.setId(id);
        account.setUsername(username);
        account.setDisplayName(displayName);
        return account;
    }

    private record Fixture(AuditLogService service, FakeAuditLogMapper auditLogMapper)
    {
    }

    private static class FakeAuditLogMapper implements CbAuditLogMapper
    {
        private final List<CbAuditLog> logs;
        private final List<CbAuditLog> insertedLogs = new ArrayList<>();

        FakeAuditLogMapper(List<CbAuditLog> logs)
        {
            this.logs = logs;
        }

        List<CbAuditLog> insertedLogs()
        {
            return insertedLogs;
        }

        @Override
        public int countAuditLogs(String operatorKeyword, Integer businessType, LocalDateTime operatedFrom,
                LocalDateTime operatedTo)
        {
            return (int) filtered(operatorKeyword, businessType, operatedFrom, operatedTo).count();
        }

        @Override
        public List<CbAuditLog> selectAuditLogs(String operatorKeyword, Integer businessType,
                LocalDateTime operatedFrom, LocalDateTime operatedTo, int offset, int pageSize)
        {
            return filtered(operatorKeyword, businessType, operatedFrom, operatedTo)
                    .sorted(Comparator.comparing(CbAuditLog::getId).reversed())
                    .skip(offset)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public int insertAuditLog(CbAuditLog auditLog)
        {
            insertedLogs.add(auditLog);
            return 1;
        }

        private java.util.stream.Stream<CbAuditLog> filtered(String operatorKeyword, Integer businessType,
                LocalDateTime operatedFrom, LocalDateTime operatedTo)
        {
            return logs.stream()
                    .filter(log -> operatorKeyword == null || log.getOperatorName().contains(operatorKeyword))
                    .filter(log -> businessType == null || Objects.equals(businessType, businessType(log.getOperationType())))
                    .filter(log -> operatedFrom == null || !log.getOperatedAt().isBefore(operatedFrom))
                    .filter(log -> operatedTo == null || !log.getOperatedAt().isAfter(operatedTo));
        }

        private Integer businessType(String operationType)
        {
            return switch (operationType)
            {
                case "CREATE" -> 1;
                case "UPDATE" -> 2;
                case "DELETE" -> 3;
                case "IMPORT" -> 6;
                case "ACCOUNT" -> 10;
                case "PASSWORD" -> 11;
                default -> 0;
            };
        }
    }

    private static class FakeAccountExtensionMapper implements CbAccountExtensionMapper
    {
        private final List<CbAccountExtension> extensions;

        FakeAccountExtensionMapper(List<CbAccountExtension> extensions)
        {
            this.extensions = extensions;
        }

        @Override
        public int countAccountExtensions()
        {
            return extensions.size();
        }

        @Override
        public CbAccountExtension selectByUserId(Long userId)
        {
            return extensions.stream().filter(item -> Objects.equals(item.getUserId(), userId)).findFirst()
                    .orElse(null);
        }
    }

    private static class FakeAdminAccountMapper implements CbAdminAccountMapper
    {
        private final List<CbAdminAccount> accounts;

        FakeAdminAccountMapper(List<CbAdminAccount> accounts)
        {
            this.accounts = accounts;
        }

        @Override
        public CbAdminAccount selectAdminAccountByUserId(Long userId)
        {
            return accounts.stream().filter(item -> Objects.equals(item.getId(), userId)).findFirst().orElse(null);
        }

        @Override
        public int countAdminAccounts(String keyword, String officeCode, String role)
        {
            return 0;
        }

        @Override
        public List<CbAdminAccount> selectAdminAccounts(String keyword, String officeCode, String role, int offset,
                int pageSize)
        {
            return List.of();
        }

        @Override
        public Long selectUserIdByUsername(String username)
        {
            return null;
        }

        @Override
        public int insertSysUser(CbAdminAccount account)
        {
            return 0;
        }

        @Override
        public int insertUserRole(Long userId, Long roleId)
        {
            return 0;
        }

        @Override
        public int updateSysUser(CbAdminAccount account)
        {
            return 0;
        }

        @Override
        public int resetPassword(Long userId, String password)
        {
            return 0;
        }

        @Override
        public int deleteSysUserById(Long userId)
        {
            return 0;
        }

        @Override
        public int deleteUserRolesByUserId(Long userId)
        {
            return 0;
        }

        @Override
        public int insertAccountExtension(CbAccountExtension extension)
        {
            return 0;
        }

        @Override
        public int updateAccountExtension(CbAccountExtension extension)
        {
            return 0;
        }

        @Override
        public int deleteAccountExtensionByUserId(Long userId)
        {
            return 0;
        }
    }
}
