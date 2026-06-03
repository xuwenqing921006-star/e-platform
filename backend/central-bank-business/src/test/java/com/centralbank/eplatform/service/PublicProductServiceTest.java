package com.centralbank.eplatform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import com.centralbank.eplatform.controller.publicapi.PublicProductController;
import com.centralbank.eplatform.domain.CbFinancialProduct;
import com.centralbank.eplatform.mapper.CbFinancialProductMapper;

class PublicProductServiceTest
{
    @Test
    void listsPublicProductsWithContractPagination()
    {
        PublicProductService service = new PublicProductService(new FakeProductMapper(List.of(
                product(2003L, "建设银行", "裕农快贷", "AGRICULTURAL"),
                product(2001L, "农业银行", "惠农e贷", "AGRICULTURAL"),
                product(2002L, "大庆农商银行", "小微企业流动资金贷款", "SMALL_MICRO"))));

        var data = service.list(1, 2);

        assertThat(data.total()).isEqualTo(3);
        assertThat(data.page()).isEqualTo(1);
        assertThat(data.pageSize()).isEqualTo(2);
        assertThat(data.items()).hasSize(2);
        assertThat(data.items().get(0).id()).isEqualTo(2001L);
        assertThat(data.items().get(0).bankName()).isEqualTo("农业银行");
        assertThat(data.items().get(0).productName()).isEqualTo("惠农e贷");
        assertThat(data.items().get(0).productType()).isEqualTo("AGRICULTURAL");
    }

    @Test
    void rejectsInvalidPagination()
    {
        PublicProductService service = new PublicProductService(new FakeProductMapper(List.of()));

        assertThatThrownBy(() -> service.list(1, 51))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("分页参数不合法");
    }

    @Test
    void returnsDetailWithoutInternalFields()
    {
        PublicProductService service = new PublicProductService(new FakeProductMapper(List.of(
                product(2001L, "农业银行", "惠农e贷", "AGRICULTURAL"))));

        var detail = service.detail(2001L).orElseThrow();

        assertThat(detail.id()).isEqualTo(2001L);
        assertThat(detail.bankName()).isEqualTo("农业银行");
        assertThat(detail.productName()).isEqualTo("惠农e贷");
        assertThat(detail.productType()).isEqualTo("AGRICULTURAL");
        assertThat(detail.admissionConditions()).isEqualTo("面向涉农经营主体。");
        assertThat(detail.productIntro()).isEqualTo("用于验证 7 字段产品数据层。");
        assertThat(detail.businessManager()).isEqualTo("张经理");
        assertThat(detail.contactInfo()).isEqualTo("0459-0002001");
    }

    @Test
    void controllerReturnsContracted404ForMissingProduct()
    {
        PublicProductController controller = new PublicProductController(
                new PublicProductService(new FakeProductMapper(List.of())));

        var response = controller.detail(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("产品不存在");
        assertThat(response.getBody().data()).isNull();
    }

    private static CbFinancialProduct product(Long id, String bankName, String productName, String productType)
    {
        CbFinancialProduct product = new CbFinancialProduct();
        product.setId(id);
        product.setBankCode("BANK_" + id);
        product.setBankName(bankName);
        product.setProductName(productName);
        product.setProductType(productType);
        product.setAdmissionConditions("面向涉农经营主体。");
        product.setProductIntro("用于验证 7 字段产品数据层。");
        product.setBusinessManager("张经理");
        product.setContactInfo("0459-0002001");
        product.setCreatedAt(LocalDateTime.of(2026, 5, 30, 9, 30));
        product.setUpdatedAt(LocalDateTime.of(2026, 5, 30, 9, 30));
        return product;
    }

    private record FakeProductMapper(List<CbFinancialProduct> products) implements CbFinancialProductMapper
    {
        @Override
        public int countProducts()
        {
            return products.size();
        }

        @Override
        public List<CbFinancialProduct> selectPublicProducts(int offset, int pageSize)
        {
            return products.stream()
                    .sorted(Comparator.comparing(CbFinancialProduct::getId))
                    .skip(offset)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public CbFinancialProduct selectProductById(Long id)
        {
            return products.stream().filter(product -> Objects.equals(product.getId(), id)).findFirst().orElse(null);
        }
    }
}
