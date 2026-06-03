package com.centralbank.eplatform.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.centralbank.eplatform.domain.CbContent;

public interface CbContentMapper
{
    int countContents();

    CbContent selectContentById(@Param("id") Long id);

    List<CbContent> selectRecentContents(@Param("limit") int limit);
}
