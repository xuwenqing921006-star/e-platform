package com.centralbank.eplatform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import com.centralbank.eplatform.domain.CbAccountExtension;
import com.centralbank.eplatform.domain.CbAdminAccount;
import com.centralbank.eplatform.dto.AdminAccountRequest;
import com.centralbank.eplatform.mapper.CbAccountExtensionMapper;
import com.centralbank.eplatform.mapper.CbAdminAccountMapper;
import org.junit.jupiter.api.Test;

class AdminAccountServiceTest
{
    @Test
    void adminListsAccountsWithOfficeAndRoleFilters()
    {
        Fixture fixture = fixture(1L);

        var page = fixture.service.list("zx", "CREDIT_REPORT", "OFFICE_USER", 1, 20);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).username()).isEqualTo("zxglk");
        assertThat(page.items().get(0).displayName()).isEqualTo("张伟");
        assertThat(page.items().get(0).role()).isEqualTo("OFFICE_USER");
        assertThat(page.items().get(0).officeName()).isEqualTo("征信管理科");
        assertThat(page.items().get(0).enabled()).isTrue();
    }

    @Test
    void nonAdminCannotManageAccounts()
    {
        Fixture fixture = fixture(10L);

        assertThatThrownBy(() -> fixture.service.list(null, null, null, 1, 20))
                .isInstanceOf(AdminAccountException.class)
                .hasMessage("仅管理员可管理账号")
                .extracting("statusCode")
                .isEqualTo(403);
    }

    @Test
    void createRequiresOfficeForOfficeUserAndStoresExtension()
    {
        Fixture fixture = fixture(1L);

        var created = fixture.service.create(new AdminAccountRequest(
                "newuser", "李娜", "OFFICE_USER", "MONETARY_CREDIT", "Initial123!", null));

        CbAdminAccount account = fixture.accountMapper.selectAdminAccountByUserId(created.id());
        assertThat(account.getUsername()).isEqualTo("newuser");
        assertThat(account.getDisplayName()).isEqualTo("李娜");
        assertThat(account.getRole()).isEqualTo("OFFICE_USER");
        assertThat(account.getOfficeName()).isEqualTo("货币信贷政策管理科");
        assertThat(account.getEnabled()).isTrue();
        assertThat(fixture.accountMapper.selectAdminAccountByUserId(created.id()).getPassword())
                .isNotEqualTo("Initial123!");
    }

    @Test
    void createRejectsDuplicateUsername()
    {
        Fixture fixture = fixture(1L);

        assertThatThrownBy(() -> fixture.service.create(new AdminAccountRequest(
                "zxglk", "重复账号", "OFFICE_USER", "CREDIT_REPORT", "Initial123!", true)))
                .isInstanceOf(AdminAccountException.class)
                .hasMessage("登录账号已存在")
                .extracting("statusCode")
                .isEqualTo(409);
    }

    @Test
    void officeUserMustBindOffice()
    {
        Fixture fixture = fixture(1L);

        assertThatThrownBy(() -> fixture.service.create(new AdminAccountRequest(
                "nooffice", "未绑机构", "OFFICE_USER", null, "Initial123!", true)))
                .isInstanceOf(AdminAccountException.class)
                .hasMessage("普通账号必须绑定一个办公室")
                .extracting("statusCode")
                .isEqualTo(400);
    }

    @Test
    void updateAndResetPasswordUseContractFields()
    {
        Fixture fixture = fixture(1L);

        var updated = fixture.service.update(10L, new AdminAccountRequest(
                null, "张伟二", "OFFICE_USER", "MONETARY_CREDIT", null, false));
        var reset = fixture.service.resetPassword(10L, "ResetPassword1!");

        CbAdminAccount account = fixture.accountMapper.selectAdminAccountByUserId(10L);
        assertThat(updated.updated()).isTrue();
        assertThat(reset.reset()).isTrue();
        assertThat(account.getDisplayName()).isEqualTo("张伟二");
        assertThat(account.getOfficeCode()).isEqualTo("MONETARY_CREDIT");
        assertThat(account.getEnabled()).isFalse();
        assertThat(fixture.accountMapper.selectAdminAccountByUserId(10L).getPassword()).isNotEqualTo("ResetPassword1!");
    }

    @Test
    void deleteRejectsCurrentUser()
    {
        Fixture fixture = fixture(1L);

        assertThatThrownBy(() -> fixture.service.delete(1L))
                .isInstanceOf(AdminAccountException.class)
                .hasMessage("不能删除当前登录账号")
                .extracting("statusCode")
                .isEqualTo(409);
    }

    private Fixture fixture(Long operatorId)
    {
        List<CbAdminAccount> users = new ArrayList<>(List.of(
                user(1L, "admin", "系统管理员", "0", "encoded-admin"),
                user(10L, "zxglk", "张伟", "0", "encoded-zx"),
                user(11L, "fkgl", "赵敏", "1", "encoded-fk")));
        List<CbAccountExtension> extensions = new ArrayList<>(List.of(
                extension(1L, "ADMIN", null, null, true),
                extension(10L, "OFFICE_USER", "CREDIT_REPORT", "征信管理科", true),
                extension(11L, "OFFICE_USER", "FOREIGN_EXCHANGE", "外汇管理科", false)));
        FakeAdminAccountMapper accountMapper = new FakeAdminAccountMapper(users, extensions);
        FakeAccountExtensionMapper extensionMapper = new FakeAccountExtensionMapper(extensions);
        AdminAccountService service = new AdminAccountService(accountMapper, extensionMapper, new FixedOptionsService(),
                () -> operatorId);
        return new Fixture(service, accountMapper, extensionMapper);
    }

    private static CbAdminAccount user(Long id, String username, String displayName, String status, String password)
    {
        CbAdminAccount user = new CbAdminAccount();
        user.setId(id);
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setStatus(status);
        user.setPassword(password);
        return user;
    }

    private static CbAccountExtension extension(Long userId, String role, String officeCode, String officeName,
            boolean enabled)
    {
        CbAccountExtension extension = new CbAccountExtension();
        extension.setUserId(userId);
        extension.setRole(role);
        extension.setOfficeCode(officeCode);
        extension.setOfficeName(officeName);
        extension.setEnabled(enabled);
        return extension;
    }

    private record Fixture(AdminAccountService service, FakeAdminAccountMapper accountMapper,
            FakeAccountExtensionMapper extensionMapper)
    {
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
        private final List<CbAdminAccount> users;
        private final List<CbAccountExtension> extensions;

        FakeAdminAccountMapper(List<CbAdminAccount> users, List<CbAccountExtension> extensions)
        {
            this.users = users;
            this.extensions = extensions;
        }

        @Override
        public int countAdminAccounts(String keyword, String officeCode, String role)
        {
            return (int) filtered(keyword, officeCode, role).count();
        }

        @Override
        public List<CbAdminAccount> selectAdminAccounts(String keyword, String officeCode, String role, int offset,
                int pageSize)
        {
            return filtered(keyword, officeCode, role)
                    .sorted(Comparator.comparing(CbAdminAccount::getId))
                    .skip(offset)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public CbAdminAccount selectAdminAccountByUserId(Long userId)
        {
            return joined(userId);
        }

        @Override
        public int insertAccountExtension(CbAccountExtension extension)
        {
            extensions.add(extension);
            return 1;
        }

        @Override
        public int updateAccountExtension(CbAccountExtension extension)
        {
            CbAccountExtension existing = selectExtension(extension.getUserId());
            if (existing == null)
            {
                return 0;
            }
            existing.setRole(extension.getRole());
            existing.setOfficeCode(extension.getOfficeCode());
            existing.setOfficeName(extension.getOfficeName());
            existing.setEnabled(extension.getEnabled());
            return 1;
        }

        @Override
        public int deleteAccountExtensionByUserId(Long userId)
        {
            return extensions.removeIf(item -> Objects.equals(item.getUserId(), userId)) ? 1 : 0;
        }

        private java.util.stream.Stream<CbAdminAccount> filtered(String keyword, String officeCode, String role)
        {
            return extensions.stream()
                    .map(extension -> joined(extension.getUserId()))
                    .filter(Objects::nonNull)
                    .filter(account -> keyword == null || account.getUsername().contains(keyword)
                            || account.getDisplayName().contains(keyword))
                    .filter(account -> officeCode == null || Objects.equals(account.getOfficeCode(), officeCode))
                    .filter(account -> role == null || Objects.equals(account.getRole(), role));
        }

        private CbAdminAccount joined(Long userId)
        {
            CbAdminAccount user = selectUser(userId);
            CbAccountExtension extension = selectExtension(userId);
            if (user == null || extension == null || "2".equals(user.getStatus()))
            {
                return null;
            }
            CbAdminAccount account = new CbAdminAccount();
            account.setId(user.getId());
            account.setUsername(user.getUsername());
            account.setDisplayName(user.getDisplayName());
            account.setPassword(user.getPassword());
            account.setRole(extension.getRole());
            account.setOfficeCode(extension.getOfficeCode());
            account.setOfficeName(extension.getOfficeName());
            account.setStatus(user.getStatus());
            account.setEnabled("0".equals(user.getStatus()) && Boolean.TRUE.equals(extension.getEnabled()));
            return account;
        }

        private CbAdminAccount selectUser(Long userId)
        {
            return users.stream().filter(user -> Objects.equals(user.getId(), userId)).findFirst().orElse(null);
        }

        private CbAccountExtension selectExtension(Long userId)
        {
            return extensions.stream().filter(item -> Objects.equals(item.getUserId(), userId)).findFirst()
                    .orElse(null);
        }

        @Override
        public Long selectUserIdByUsername(String username)
        {
            return users.stream()
                    .filter(user -> Objects.equals(user.getUsername(), username) && !"2".equals(user.getStatus()))
                    .map(CbAdminAccount::getId)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public int insertSysUser(CbAdminAccount user)
        {
            user.setId(users.stream().map(CbAdminAccount::getId).max(Long::compareTo).orElse(0L) + 1L);
            users.add(user);
            return 1;
        }

        @Override
        public int updateSysUser(CbAdminAccount user)
        {
            CbAdminAccount existing = selectUser(user.getId());
            if (existing == null)
            {
                return 0;
            }
            existing.setDisplayName(user.getDisplayName());
            existing.setStatus(user.getStatus());
            return 1;
        }

        @Override
        public int resetPassword(Long userId, String password)
        {
            CbAdminAccount existing = selectUser(userId);
            if (existing == null)
            {
                return 0;
            }
            existing.setPassword(password);
            return 1;
        }

        @Override
        public int deleteSysUserById(Long userId)
        {
            CbAdminAccount existing = selectUser(userId);
            if (existing == null)
            {
                return 0;
            }
            existing.setStatus("2");
            return 1;
        }
    }
}
