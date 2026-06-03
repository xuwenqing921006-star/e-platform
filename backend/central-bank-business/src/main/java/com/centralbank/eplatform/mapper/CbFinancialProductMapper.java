package com.centralbank.eplatform.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.centralbank.eplatform.domain.CbFinancialProduct;

public interface CbFinancialProductMapper
{
    int countProducts();

    List<CbFinancialProduct> selectPublicProducts(@Param("offset") int offset, @Param("pageSize") int pageSize);

    CbFinancialProduct selectProductById(@Param("id") Long id);
}
