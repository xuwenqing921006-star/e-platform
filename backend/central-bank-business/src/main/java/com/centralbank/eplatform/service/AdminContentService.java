package com.centralbank.eplatform.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.centralbank.eplatform.domain.CbAccountExtension;
import com.centralbank.eplatform.domain.CbAttachment;
import com.centralbank.eplatform.domain.CbContent;
import com.centralbank.eplatform.dto.AdminContentCreateData;
import com.centralbank.eplatform.dto.AdminContentDeleteData;
import com.centralbank.eplatform.dto.AdminContentDetailData;
import com.centralbank.eplatform.dto.AdminContentListItem;
import com.centralbank.eplatform.dto.AdminContentRequest;
import com.centralbank.eplatform.dto.AdminContentUpdateData;
import com.centralbank.eplatform.dto.OptionItem;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.dto.PublicAttachment;
import com.centralbank.eplatform.mapper.CbAccountExtensionMapper;
import com.centralbank.eplatform.mapper.CbAttachmentMapper;
import com.centralbank.eplatform.mapper.CbContentMapper;

@Service
public class AdminContentService
{
    private record Operator(Long userId, String role, String officeCode, String officeName, boolean admin)
    {
    }

    private record OfficeMeta(String officeCode, String officeName, String scope, String countyCode)
    {
    }

    private static final ZoneOffset CHINA_OFFSET = ZoneOffset.ofHours(8);
    private static final Pattern SCRIPT_TAG = Pattern.compile("(?is)<script[^>]*>.*?</script>");
    private static final Pattern EVENT_HANDLER = Pattern.compile("(?i)\\s+on[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)");
    private static final Pattern JAVASCRIPT_URL = Pattern.compile("(?i)javascript\\s*:");

    private final CbContentMapper contentMapper;
    private final CbAttachmentMapper attachmentMapper;
    private final CbAccountExtensionMapper accountExtensionMapper;
    private final AttachmentStorageService attachmentStorageService;
    private final FixedOptionsService fixedOptionsService;
    private final AdminOperatorContext operatorContext;
    private final AuditLogRecorder auditLogRecorder;

    public AdminContentService(CbContentMapper contentMapper, CbAttachmentMapper attachmentMapper,
            CbAccountExtensionMapper accountExtensionMapper, AttachmentStorageService attachmentStorageService,
            FixedOptionsService fixedOptionsService, AdminOperatorContext operatorContext)
    {
        this(contentMapper, attachmentMapper, accountExtensionMapper, attachmentStorageService, fixedOptionsService,
                operatorContext, AuditLogRecorder.noop());
    }

    @Autowired
    public AdminContentService(CbContentMapper contentMapper, CbAttachmentMapper attachmentMapper,
            CbAccountExtensionMapper accountExtensionMapper, AttachmentStorageService attachmentStorageService,
            FixedOptionsService fixedOptionsService, AdminOperatorContext operatorContext,
            AuditLogRecorder auditLogRecorder)
    {
        this.contentMapper = contentMapper;
        this.attachmentMapper = attachmentMapper;
        this.accountExtensionMapper = accountExtensionMapper;
        this.attachmentStorageService = attachmentStorageService;
        this.fixedOptionsService = fixedOptionsService;
        this.operatorContext = operatorContext;
        this.auditLogRecorder = auditLogRecorder;
    }

    public PaginatedData<AdminContentListItem> list(String keyword, String category, String officeCode,
            String publishedFrom, String publishedTo, int page, int pageSize)
    {
        validatePage(page, pageSize);
        validateCategoryIfPresent(category);
        Operator operator = currentOperator();
        String effectiveOfficeCode = effectiveOfficeFilter(operator, officeCode);
        LocalDateTime from = parseDateStart(publishedFrom);
        LocalDateTime to = parseDateEndExclusive(publishedTo);
        int offset = (page - 1) * pageSize;
        int total = contentMapper.countAdminContents(normalizeBlank(keyword), normalizeBlank(category),
                effectiveOfficeCode, from, to);
        List<AdminContentListItem> items = contentMapper.selectAdminContents(normalizeBlank(keyword),
                        normalizeBlank(category), effectiveOfficeCode, from, to, offset, pageSize)
                .stream()
                .map(this::toListItem)
                .toList();
        return new PaginatedData<>(items, total, page, pageSize);
    }

