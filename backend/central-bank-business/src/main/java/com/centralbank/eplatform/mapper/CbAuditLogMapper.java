package com.centralbank.eplatform.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.centralbank.eplatform.domain.CbAuditLog;

@Mapper
public interface CbAuditLogMapper
{
    int countAuditLogs(@Param("operatorKeyword") String operatorKeyword, @Param("businessType") Integer businessType,
            @Param("operatedFrom") LocalDateTime operatedFrom, @Param("operatedTo") LocalDateTime operatedTo);

    List<CbAuditLog> selectAuditLogs(@Param("operatorKeyword") String operatorKeyword,
            @Param("businessType") Integer businessType, @Param("operatedFrom") LocalDateTime operatedFrom,
            @Param("operatedTo") LocalDateTime operatedTo, @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    int insertAuditLog(CbAuditLog auditLog);
}
