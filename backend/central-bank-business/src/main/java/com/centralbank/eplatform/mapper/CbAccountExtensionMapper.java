package com.centralbank.eplatform.mapper;

import org.apache.ibatis.annotations.Param;
import com.centralbank.eplatform.domain.CbAccountExtension;

public interface CbAccountExtensionMapper
{
    int countAccountExtensions();

    CbAccountExtension selectByUserId(@Param("userId") Long userId);
}
