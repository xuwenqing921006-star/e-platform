package com.centralbank.eplatform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import com.centralbank.eplatform.domain.CbAttachment;
import com.centralbank.eplatform.mapper.CbAttachmentMapper;

class AttachmentStorageServiceTest
{
    @TempDir
    Path storageRoot;

    @Test
    void uploadsAllowedAttachmentToConfiguredStorageRoot() throws Exception
    {
        FakeAttachmentMapper mapper = new FakeAttachmentMapper();
        AttachmentStorageService service = new AttachmentStorageService(mapper, storageRoot);

        var uploaded = service.upload(new MockMultipartFile(
                "file",
                "县域征信服务网点信息表.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "xlsx-content".getBytes()));

        CbAttachment persisted = mapper.selectAttachmentById(uploaded.id());
        assertThat(uploaded.fileName()).isEqualTo("县域征信服务网点信息表.xlsx");
        assertThat(uploaded.fileType()).isEqualTo("EXCEL");
        assertThat(uploaded.fileSize()).isEqualTo(12L);
        assertThat(uploaded.downloadUrl()).isEqualTo("/api/public/attachments/" + uploaded.id() + "/download");
        assertThat(persisted.getContentId()).isNull();
        assertThat(Files.readString(storageRoot.resolve(persisted.getStoragePath()))).isEqualTo("xlsx-content");
    }

    @Test
    void rejectsUnsupportedAndOversizedAttachments()
    {
        AttachmentStorageService service = new AttachmentStorageService(new FakeAttachmentMapper(), storageRoot);

        assertThatThrownBy(() -> service.upload(new MockMultipartFile("file", "readme.txt", "text/plain",
                "txt".getBytes())))
                .isInstanceOf(AttachmentException.class)
                .hasMessage("仅支持 PDF、Word 或 Excel 文件")
                .extracting("statusCode")
                .isEqualTo(400);

        assertThatThrownBy(() -> service.upload(new MockMultipartFile("file", "伪装附件.pdf", "text/plain",
                "fake-pdf".getBytes())))
                .isInstanceOf(AttachmentException.class)
                .hasMessage("仅支持 PDF、Word 或 Excel 文件")
                .extracting("statusCode")
                .isEqualTo(400);

        assertThatThrownBy(() -> service.upload(new MockMultipartFile("file", "big.pdf", "application/pdf",
                new byte[20 * 1024 * 1024 + 1])))
                .isInstanceOf(AttachmentException.class)
                .hasMessage("单个附件不能超过 20MB")
                .extracting("statusCode")
                .isEqualTo(413);
    }

    @Test
    void validatesContentAttachmentLimit()
    {
        AttachmentStorageService service = new AttachmentStorageService(new FakeAttachmentMapper(), storageRoot);

        assertThatThrownBy(() -> service.validateContentAttachmentLimit(List.of(1L, 2L, 3L, 4L)))
                .isInstanceOf(AttachmentException.class)
                .hasMessage("每篇内容最多上传 3 个附件")
                .extracting("statusCode")
                .isEqualTo(400);
    }

    @Test
    void downloadsAndDeletesExistingAttachment() throws Exception
    {
        FakeAttachmentMapper mapper = new FakeAttachmentMapper();
        AttachmentStorageService service = new AttachmentStorageService(mapper, storageRoot);
        var uploaded = service.upload(new MockMultipartFile("file", "政策解读.pdf", "application/pdf",
                "pdf-content".getBytes()));

        var downloadFile = service.downloadFile(uploaded.id()).orElseThrow();
        assertThat(Files.readString(downloadFile.path())).isEqualTo("pdf-content");

        assertThat(service.delete(uploaded.id())).isTrue();
        assertThat(mapper.selectAttachmentById(uploaded.id())).isNull();
        assertThat(Files.exists(downloadFile.path())).isFalse();
    }

    private static class FakeAttachmentMapper implements CbAttachmentMapper
    {
        private final List<CbAttachment> attachments = new ArrayList<>();

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
            return attachments.removeIf(attachment -> Objects.equals(attachment.getId(), id)) ? 1 : 0;
        }
    }
}
