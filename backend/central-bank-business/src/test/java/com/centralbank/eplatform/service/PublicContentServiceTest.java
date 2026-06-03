package com.centralbank.eplatform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import com.centralbank.eplatform.controller.publicapi.PublicContentController;
import com.centralbank.eplatform.domain.CbAttachment;
import com.centralbank.eplatform.domain.CbContent;
import com.centralbank.eplatform.mapper.CbAttachmentMapper;
import com.centralbank.eplatform.mapper.CbContentMapper;

class PublicContentServiceTest
{
    @Test
    void listsPublicContentsWithContractPagination()
    {
        PublicContentService service = new PublicContentService(
                new FakeContentMapper(List.of(
                        content(101L, "县域服务指南", "SERVICE_GUIDE", "RURAL", "ZHAOZHOU", "肇州县",
                                LocalDateTime.of(2026, 5, 29, 9, 30)),
                        content(104L, "涉农金融服务办理提示", "SERVICE_GUIDE", "RURAL", "ZHAOZHOU", "肇州县",
                                LocalDateTime.of(2026, 6, 1, 9, 30)),
                        content(103L, "金融支持小微企业政策提示", "POLICY_PROMOTION", "FINANCIAL", null,
                                "货币信贷政策管理科", LocalDateTime.of(2026, 5, 31, 9, 30)))),
                new FakeAttachmentMapper(List.of()));

        var data = service.list("SERVICE_GUIDE", "RURAL", "ZHAOZHOU", 1, 1);

        assertThat(data.total()).isEqualTo(2);
        assertThat(data.page()).isEqualTo(1);
        assertThat(data.pageSize()).isEqualTo(1);
        assertThat(data.items()).hasSize(1);
        assertThat(data.items().get(0).id()).isEqualTo(104L);
        assertThat(data.items().get(0).officeName()).isEqualTo("肇州县");
        assertThat(data.items().get(0).publishedAt()).isEqualTo("2026-06-01T09:30:00+08:00");
    }

    @Test
    void rejectsRuralContentListWithoutCountyCode()
    {
        PublicContentService service = new PublicContentService(new FakeContentMapper(List.of()),
                new FakeAttachmentMapper(List.of()));

        assertThatThrownBy(() -> service.list("SERVICE_GUIDE", "RURAL", null, 1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("乡村振兴服务指引必须指定县域");
    }

    @Test
    void returnsDetailWithAtMostThreePublicAttachments()
    {
        PublicContentService service = new PublicContentService(
                new FakeContentMapper(List.of(content(101L, "县域服务指南", "SERVICE_GUIDE", "RURAL", "ZHAOZHOU",
                        "肇州县", LocalDateTime.of(2026, 5, 29, 9, 30)))),
                new FakeAttachmentMapper(List.of(
                        attachment(9001L, 101L, "网点信息表.xlsx", "EXCEL"),
                        attachment(9002L, 101L, "查询指引.pdf", "PDF"),
                        attachment(9003L, 101L, "材料清单.docx", "WORD"),
                        attachment(9004L, 101L, "补充说明.pdf", "PDF"))));

        var detail = service.detail(101L).orElseThrow();

        assertThat(detail.title()).isEqualTo("县域服务指南");
        assertThat(detail.richTextHtml()).contains("办理说明");
        assertThat(detail.attachments()).hasSize(3);
        assertThat(detail.attachments().get(0).downloadUrl()).isEqualTo("/api/public/attachments/9001/download");
        assertThat(detail.attachments().get(2).fileType()).isEqualTo("WORD");
    }

    @Test
    void controllerReturnsContracted404ForMissingContent()
    {
        PublicContentController controller = new PublicContentController(
                new PublicContentService(new FakeContentMapper(List.of()), new FakeAttachmentMapper(List.of())));

        var response = controller.detail(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("内容不存在");
        assertThat(response.getBody().data()).isNull();
    }

    private static CbContent content(Long id, String title, String category, String scope, String countyCode,
            String officeName, LocalDateTime publishedAt)
    {
        CbContent content = new CbContent();
        content.setId(id);
        content.setTitle(title);
        content.setCategory(category);
        content.setScope(scope);
        content.setCountyCode(countyCode);
        content.setOfficeCode(officeName);
        content.setOfficeName(officeName);
        content.setRichTextHtml("<h2>办理说明</h2><p>测试正文</p>");
        content.setPublishedAt(publishedAt);
        return content;
    }

    private static CbAttachment attachment(Long id, Long contentId, String fileName, String fileType)
    {
        CbAttachment attachment = new CbAttachment();
        attachment.setId(id);
        attachment.setContentId(contentId);
        attachment.setFileName(fileName);
        attachment.setFileType(fileType);
        attachment.setFileSize(20480L);
        attachment.setStoragePath("seed/attachments/" + id);
        return attachment;
    }

    private record FakeContentMapper(List<CbContent> contents) implements CbContentMapper
    {
        @Override
        public int countContents()
        {
            return contents.size();
        }

        @Override
        public int countPublicContents(String category, String scope, String countyCode)
        {
            return (int) filtered(category, scope, countyCode).count();
        }

        @Override
        public CbContent selectContentById(Long id)
        {
            return contents.stream().filter(content -> Objects.equals(content.getId(), id)).findFirst().orElse(null);
        }

        @Override
        public List<CbContent> selectPublicContents(String category, String scope, String countyCode, int offset,
                int pageSize)
        {
            return filtered(category, scope, countyCode)
                    .sorted(Comparator.comparing(CbContent::getPublishedAt).reversed()
                            .thenComparing(CbContent::getId, Comparator.reverseOrder()))
                    .skip(offset)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public List<CbContent> selectRecentContents(int limit)
        {
            return contents.stream().limit(limit).toList();
        }

        private java.util.stream.Stream<CbContent> filtered(String category, String scope, String countyCode)
        {
            return contents.stream()
                    .filter(content -> Objects.equals(content.getCategory(), category))
                    .filter(content -> Objects.equals(content.getScope(), scope))
                    .filter(content -> countyCode == null || Objects.equals(content.getCountyCode(), countyCode));
        }
    }

    private record FakeAttachmentMapper(List<CbAttachment> attachments) implements CbAttachmentMapper
    {
        @Override
        public int countAttachments()
        {
            return attachments.size();
        }

        @Override
        public int insertAttachment(CbAttachment attachment)
        {
            throw new UnsupportedOperationException("not needed in this test");
        }

        @Override
        public CbAttachment selectAttachmentById(Long id)
        {
            return attachments.stream()
                    .filter(attachment -> Objects.equals(attachment.getId(), id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<CbAttachment> selectAttachmentsByContentId(Long contentId)
        {
            return attachments.stream()
                    .filter(attachment -> Objects.equals(attachment.getContentId(), contentId))
                    .toList();
        }

        @Override
        public int deleteAttachmentById(Long id)
        {
            throw new UnsupportedOperationException("not needed in this test");
        }
    }
}
