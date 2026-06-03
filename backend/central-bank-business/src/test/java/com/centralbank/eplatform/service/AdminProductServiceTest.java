package com.centralbank.eplatform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import com.centralbank.eplatform.domain.CbAccountExtension;
import com.centralbank.eplatform.domain.CbFinancialProduct;
import com.centralbank.eplatform.dto.AdminProductRequest;
import com.centralbank.eplatform.mapper.CbAccountExtensionMapper;
import com.centralbank.eplatform.mapper.CbFinancialProductMapper;

class AdminProductServiceTest
{
    @Test
    void productManagerListsProductsWithoutReferenceRate()
    {
        Fixture fixture = fixture(10L);

        var page = fixture.service.list("惠农", "ABC", "AGRICULTURAL", 1, 20);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).productName()).isEqualTo("惠农e贷");
        assertThat(page.items().get(0).bankCode()).isEqualTo("ABC");
        assertThat(page.items().get(0).updatedAt()).isEqualTo("2026-05-30T09:30:00+08:00");
    }

    @Test
    void nonProductOfficeCannotManageProducts()
    {
        Fixture fixture = fixture(20L);

        assertThatThrownBy(() -> fixture.service.list(null, null, null, 1, 20))
                .isInstanceOf(AdminProductException.class)
                .hasMessage("无权管理金融产品")
                .extracting("statusCode")
                .isEqualTo(403);
    }

    @Test
    void createValidatesFixedBankAndStoresSevenFields()
    {
        Fixture fixture = fixture(10L);
        AdminProductRequest request = request("ABC", "惠农e贷二期", "AGRICULTURAL");

        var created = fixture.service.create(request);
        CbFinancialProduct product = fixture.productMapper.selectProductById(created.id());

        assertThat(product.getBankName()).isEqualTo("农业银行");
        assertThat(product.getProductName()).isEqualTo("惠农e贷二期");
        assertThat(product.getProductType()).isEqualTo("AGRICULTURAL");
        assertThat(product.getAdmissionConditions()).isEqualTo("面向涉农经营主体。");
        assertThat(product.getProductIntro()).isEqualTo("用于验证 7 字段产品数据层。");
        assertThat(product.getBusinessManager()).isEqualTo("张经理");
        assertThat(product.getContactInfo()).isEqualTo("0459-0002001");
    }

    @Test
    void createRejectsUnknownBank()
    {
        Fixture fixture = fixture(10L);

        assertThatThrownBy(() -> fixture.service.create(request("UNKNOWN", "未知产品", "AGRICULTURAL")))
                .isInstanceOf(AdminProductException.class)
                .hasMessage("银行机构不在固定列表中")
                .extracting("statusCode")
                .isEqualTo(400);
    }

    @Test
    void updateAndDeleteRequireExistingProduct()
    {
        Fixture fixture = fixture(10L);

        assertThatThrownBy(() -> fixture.service.update(999L, request("ABC", "不存在", "AGRICULTURAL")))
                .isInstanceOf(AdminProductException.class)
                .hasMessage("产品不存在")
                .extracting("statusCode")
                .isEqualTo(404);

        assertThatThrownBy(() -> fixture.service.delete(999L))
                .isInstanceOf(AdminProductException.class)
                .hasMessage("产品不存在")
                .extracting("statusCode")
                .isEqualTo(404);
    }

    @Test
    void importTemplateContainsContractHeaders() throws IOException
    {
        Fixture fixture = fixture(10L);

        byte[] bytes = fixture.service.importTemplate();

        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes)))
        {
            var sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("银行机构");
            assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("产品名称");
            assertThat(sheet.getRow(0).getCell(2).getStringCellValue()).isEqualTo("类型");
            assertThat(sheet.getRow(0).getCell(3).getStringCellValue()).isEqualTo("准入条件");
            assertThat(sheet.getRow(0).getCell(4).getStringCellValue()).isEqualTo("产品介绍");
            assertThat(sheet.getRow(0).getCell(5).getStringCellValue()).isEqualTo("业务经办人");
            assertThat(sheet.getRow(0).getCell(6).getStringCellValue()).isEqualTo("联系方式");
        }
    }

    @Test
    void validateImportReturnsCountsErrorsAndKeepsSunshineAgricultureRow() throws IOException
    {
        Fixture fixture = fixture(10L);

        var validated = fixture.service.validateImport(workbookBytes(List.of(
                List.of("农业银行", "惠农e贷三期", "涉农产品", "面向涉农经营主体。", "用于农业生产经营。", "张经理", "0459-0002001"),
                List.of("阳光惠农贷", "阳光惠农贷", "涉农产品", "阳光惠农贷准入。", "阳光惠农贷介绍。", "王经理", "0459-0002002"),
                List.of("不存在银行", "未知产品", "涉农产品", "准入。", "介绍。", "李经理", "0459-0002003"),
                List.of("建设银行", "缺少联系方式", "小微产品", "准入。", "介绍。", "赵经理", ""))));

        assertThat(validated.importToken()).startsWith("import-");
        assertThat(validated.totalCount()).isEqualTo(4);
        assertThat(validated.validCount()).isEqualTo(2);
        assertThat(validated.invalidCount()).isEqualTo(2);
        assertThat(validated.errors()).extracting("rowNumber").containsExactly(4, 5);
        assertThat(validated.errors()).extracting("field").containsExactly("银行机构", "联系方式");
        assertThat(validated.errors()).extracting("rawValue").containsExactly("不存在银行", "");
    }

    @Test
    void commitImportWritesValidRowsOnceAndRejectsExpiredToken() throws IOException
    {
        Fixture fixture = fixture(10L);
        var validated = fixture.service.validateImport(workbookBytes(List.of(
                List.of("农业银行", "惠农e贷三期", "涉农产品", "面向涉农经营主体。", "用于农业生产经营。", "张经理", "0459-0002001"),
                List.of("不存在银行", "未知产品", "涉农产品", "准入。", "介绍。", "李经理", "0459-0002003"))));

        var committed = fixture.service.commitImport(validated.importToken());

        assertThat(committed.importedCount()).isEqualTo(1);
        assertThat(committed.skippedCount()).isEqualTo(1);
        assertThat(fixture.productMapper.adminFiltered(null, null, null))
                .extracting(CbFinancialProduct::getProductName)
                .contains("惠农e贷三期")
                .doesNotContain("未知产品");
        assertThatThrownBy(() -> fixture.service.commitImport(validated.importToken()))
                .isInstanceOf(AdminProductException.class)
                .hasMessage("导入任务已提交或已失效")
                .extracting("statusCode")
                .isEqualTo(409);
    }

    private Fixture fixture(Long userId)
    {
        FakeProductMapper productMapper = new FakeProductMapper(List.of(
                product(2001L, "ABC", "农业银行", "惠农e贷", "AGRICULTURAL"),
                product(2002L, "CCB", "建设银行", "裕农快贷", "AGRICULTURAL"),
                product(2003L, "ICBC", "中国工商银行", "小微经营贷", "SMALL_MICRO")));
        FakeAccountExtensionMapper accountMapper = new FakeAccountExtensionMapper(List.of(
                account(1L, "ADMIN", null, null),
                account(10L, "OFFICE", "MONETARY_CREDIT", "货币信贷政策管理科"),
                account(20L, "OFFICE", "CREDIT_REPORT", "征信管理科")));
        AdminProductService service = new AdminProductService(productMapper, accountMapper, new FixedOptionsService(),
                () -> userId);
        return new Fixture(service, productMapper);
    }

    private static AdminProductRequest request(String bankCode, String productName, String productType)
    {
        return new AdminProductRequest(bankCode, productName, productType, "面向涉农经营主体。",
                "用于验证 7 字段产品数据层。", "张经理", "0459-0002001");
    }

    private static byte[] workbookBytes(List<List<String>> dataRows) throws IOException
    {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            var sheet = workbook.createSheet("金融产品导入");
            var header = sheet.createRow(0);
            List<String> headers = List.of("银行机构", "产品名称", "类型", "准入条件", "产品介绍", "业务经办人", "联系方式");
            for (int index = 0; index < headers.size(); index++)
            {
                header.createCell(index).setCellValue(headers.get(index));
            }
            for (int rowIndex = 0; rowIndex < dataRows.size(); rowIndex++)
            {
                var row = sheet.createRow(rowIndex + 1);
                List<String> values = dataRows.get(rowIndex);
                for (int cellIndex = 0; cellIndex < values.size(); cellIndex++)
                {
                    row.createCell(cellIndex).setCellValue(values.get(cellIndex));
                }
            }
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private static CbFinancialProduct product(Long id, String bankCode, String bankName, String productName,
            String productType)
    {
        CbFinancialProduct product = new CbFinancialProduct();
        product.setId(id);
        product.setBankCode(bankCode);
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

    private static CbAccountExtension account(Long userId, String role, String officeCode, String officeName)
    {
        CbAccountExtension account = new CbAccountExtension();
        account.setUserId(userId);
        account.setRole(role);
        account.setOfficeCode(officeCode);
        account.setOfficeName(officeName);
        account.setEnabled(true);
        return account;
    }

    private record Fixture(AdminProductService service, FakeProductMapper productMapper)
    {
    }

    private static class FakeProductMapper implements CbFinancialProductMapper
    {
        private final List<CbFinancialProduct> products = new ArrayList<>();

        FakeProductMapper(List<CbFinancialProduct> products)
        {
            this.products.addAll(products);
        }

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

        @Override
        public int countAdminProducts(String keyword, String bankCode, String productType)
        {
            return (int) adminFiltered(keyword, bankCode, productType).count();
        }

        @Override
        public List<CbFinancialProduct> selectAdminProducts(String keyword, String bankCode, String productType,
                int offset, int pageSize)
        {
            return adminFiltered(keyword, bankCode, productType)
                    .sorted(Comparator.comparing(CbFinancialProduct::getUpdatedAt).reversed())
                    .skip(offset)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public int insertProduct(CbFinancialProduct product)
        {
            products.add(product);
            return 1;
        }

        @Override
        public int updateProduct(CbFinancialProduct product)
        {
            CbFinancialProduct existing = selectProductById(product.getId());
            if (existing == null)
            {
                return 0;
            }
            existing.setBankCode(product.getBankCode());
            existing.setBankName(product.getBankName());
            existing.setProductName(product.getProductName());
            existing.setProductType(product.getProductType());
            existing.setAdmissionConditions(product.getAdmissionConditions());
            existing.setProductIntro(product.getProductIntro());
            existing.setBusinessManager(product.getBusinessManager());
            existing.setContactInfo(product.getContactInfo());
            existing.setUpdatedAt(product.getUpdatedAt());
            return 1;
        }

        @Override
        public int deleteProductById(Long id)
        {
            return products.removeIf(product -> Objects.equals(product.getId(), id)) ? 1 : 0;
        }

        private java.util.stream.Stream<CbFinancialProduct> adminFiltered(String keyword, String bankCode,
                String productType)
        {
            return products.stream()
                    .filter(product -> keyword == null || product.getProductName().contains(keyword))
                    .filter(product -> bankCode == null || Objects.equals(product.getBankCode(), bankCode))
                    .filter(product -> productType == null || Objects.equals(product.getProductType(), productType));
        }
    }

    private record FakeAccountExtensionMapper(List<CbAccountExtension> accounts) implements CbAccountExtensionMapper
    {
        @Override
        public int countAccountExtensions()
        {
            return accounts.size();
        }

        @Override
        public CbAccountExtension selectByUserId(Long userId)
        {
            return accounts.stream().filter(account -> Objects.equals(account.getUserId(), userId)).findFirst()
                    .orElse(null);
        }
    }
}
