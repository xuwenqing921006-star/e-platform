package com.centralbank.eplatform.mapper;

import org.apache.ibatis.annotations.Param;
import com.centralbank.eplatform.domain.CbFinancialProduct;

public interface CbFinancialProductMapper
{
    int countProducts();

    CbFinancialProduct selectProductById(@Param("id") Long id);
}
