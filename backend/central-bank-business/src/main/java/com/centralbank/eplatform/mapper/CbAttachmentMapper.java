package com.centralbank.eplatform.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.centralbank.eplatform.domain.CbAttachment;

public interface CbAttachmentMapper
{
    int countAttachments();

    int insertAttachment(CbAttachment attachment);

    CbAttachment selectAttachmentById(@Param("id") Long id);

    List<CbAttachment> selectAttachmentsByContentId(@Param("contentId") Long contentId);

    int deleteAttachmentById(@Param("id") Long id);
}
