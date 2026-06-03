package com.centralbank.eplatform.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.centralbank.eplatform.domain.CbAttachment;
import com.centralbank.eplatform.dto.PublicAttachment;
import com.centralbank.eplatform.mapper.CbAttachmentMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;

@Service
public class AttachmentStorageService
{
    public record DownloadFile(CbAttachment attachment, Path path)
    {
    }

    private static final long MAX_FILE_SIZE = 20L * 1024L * 1024L;
    private static final int MAX_CONTENT_ATTACHMENTS = 3;
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final Set<String> PDF_MIME_TYPES = Set.of("application/pdf");
    private static final Set<String> WORD_MIME_TYPES = Set.of(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    private static final Set<String> EXCEL_MIME_TYPES = Set.of(
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final CbAttachmentMapper attachmentMapper;
    private final Path configuredStorageRoot;

    public AttachmentStorageService(CbAttachmentMapper attachmentMapper)
    {
        this(attachmentMapper, null);
    }

    AttachmentStorageService(CbAttachmentMapper attachmentMapper, Path configuredStorageRoot)
    {
        this.attachmentMapper = attachmentMapper;
        this.configuredStorageRoot = configuredStorageRoot;
    }

    public PublicAttachment upload(MultipartFile file) throws IOException
    {
        validateUploadFile(file);
        String originalFileName = Path.of(file.getOriginalFilename()).getFileName().toString();
        String fileType = fileTypeOf(originalFileName).orElseThrow(
                () -> new AttachmentException(400, "仅支持 PDF、Word 或 Excel 文件"));
        Long id = nextAttachmentId();
        String extension = FilenameUtils.getExtension(originalFileName).toLowerCase(Locale.ROOT);
        String storagePath = "attachments/" + DATE_PATH_FORMATTER.format(LocalDateTime.now())
                + "/" + id + "-" + IdUtils.fastSimpleUUID() + "." + extension;
        Path target = resolveStoragePath(storagePath);

        Files.createDirectories(target.getParent());
        file.transferTo(target);

        CbAttachment attachment = new CbAttachment();
        attachment.setId(id);
        attachment.setContentId(null);
        attachment.setFileName(originalFileName);
        attachment.setFileType(fileType);
        attachment.setFileSize(file.getSize());
        attachment.setStoragePath(storagePath);
        attachment.setCreatedAt(LocalDateTime.now());
        attachmentMapper.insertAttachment(attachment);

        return toPublicAttachment(attachment);
    }

    public Optional<DownloadFile> downloadFile(Long id)
    {
        if (id == null || id <= 0)
        {
            return Optional.empty();
        }
        CbAttachment attachment = attachmentMapper.selectAttachmentById(id);
        if (attachment == null)
        {
            return Optional.empty();
        }

        Path path = resolveStoragePath(attachment.getStoragePath());
        if (!Files.isRegularFile(path))
        {
            return Optional.empty();
        }

        return Optional.of(new DownloadFile(attachment, path));
    }

    public boolean delete(Long id) throws IOException
    {
        if (id == null || id <= 0)
        {
            return false;
        }
        CbAttachment attachment = attachmentMapper.selectAttachmentById(id);
        if (attachment == null)
        {
            return false;
        }

        attachmentMapper.deleteAttachmentById(id);
        Files.deleteIfExists(resolveStoragePath(attachment.getStoragePath()));
        return true;
    }

    public void validateContentAttachmentLimit(List<Long> attachmentIds)
    {
        if (attachmentIds != null && attachmentIds.size() > MAX_CONTENT_ATTACHMENTS)
        {
            throw new AttachmentException(400, "每篇内容最多上传 3 个附件");
        }
    }

    private void validateUploadFile(MultipartFile file)
    {
        if (file == null || file.isEmpty() || !StringUtils.isNotBlank(file.getOriginalFilename()))
        {
            throw new AttachmentException(400, "请选择需要上传的附件");
        }
        if (file.getSize() > MAX_FILE_SIZE)
        {
            throw new AttachmentException(413, "单个附件不能超过 20MB");
        }
        String fileType = fileTypeOf(file.getOriginalFilename()).orElseThrow(
                () -> new AttachmentException(400, "仅支持 PDF、Word 或 Excel 文件"));
        if (!isAllowedMimeType(fileType, file.getContentType()))
        {
            throw new AttachmentException(400, "仅支持 PDF、Word 或 Excel 文件");
        }
    }

    private Optional<String> fileTypeOf(String fileName)
    {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase(Locale.ROOT);
        return switch (extension)
        {
            case "pdf" -> Optional.of("PDF");
            case "doc", "docx" -> Optional.of("WORD");
            case "xls", "xlsx" -> Optional.of("EXCEL");
            default -> Optional.empty();
        };
    }

    private boolean isAllowedMimeType(String fileType, String contentType)
    {
        if (!StringUtils.isNotBlank(contentType))
        {
            return false;
        }
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        return switch (fileType)
        {
            case "PDF" -> PDF_MIME_TYPES.contains(normalizedContentType);
            case "WORD" -> WORD_MIME_TYPES.contains(normalizedContentType);
            case "EXCEL" -> EXCEL_MIME_TYPES.contains(normalizedContentType);
            default -> false;
        };
    }

    private PublicAttachment toPublicAttachment(CbAttachment attachment)
    {
        return new PublicAttachment(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFileType(),
                attachment.getFileSize(),
                "/api/public/attachments/" + attachment.getId() + "/download");
    }

    private Long nextAttachmentId()
    {
        return System.currentTimeMillis() * 1000L + ThreadLocalRandom.current().nextInt(1000);
    }

    private Path resolveStoragePath(String storagePath)
    {
        Path root = storageRoot();
        Path resolved = root.resolve(storagePath).normalize();
        if (!resolved.startsWith(root))
        {
            throw new AttachmentException(400, "附件路径不合法");
        }
        return resolved;
    }

    private Path storageRoot()
    {
        Path root = configuredStorageRoot != null
                ? configuredStorageRoot
                : Path.of(RuoYiConfig.getUploadPath());
        return root.toAbsolutePath().normalize();
    }
}
