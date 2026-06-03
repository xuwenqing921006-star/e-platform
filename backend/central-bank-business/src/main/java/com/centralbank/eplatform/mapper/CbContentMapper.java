package com.centralbank.eplatform.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.centralbank.eplatform.domain.CbContent;

public interface CbContentMapper
{
    int countContents();

    int countPublicContents(@Param("category") String category, @Param("scope") String scope,
            @Param("countyCode") String countyCode);

    CbContent selectContentById(@Param("id") Long id);

    List<CbContent> selectPublicContents(@Param("category") String category, @Param("scope") String scope,
            @Param("countyCode") String countyCode, @Param("offset") int offset, @Param("pageSize") int pageSize);

    List<CbContent> selectRecentContents(@Param("limit") int limit);
}