    public AdminContentCreateData create(AdminContentRequest request)
    {
        validateRequest(request);
        Operator operator = currentOperator();
        OfficeMeta office = validateWritableOffice(operator, request.officeCode(), "无权创建该办公室内容");
        attachmentStorageService.validateContentAttachmentLimit(request.attachmentIds());
        validateAttachmentIds(request.attachmentIds());

        LocalDateTime now = LocalDateTime.now();
        CbContent content = new CbContent();
        content.setId(nextContentId());
        content.setTitle(request.title().trim());
        content.setCategory(categoryForOffice(office, request.category()));
        content.setScope(office.scope());
        content.setCountyCode(office.countyCode());
        content.setOfficeCode(office.officeCode());
        content.setOfficeName(office.officeName());
        content.setRichTextHtml(sanitizeRichText(request.richTextHtml()));
        content.setPublishedAt(now);
        content.setCreatedAt(now);
        content.setUpdatedAt(now);
        contentMapper.insertContent(content);
        bindAttachments(content.getId(), request.attachmentIds());
        auditLogRecorder.record("CREATE", "CONTENT", content.getTitle(), "新增政策宣传内容");
        return new AdminContentCreateData(content.getId(), format(content.getPublishedAt()));
    }

    public Optional<AdminContentDetailData> detail(Long id)
    {
        CbContent content = selectExisting(id);
        if (content == null)
        {
            return Optional.empty();
        }
        assertCanRead(currentOperator(), content);
        return Optional.of(toDetailData(content));
    }

    public AdminContentUpdateData update(Long id, AdminContentRequest request)
    {
        validateRequest(request);
        CbContent existing = selectExisting(id);
        if (existing == null)
        {
            throw new AdminContentException(404, "内容不存在");
        }
        Operator operator = currentOperator();
        assertCanEdit(operator, existing);
        OfficeMeta office = validateWritableOffice(operator, request.officeCode(), "无权修改该内容");
        attachmentStorageService.validateContentAttachmentLimit(request.attachmentIds());
        validateAttachmentIds(request.attachmentIds());

        existing.setTitle(request.title().trim());
        existing.setCategory(categoryForOffice(office, request.category()));
        existing.setScope(office.scope());
        existing.setCountyCode(office.countyCode());
        existing.setOfficeCode(office.officeCode());
        existing.setOfficeName(office.officeName());
        existing.setRichTextHtml(sanitizeRichText(request.richTextHtml()));
        existing.setUpdatedAt(LocalDateTime.now());
        contentMapper.updateContent(existing);
        attachmentMapper.clearAttachmentsByContentId(existing.getId());
        bindAttachments(existing.getId(), request.attachmentIds());
        auditLogRecorder.record("UPDATE", "CONTENT", existing.getTitle(), "更新附件与正文说明");
        return new AdminContentUpdateData(existing.getId(), true);
    }

    public AdminContentDeleteData delete(Long id) throws IOException
    {
        CbContent existing = selectExisting(id);
        if (existing == null)
        {
            throw new AdminContentException(404, "内容不存在");
        }
        assertCanEdit(currentOperator(), existing);
        for (CbAttachment attachment : attachmentMapper.selectAttachmentsByContentId(existing.getId()))
        {
            attachmentStorageService.delete(attachment.getId());
        }
        contentMapper.deleteContentById(existing.getId());
        auditLogRecorder.record("DELETE", "CONTENT", existing.getTitle(), "删除已失效展示内容");
        return new AdminContentDeleteData(true);
    }

    private CbContent selectExisting(Long id)
    {
        if (id == null || id <= 0)
        {
            return null;
        }
        return contentMapper.selectContentById(id);
    }

    private Operator currentOperator()
    {
        Long userId = operatorContext.currentUserId();
        CbAccountExtension extension = accountExtensionMapper.selectByUserId(userId);
        boolean admin = userId != null && userId == 1L || extension != null && "ADMIN".equals(extension.getRole());
        if (admin)
        {
            return new Operator(userId, "ADMIN", null, null, true);
        }
        if (extension == null || !Boolean.TRUE.equals(extension.getEnabled()) || isBlank(extension.getOfficeCode()))
        {
            throw new AdminContentException(403, "普通账号必须绑定一个办公室");
        }
        return new Operator(userId, extension.getRole(), extension.getOfficeCode(), extension.getOfficeName(), false);
    }

    private String effectiveOfficeFilter(Operator operator, String requestedOfficeCode)
    {
        String normalized = normalizeBlank(requestedOfficeCode);
        if (operator.admin())
        {
            return normalized;
        }
        if (normalized != null && !normalized.equals(operator.officeCode()))
        {
            throw new AdminContentException(403, "无权查看该办公室内容");
        }
        return operator.officeCode();
    }

