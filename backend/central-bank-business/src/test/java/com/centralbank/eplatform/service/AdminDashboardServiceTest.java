package com.centralbank.eplatform.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import com.centralbank.eplatform.domain.CbContent;
import com.centralbank.eplatform.domain.CbFinancialProduct;
import com.centralbank.eplatform.domain.CbAdminAccount;
import com.centralbank.eplatform.domain.CbAuditLog;
import com.centralbank.eplatform.domain.CbAccountExtension;
import com.centralbank.eplatform.mapper.CbAdminAccountMapper;
import com.centralbank.eplatform.mapper.CbAuditLogMapper;
import com.centralbank.eplatform.mapper.CbContentMapper;
import com.centralbank.eplatform.mapper.CbFinancialProductMapper;
import org.junit.jupiter.api.Test;

class AdminDashboardServiceTest
{
    @Test
    void summaryUsesRealCountsAndRecentContents()
    {
        FakeContentMapper contentMapper = new FakeContentMapper();
        FakeProductMapper productMapper = new FakeProductMapper();
        FakeAccountMapper accountMapper = new FakeAccountMapper();
        FakeAuditMapper auditMapper = new FakeAuditMapper();
        Clock clock = Clock.fixed(Instant.parse("2026-05-30T08:00:00Z"), ZoneId.of("Asia/Shanghai"));
        AdminDashboardService service = new AdminDashboardService(contentMapper, productMapper, accountMapper,
                auditMapper, clock);

        var summary = service.summary();

        assertThat(summary.publishedContentCount()).isEqualTo(48);
        assertThat(summary.productCount()).isEqualTo(112);
        assertThat(summary.accountCount()).isEqualTo(9);
        assertThat(summary.todayOperationCount()).isEqualTo(16);
        assertThat(summary.recentContents()).hasSize(3);
        assertThat(summary.recentContents().get(0).title()).isEqualTo("中国人民银行公告〔2025〕第12号");
        assertThat(summary.recentContents().get(0).category()).isEqualTo("POLICY_PROMOTION");
        assertThat(summary.recentContents().get(0).publishedAt()).isEqualTo("2026-05-30T16:42:00+08:00");
        assertThat(contentMapper.recentLimit).isEqualTo(3);
        assertThat(auditMapper.operatedFrom).isEqualTo(LocalDateTime.of(2026, 5, 30, 0, 0));
        assertThat(auditMapper.operatedTo).isEqualTo(LocalDateTime.of(2026, 5, 30, 23, 59, 59));
    }

    private static CbContent content(Long id, String title, String category, LocalDateTime publishedAt)
    {
        CbContent content = new CbContent();
        content.setId(id);
        content.setTitle(title);
        content.setCategory(category);
        content.setPublishedAt(publishedAt);
        return content;
    }

    private static class FakeContentMapper implements CbContentMapper
    {
        int recentLimit;

        @Override
        public int countContents()
        {
            return 48;
        }

        @Override
        public List<CbContent> selectRecentContents(int limit)
        {
            this.recentLimit = limit;
            return List.of(
                    content(101L, "中国人民银行公告〔2025〕第12号", "POLICY_PROMOTION",
                            LocalDateTime.of(2026, 5, 30, 16, 42)),
                    content(102L, "肇州县金融服务便民联系指南", "RURAL_REVITALIZATION",
                            LocalDateTime.of(2026, 5, 29, 10, 8)),
                    content(103L, "征信代理查询网点地址及电话", "SERVICE_GUIDE",
                            LocalDateTime.of(2026, 5, 28, 11, 20)));
        }

        @Override
        public int countPublicContents(String category, String scope, String countyCode)
        {
            return 0;
        }

        @Override
        public int countAdminContents(String keyword, String category, String officeCode,
                LocalDateTime publishedFrom, LocalDateTime publishedTo)
        {
            return 0;
        }

        @Override
        public int insertContent(CbContent content)
        {
            return 0;
        }

        @Override
        public int updateContent(CbContent content)
        {
            return 0;
        }

        @Override
        public int deleteContentById(Long id)
        {
            return 0;
        }

        @Override
        public CbContent selectContentById(Long id)
        {
            return null;
        }

        @Override
        public List<CbContent> selectAdminContents(String keyword, String category, String officeCode,
                LocalDateTime publishedFrom, LocalDateTime publishedTo, int offset, int pageSize)
        {
            return List.of();
        }

        @Override
        public List<CbContent> selectPublicContents(String category, String scope, String countyCode, int offset,
                int pageSize)
        {
            return List.of();
        }
    }

    private static class FakeProductMapper implements CbFinancialProductMapper
    {
        @Override
        public int countProducts()
        {
            return 112;
        }

        @Override
        public List<CbFinancialProduct> selectPublicProducts(int offset, int pageSize)
        {
            return List.of();
        }

        @Override
        public CbFinancialProduct selectProductById(Long id)
        {
            return null;
        }

        @Override
        public int countAdminProducts(String keyword, String bankCode, String productType)
        {
            return 0;
        }

        @Override
        public List<CbFinancialProduct> selectAdminProducts(String keyword, String bankCode, String productType,
                int offset, int pageSize)
        {
            return List.of();
        }

        @Override
        public int insertProduct(CbFinancialProduct product)
        {
            return 0;
        }

        @Override
        public int updateProduct(CbFinancialProduct product)
        {
            return 0;
        }

        @Override
        public int deleteProductById(Long id)
        {
            return 0;
        }
    }

    private static class FakeAccountMapper implements CbAdminAccountMapper
    {
        @Override
        public int countAdminAccounts(String keyword, String officeCode, String role)
        {
            return 9;
        }

        @Override
        public List<CbAdminAccount> selectAdminAccounts(String keyword, String officeCode, String role, int offset,
                int pageSize)
        {
            return List.of();
        }

        @Override
        public CbAdminAccount selectAdminAccountByUserId(Long userId)
        {
            return null;
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

    private static class FakeAuditMapper implements CbAuditLogMapper
    {
        LocalDateTime operatedFrom;
        LocalDateTime operatedTo;

        @Override
        public int countAuditLogs(String operatorKeyword, Integer businessType, LocalDateTime operatedFrom,
                LocalDateTime operatedTo)
        {
            this.operatedFrom = operatedFrom;
            this.operatedTo = operatedTo;
            return 16;
        }

        @Override
        public List<CbAuditLog> selectAuditLogs(String operatorKeyword, Integer businessType,
                LocalDateTime operatedFrom, LocalDateTime operatedTo, int offset, int pageSize)
        {
            return List.of();
        }

        @Override
        public int insertAuditLog(CbAuditLog auditLog)
        {
            return 0;
        }
    }
}
