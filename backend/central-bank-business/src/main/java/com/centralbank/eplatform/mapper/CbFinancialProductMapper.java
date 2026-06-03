package com.centralbank.eplatform.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.centralbank.eplatform.domain.CbFinancialProduct;

public interface CbFinancialProductMapper
{
    int countProducts();

    List<CbFinancialProduct> selectPublicProducts(@Param("offset") int offset, @Param("pageSize") int pageSize);

    CbFinancialProduct selectProductById(@Param("id") Long id);

    int countAdminProducts(@Param("keyword") String keyword, @Param("bankCode") String bankCode,
            @Param("productType") String productType);

    List<CbFinancialProduct> selectAdminProducts(@Param("keyword") String keyword, @Param("bankCode") String bankCode,
            @Param("productType") String productType, @Param("offset") int offset, @Param("pageSize") int pageSize);

    int insertProduct(CbFinancialProduct product);

    int updateProduct(CbFinancialProduct product);

    int deleteProductById(@Param("id") Long id);
}
