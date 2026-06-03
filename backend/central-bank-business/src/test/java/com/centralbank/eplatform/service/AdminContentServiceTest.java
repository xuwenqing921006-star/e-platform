package com.centralbank.eplatform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.centralbank.eplatform.domain.CbAccountExtension;
import com.centralbank.eplatform.domain.CbAttachment;
import com.centralbank.eplatform.domain.CbContent;
import com.centralbank.eplatform.dto.AdminContentRequest;
import com.centralbank.eplatform.mapper.CbAccountExtensionMapper;
import com.centralbank.eplatform.mapper.CbAttachmentMapper;
import com.centralbank.eplatform.mapper.CbContentMapper;

class AdminContentServiceTest
{
    @TempDir
    Path storageRoot;

    @Test
    void adminListsContentsWithoutAttachmentCountAndFiltersByOffice()
    {
        Fixture fixture = fixture(1L);

        var page = fixture.service.list(null, null, "CREDIT_REPORT", null, null, 1, 20);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).officeCode()).isEqualTo("CREDIT_REPORT");
        assertThat(page.items().get(0).publishedAt()).isEqualTo("2026-05-30T09:30:00+08:00");
    }

    @Test
    void officeUserCanOnlySeeOwnOffice()
    {
        Fixture fixture = fixture(10L);

        var own = fixture.service.list(null, null, null, null, null, 1, 20);
        assertThat(own.total()).isEqualTo(1);
        assertThat(own.items().get(0).officeCode()).isEqualTo("CREDIT_REPORT");

        assertThatThrownBy(() -> fixture.service.list(null, null, "MONETARY_CREDIT", null, null, 1, 20))
                .isInstanceOf(AdminContentException.class)
                .hasMessage("无权查看该办公室内容")
                .extracting("statusCode")
                .isEqualTo(403);
    }

    @Test
    void createBindsAttachmentsAndSanitizesRichText()
    {
        Fixture fixture = fixture(10L);
        AdminContentRequest request = new AdminContentRequest(
                "征信服务办理指南",
                "SERVICE_GUIDE",
                "CREDIT_REPORT",
                "<h2 onclick=\"bad()\">办理</h2><script>alert(1)</script><a href=\"javascript:alert(1)\">链接</a>",
                List.of(9005L));

        var created = fixture.service.create(request);
        CbContent content = fixture.contentMapper.selectContentById(created.id());

        assertThat(content.getOfficeCode()).isEqualTo("CREDIT_REPORT");
        assertThat(content.getScope()).isEqualTo("FINANCIAL");
        assertThat(content.getRichTextHtml()).doesNotContain("script", "onclick", "javascript");
        assertThat(fixture.attachmentMapper.selectAttachmentById(9005L).getContentId()).isEqualTo(created.id());
    }

    @Test
    void countyOfficeCanOnlyPublishServiceGuide()
    {
        Fixture fixture = fixture(20L);
        AdminContentRequest request = new AdminContentRequest(
                "县域政策宣传",
                "POLICY_PROMOTION",
                "ZHAOZHOU",
                "<p>正文</p>",
                List.of());

        assertThatThrownBy(() -> fixture.service.create(request))
                .isInstanceOf(AdminContentException.class)
                .hasMessage("县域账号只能发布服务指引")
                .extracting("statusCode")
                .isEqualTo(403);
    }

    @Test
    void updateRejectsCrossOfficeModification()
    {
        Fixture fixture = fixture(10L);
        AdminContentRequest request = new AdminContentRequest(
                "改别人内容",
                "SERVICE_GUIDE",
                "MONETARY_CREDIT",
                "<p>正文</p>",
                List.of());

        assertThatThrownBy(() -> fixture.service.update(103L, request))
                .isInstanceOf(AdminContentException.class)
                .hasMessage("无权修改该内容")
                .extracting("statusCode")
                .isEqualTo(403);
    }

    private Fixture fixture(Long userId)
    {
        FakeContentMapper contentMapper = new FakeContentMapper(List.of(
                content(102L, "大庆市征信代理查询网点地址及电话", "SERVICE_GUIDE", "FINANCIAL", null,
                        "CREDIT_REPORT", "征信管理科", LocalDateTime.of(2026, 5, 30, 9, 30)),
                content(103L, "大庆市金融支持小微企业政策提示", "POLICY_PROMOTION", "FINANCIAL", null,
                        "MONETARY_CREDIT", "货币信贷政策管理科", LocalDateTime.of(2026, 5, 31, 9, 30))));
        FakeAttachmentMapper attachmentMapper = new FakeAttachmentMapper(List.of(attachment(9005L, null)));
        FakeAccountExtensionMapper accountMapper = new FakeAccountExtensionMapper(List.of(
                account(1L, "ADMIN", null, null),
                account(10L, "OFFICE", "CREDIT_REPORT", "征信管理科"),
                account(20L, "COUNTY", "ZHAOZHOU", "肇州县")));
        AttachmentStorageService storageService = new AttachmentStorageService(attachmentMapper, storageRoot);
        AdminContentService service = new AdminContentService(contentMapper, attachmentMapper, accountMapper,
                storageService, new FixedOptionsService(), () -> userId);
        return new Fixture(service, contentMapper, attachmentMapper);
    }

    private record Fixture(AdminContentService service, FakeContentMapper contentMapper,
            FakeAttachmentMapper attachmentMapper)
    {
    }

    private static CbContent content(Long id, String title, String category, String scope, String countyCode,
            String officeCode, String officeName, LocalDateTime publishedAt)
    {
        CbContent content = new CbContent();
        content.setId(id);
        content.setTitle(title);
        content.setCategory(category);
        content.setScope(scope);
        content.setCountyCode(countyCode);
        content.setOfficeCode(officeCode);
        content.setOfficeName(officeName);
        content.setRichTextHtml("<p>正文</p>");
        content.setPublishedAt(publishedAt);
        content.setCreatedAt(publishedAt);
        content.setUpdatedAt(publishedAt);
        return content;
    }

    private static CbAttachment attachment(Long id, Long contentId)
    {
        CbAttachment attachment = new CbAttachment();
        attachment.setId(id);
        attachment.setContentId(contentId);
        attachment.setFileName("附件.pdf");
        attachment.setFileType("PDF");
        attachment.setFileSize(10L);
        attachment.setStoragePath("attachments/" + id + ".pdf");
        attachment.setCreatedAt(LocalDateTime.now());
        return attachment;
    }

    private static CbAccountExtension account(Long userId, String role, String officeCode, String officeName)
    {
        CbAccountExtension account = new CbAccountExtension();
        account.setUserId(userId);
        account.setRole(role);
        account.setOfficeCode(officeCode);
        account.setOfficeName(officeName);
        account.setEnabled(true);
        return account;
    }

    private static class FakeContentMapper implements CbContentMapper
    {
        private final List<CbContent> contents = new ArrayList<>();

        FakeContentMapper(List<CbContent> contents)
        {
            this.contents.addAll(contents);
        }

        @Override
        public int countContents()
        {
            return contents.size();
        }

        @Override
        public int countPublicContents(String category, String scope, String countyCode)
        {
            throw new UnsupportedOperationException("not needed");
        }

        @Override
        public int countAdminContents(String keyword, String category, String officeCode, LocalDateTime publishedFrom,
                LocalDateTime publishedTo)
        {
            return (int) adminFiltered(keyword, category, officeCode, publishedFrom, publishedTo).count();
        }

        @Override
        public int insertContent(CbContent content)
        {
            contents.add(content);
            return 1;
        }

        @Override
        public int updateContent(CbContent content)
        {
            return 1;
        }

        @Override
        public int deleteContentById(Long id)
        {
            return contents.removeIf(content -> Objects.equals(content.getId(), id)) ? 1 : 0;
        }

        @Override
        public CbContent selectContentById(Long id)
        {
            return contents.stream().filter(content -> Objects.equals(content.getId(), id)).findFirst().orElse(null);
        }

        @Override
        public List<CbContent> selectAdminContents(String keyword, String category, String officeCode,
                LocalDateTime publishedFrom, LocalDateTime publishedTo, int offset, int pageSize)
        {
            return adminFiltered(keyword, category, officeCode, publishedFrom, publishedTo)
                    .sorted(Comparator.comparing(CbContent::getPublishedAt).reversed())
                    .skip(offset)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public List<CbContent> selectPublicContents(String category, String scope, String countyCode, int offset,
                int pageSize)
        {
            throw new UnsupportedOperationException("not needed");
        }

        @Override
        public List<CbContent> selectRecentContents(int limit)
        {
            return contents.stream().limit(limit).toList();
        }

        private java.util.stream.Stream<CbContent> adminFiltered(String keyword, String category, String officeCode,
                LocalDateTime publishedFrom, LocalDateTime publishedTo)
        {
            return contents.stream()
                    .filter(content -> keyword == null || content.getTitle().contains(keyword))
                    .filter(content -> category == null || Objects.equals(content.getCategory(), category))
                    .filter(content -> officeCode == null || Objects.equals(content.getOfficeCode(), officeCode))
                    .filter(content -> publishedFrom == null || !content.getPublishedAt().isBefore(publishedFrom))
                    .filter(content -> publishedTo == null || content.getPublishedAt().isBefore(publishedTo));
        }
    }

    private static class FakeAttachmentMapper implements CbAttachmentMapper
    {
        private final List<CbAttachment> attachments = new ArrayList<>();

        FakeAttachmentMapper(List<CbAttachment> attachments)
        {
            this.attachments.addAll(attachments);
        }

        @Override
        public int countAttachments()
        {
            return attachments.size();
        }

        @Override
        public int insertAttachment(CbAttachment attachment)
        {
            attachments.add(attachment);
            return 1;
        }

        @Override
        public CbAttachment selectAttachmentById(Long id)
        {
            return attachments.stream().filter(attachment -> Objects.equals(attachment.getId(), id)).findFirst()
                    .orElse(null);
        }

        @Override
        public List<CbAttachment> selectAttachmentsByContentId(Long contentId)
        {
            return attachments.stream().filter(attachment -> Objects.equals(attachment.getContentId(), contentId))
                    .toList();
        }

        @Override
        public int updateAttachmentContentId(Long id, Long contentId)
        {
            CbAttachment attachment = selectAttachmentById(id);
            if (attachment == null)
            {
                return 0;
            }
            attachment.setContentId(contentId);
            return 1;
        }

        @Override
        public int clearAttachmentsByContentId(Long contentId)
        {
            attachments.stream().filter(attachment -> Objects.equals(attachment.getContentId(), contentId))
                    .forEach(attachment -> attachment.setContentId(null));
            return 1;
        }

        @Override
        public int deleteAttachmentById(Long id)
        {
            return attachments.removeIf(attachment -> Objects.equals(attachment.getId(), id)) ? 1 : 0;
        }
    }

    private record FakeAccountExtensionMapper(List<CbAccountExtension> accounts) implements CbAccountExtensionMapper
    {
        @Override
        public int countAccountExtensions()
        {
            return accounts.size();
        }

        @Override
        public CbAccountExtension selectByUserId(Long userId)
        {
            return accounts.stream().filter(account -> Objects.equals(account.getUserId(), userId)).findFirst()
                    .orElse(null);
        }
    }
}
