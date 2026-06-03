package com.centralbank.eplatform.service;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.centralbank.eplatform.domain.CbAttachment;
import com.centralbank.eplatform.domain.CbContent;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.dto.PublicAttachment;
import com.centralbank.eplatform.dto.PublicContentDetailData;
import com.centralbank.eplatform.dto.PublicContentListItem;
import com.centralbank.eplatform.mapper.CbAttachmentMapper;
import com.centralbank.eplatform.mapper.CbContentMapper;

@Service
public class PublicContentService
{
    private static final ZoneOffset CHINA_OFFSET = ZoneOffset.ofHours(8);

    private final CbContentMapper contentMapper;
    private final CbAttachmentMapper attachmentMapper;

    public PublicContentService(CbContentMapper contentMapper, CbAttachmentMapper attachmentMapper)
    {
        this.contentMapper = contentMapper;
        this.attachmentMapper = attachmentMapper;
    }

    public PaginatedData<PublicContentListItem> list(String category, String scope, String countyCode, int page,
            int pageSize)
    {
        validateListParams(category, scope, countyCode, page, pageSize);
        int offset = (page - 1) * pageSize;
        int total = contentMapper.countPublicContents(category, scope, countyCode);
        List<PublicContentListItem> items = contentMapper
                .selectPublicContents(category, scope, countyCode, offset, pageSize)
                .stream()
                .map(this::toListItem)
                .toList();

        return new PaginatedData<>(items, total, page, pageSize);
    }

    public Optional<PublicContentDetailData> detail(Long id)
    {
        if (id == null || id <= 0)
        {
            return Optional.empty();
        }

        CbContent content = contentMapper.selectContentById(id);
        if (content == null)
        {
            return Optional.empty();
        }

        List<PublicAttachment> attachments = attachmentMapper.selectAttachmentsByContentId(id)
                .stream()
                .limit(3)
                .map(this::toAttachment)
                .toList();

        return Optional.of(new PublicContentDetailData(
                content.getId(),
                content.getTitle(),
                content.getCategory(),
                content.getOfficeName(),
                formatPublishedAt(content),
                content.getRichTextHtml(),
                attachments));
    }

    private void validateListParams(String category, String scope, String countyCode, int page, int pageSize)
    {
        if (!"SERVICE_GUIDE".equals(category) && !"POLICY_PROMOTION".equals(category))
        {
            throw new IllegalArgumentException("内容分类不合法");
        }
        if (!"FINANCIAL".equals(scope) && !"RURAL".equals(scope))
        {
            throw new IllegalArgumentException("内容范围不合法");
        }
        if ("RURAL".equals(scope) && (countyCode == null || countyCode.isBlank()))
        {
            throw new IllegalArgumentException("乡村振兴服务指引必须指定县域");
        }
        if (page < 1 || pageSize < 1 || pageSize > 50)
        {
            throw new IllegalArgumentException("分页参数不合法");
        }
    }

    private PublicContentListItem toListItem(CbContent content)
    {
        return new PublicContentListItem(
                content.getId(),
                content.getTitle(),
                content.getCategory(),
                content.getOfficeName(),
                formatPublishedAt(content));
    }

    private PublicAttachment toAttachment(CbAttachment attachment)
    {
        return new PublicAttachment(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFileType(),
                attachment.getFileSize(),
                "/api/public/attachments/" + attachment.getId() + "/download");
    }

    private String formatPublishedAt(CbContent content)
    {
        return content.getPublishedAt().atOffset(CHINA_OFFSET).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
