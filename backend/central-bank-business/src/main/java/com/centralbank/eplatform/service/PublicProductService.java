package com.centralbank.eplatform.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.centralbank.eplatform.domain.CbFinancialProduct;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.dto.PublicProductDetailData;
import com.centralbank.eplatform.dto.PublicProductListItem;
import com.centralbank.eplatform.mapper.CbFinancialProductMapper;

@Service
public class PublicProductService
{
    private final CbFinancialProductMapper productMapper;

    public PublicProductService(CbFinancialProductMapper productMapper)
    {
        this.productMapper = productMapper;
    }

    public PaginatedData<PublicProductListItem> list(int page, int pageSize)
    {
        validatePagination(page, pageSize);
        int offset = (page - 1) * pageSize;
        int total = productMapper.countProducts();
        List<PublicProductListItem> items = productMapper.selectPublicProducts(offset, pageSize)
                .stream()
                .map(this::toListItem)
                .toList();

        return new PaginatedData<>(items, total, page, pageSize);
    }

    public Optional<PublicProductDetailData> detail(Long id)
    {
        if (id == null || id <= 0)
        {
            return Optional.empty();
        }

        CbFinancialProduct product = productMapper.selectProductById(id);
        if (product == null)
        {
            return Optional.empty();
        }

        return Optional.of(toDetailData(product));
    }

    private void validatePagination(int page, int pageSize)
    {
        if (page < 1 || pageSize < 1 || pageSize > 50)
        {
            throw new IllegalArgumentException("分页参数不合法");
        }
    }

    private PublicProductListItem toListItem(CbFinancialProduct product)
    {
        return new PublicProductListItem(
                product.getId(),
                product.getBankName(),
                product.getProductName(),
                product.getProductType());
    }

    private PublicProductDetailData toDetailData(CbFinancialProduct product)
    {
        return new PublicProductDetailData(
                product.getId(),
                product.getBankName(),
                product.getProductName(),
                product.getProductType(),
                product.getAdmissionConditions(),
                product.getProductIntro(),
                product.getBusinessManager(),
                product.getContactInfo());
    }
}