    private OfficeMeta validateWritableOffice(Operator operator, String officeCode, String deniedMessage)
    {
        String normalizedOffice = normalizeBlank(officeCode);
        OptionItem office = fixedOptionsService.findOffice(normalizedOffice)
                .orElseThrow(() -> new AdminContentException(400, "发布办公室不合法"));
        if (!operator.admin() && !office.value().equals(operator.officeCode()))
        {
            throw new AdminContentException(403, deniedMessage);
        }
        String countyCode = office.countyCode();
        String scope = countyCode == null ? "FINANCIAL" : "RURAL";
        return new OfficeMeta(office.value(), office.label(), scope, countyCode);
    }

    private void assertCanRead(Operator operator, CbContent content)
    {
        if (!operator.admin() && !operator.officeCode().equals(content.getOfficeCode()))
        {
            throw new AdminContentException(403, "无权查看该办公室内容");
        }
    }

    private void assertCanEdit(Operator operator, CbContent content)
    {
        if (!operator.admin() && !operator.officeCode().equals(content.getOfficeCode()))
        {
            throw new AdminContentException(403, "无权修改该内容");
        }
    }

    private void validateRequest(AdminContentRequest request)
    {
        if (request == null)
        {
            throw new AdminContentException(400, "请求体不能为空");
        }
        if (isBlank(request.title()))
        {
            throw new AdminContentException(400, "标题不能为空");
        }
        validateCategory(request.category());
        if (isBlank(request.officeCode()))
        {
            throw new AdminContentException(400, "发布办公室不能为空");
        }
        if (isBlank(request.richTextHtml()))
        {
            throw new AdminContentException(400, "正文不能为空");
        }
    }

    private String categoryForOffice(OfficeMeta office, String category)
    {
        return office.countyCode() == null ? category : "SERVICE_GUIDE";
    }

    private void validateCategoryIfPresent(String category)
    {
        if (!isBlank(category))
        {
            validateCategory(category);
        }
    }

    private void validateCategory(String category)
    {
        if (!"SERVICE_GUIDE".equals(category) && !"POLICY_PROMOTION".equals(category))
        {
            throw new AdminContentException(400, "内容分类不合法");
        }
    }

    private void validatePage(int page, int pageSize)
    {
        if (page < 1 || pageSize < 1 || pageSize > 100)
        {
            throw new AdminContentException(400, "分页参数不合法");
        }
    }

    private void validateAttachmentIds(List<Long> attachmentIds)
    {
        if (attachmentIds == null)
        {
            return;
        }
        for (Long attachmentId : attachmentIds)
        {
            if (attachmentId == null || attachmentMapper.selectAttachmentById(attachmentId) == null)
            {
                throw new AdminContentException(400, "附件不存在");
            }
        }
    }

    private void bindAttachments(Long contentId, List<Long> attachmentIds)
    {
        if (attachmentIds == null)
        {
            return;
        }
        attachmentIds.forEach(attachmentId -> attachmentMapper.updateAttachmentContentId(attachmentId, contentId));
    }

    private AdminContentListItem toListItem(CbContent content)
    {
        return new AdminContentListItem(
                content.getId(),
                content.getTitle(),
                content.getCategory(),
                content.getOfficeCode(),
                content.getOfficeName(),
                format(content.getPublishedAt()));
    }

    private AdminContentDetailData toDetailData(CbContent content)
    {
        return new AdminContentDetailData(
                content.getId(),
                content.getTitle(),
                content.getCategory(),
                content.getOfficeCode(),
                content.getOfficeName(),
                content.getRichTextHtml(),
                format(content.getPublishedAt()),
                attachmentMapper.selectAttachmentsByContentId(content.getId()).stream()
                        .limit(3)
                        .map(this::toPublicAttachment)
                        .toList());
    }

    private PublicAttachment toPublicAttachment(CbAttachment attachment)
    {
        return new PublicAttachment(attachment.getId(), attachment.getFileName(), attachment.getFileType(),
                attachment.getFileSize(), "/api/public/attachments/" + attachment.getId() + "/download");
    }

    private String sanitizeRichText(String html)
    {
        return JAVASCRIPT_URL.matcher(EVENT_HANDLER.matcher(SCRIPT_TAG.matcher(html).replaceAll(""))
                .replaceAll(""))
                .replaceAll("");
    }

    private LocalDateTime parseDateStart(String value)
    {
        return isBlank(value) ? null : LocalDate.parse(value).atStartOfDay();
    }

    private LocalDateTime parseDateEndExclusive(String value)
    {
        return isBlank(value) ? null : LocalDate.parse(value).plusDays(1).atStartOfDay();
    }

    private String format(LocalDateTime time)
    {
        return time.atOffset(CHINA_OFFSET).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private Long nextContentId()
    {
        return System.currentTimeMillis() * 1000L + ThreadLocalRandom.current().nextInt(1000);
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
