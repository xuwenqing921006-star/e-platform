package com.centralbank.eplatform.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.centralbank.eplatform.domain.CbContent;
import com.centralbank.eplatform.dto.AdminDashboardRecentContent;
import com.centralbank.eplatform.dto.AdminDashboardSummaryData;
import com.centralbank.eplatform.mapper.CbAdminAccountMapper;
import com.centralbank.eplatform.mapper.CbAuditLogMapper;
import com.centralbank.eplatform.mapper.CbContentMapper;
import com.centralbank.eplatform.mapper.CbFinancialProductMapper;

@Service
public class AdminDashboardService
{
    private static final ZoneOffset CHINA_OFFSET = ZoneOffset.ofHours(8);

    private final CbContentMapper contentMapper;
    private final CbFinancialProductMapper productMapper;
    private final CbAdminAccountMapper accountMapper;
    private final CbAuditLogMapper auditLogMapper;
    private final Clock clock;

    @Autowired
    public AdminDashboardService(CbContentMapper contentMapper, CbFinancialProductMapper productMapper,
            CbAdminAccountMapper accountMapper, CbAuditLogMapper auditLogMapper)
    {
        this(contentMapper, productMapper, accountMapper, auditLogMapper, Clock.system(CHINA_OFFSET));
    }

    public AdminDashboardService(CbContentMapper contentMapper, CbFinancialProductMapper productMapper,
            CbAdminAccountMapper accountMapper, CbAuditLogMapper auditLogMapper, Clock clock)
    {
        this.contentMapper = contentMapper;
        this.productMapper = productMapper;
        this.accountMapper = accountMapper;
        this.auditLogMapper = auditLogMapper;
        this.clock = clock;
    }

    public AdminDashboardSummaryData summary()
    {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.atTime(23, 59, 59);
        var recentContents = contentMapper.selectRecentContents(3)
                .stream()
                .map(this::toRecentContent)
                .toList();
        return new AdminDashboardSummaryData(
                contentMapper.countContents(),
                productMapper.countProducts(),
                accountMapper.countAdminAccounts(null, null, null),
                auditLogMapper.countAuditLogs(null, null, from, to),
                recentContents);
    }

    private AdminDashboardRecentContent toRecentContent(CbContent content)
    {
        return new AdminDashboardRecentContent(content.getId(), content.getTitle(), content.getCategory(),
                format(content.getPublishedAt()));
    }

    private String format(LocalDateTime time)
    {
        return time == null ? null : time.atOffset(CHINA_OFFSET).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
