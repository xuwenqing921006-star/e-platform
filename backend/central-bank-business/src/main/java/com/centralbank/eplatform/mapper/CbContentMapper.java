package com.centralbank.eplatform.mapper;

import java.util.List;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;
import com.centralbank.eplatform.domain.CbContent;

public interface CbContentMapper
{
    int countContents();

    int countPublicContents(@Param("category") String category, @Param("scope") String scope,
            @Param("countyCode") String countyCode);

    int countAdminContents(@Param("keyword") String keyword, @Param("category") String category,
            @Param("officeCode") String officeCode, @Param("publishedFrom") LocalDateTime publishedFrom,
            @Param("publishedTo") LocalDateTime publishedTo);

    int insertContent(CbContent content);

    int updateContent(CbContent content);

    int deleteContentById(@Param("id") Long id);

    CbContent selectContentById(@Param("id") Long id);

    List<CbContent> selectAdminContents(@Param("keyword") String keyword, @Param("category") String category,
            @Param("officeCode") String officeCode, @Param("publishedFrom") LocalDateTime publishedFrom,
            @Param("publishedTo") LocalDateTime publishedTo, @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    List<CbContent> selectPublicContents(@Param("category") String category, @Param("scope") String scope,
            @Param("countyCode") String countyCode, @Param("offset") int offset, @Param("pageSize") int pageSize);

    List<CbContent> selectRecentContents(@Param("limit") int limit);
}
