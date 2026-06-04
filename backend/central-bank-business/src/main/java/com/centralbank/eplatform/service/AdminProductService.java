package com.centralbank.eplatform.service;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.centralbank.eplatform.domain.CbAccountExtension;
import com.centralbank.eplatform.domain.CbFinancialProduct;
import com.centralbank.eplatform.dto.AdminProductCreateData;
import com.centralbank.eplatform.dto.AdminProductDeleteData;
import com.centralbank.eplatform.dto.AdminProductDetailData;
import com.centralbank.eplatform.dto.AdminProductImportCommitData;
import com.centralbank.eplatform.dto.AdminProductImportError;
import com.centralbank.eplatform.dto.AdminProductImportValidateData;
import com.centralbank.eplatform.dto.AdminProductListItem;
import com.centralbank.eplatform.dto.AdminProductRequest;
import com.centralbank.eplatform.dto.AdminProductUpdateData;
import com.centralbank.eplatform.dto.OptionItem;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.mapper.CbAccountExtensionMapper;
import com.centralbank.eplatform.mapper.CbFinancialProductMapper;

@Service
public class AdminProductService
{
    private static final ZoneOffset CHINA_OFFSET = ZoneOffset.ofHours(8);
    private static final DateTimeFormatter IMPORT_TOKEN_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String PRODUCT_MANAGER_OFFICE = "MONETARY_CREDIT";
    private static final int MAX_CONTACTS = 5;
    private static final List<String> TEMPLATE_HEADERS = List.of(
            "银行机构", "产品名称", "类型", "准入条件", "产品介绍", "业务经办人", "联系方式");

    private final CbFinancialProductMapper productMapper;
    private final CbAccountExtensionMapper accountExtensionMapper;
    private final FixedOptionsService fixedOptionsService;
    private final AdminOperatorContext operatorContext;
    private final AuditLogRecorder auditLogRecorder;
    private final Map<String, ImportSession> importSessions = new ConcurrentHashMap<>();

    public AdminProductService(CbFinancialProductMapper productMapper, CbAccountExtensionMapper accountExtensionMapper,
            FixedOptionsService fixedOptionsService, AdminOperatorContext operatorContext)
    {
        this(productMapper, accountExtensionMapper, fixedOptionsService, operatorContext, AuditLogRecorder.noop());
    }

    @Autowired
    public AdminProductService(CbFinancialProductMapper productMapper, CbAccountExtensionMapper accountExtensionMapper,
            FixedOptionsService fixedOptionsService, AdminOperatorContext operatorContext,
            AuditLogRecorder auditLogRecorder)
    {
        this.productMapper = productMapper;
        this.accountExtensionMapper = accountExtensionMapper;
        this.fixedOptionsService = fixedOptionsService;
        this.operatorContext = operatorContext;
        this.auditLogRecorder = auditLogRecorder;
    }

    public PaginatedData<AdminProductListItem> list(String keyword, String bankCode, String productType, int page,
            int pageSize)
    {
        assertCanManageProducts("无权管理金融产品");
        validatePage(page, pageSize);
        validateBankFilter(bankCode);
        validateProductTypeFilter(productType);
        int offset = (page - 1) * pageSize;
        String normalizedKeyword = normalizeBlank(keyword);
        String normalizedBankCode = normalizeBlank(bankCode);
        String normalizedProductType = normalizeBlank(productType);
        int total = productMapper.countAdminProducts(normalizedKeyword, normalizedBankCode, normalizedProductType);
        List<AdminProductListItem> items = productMapper
                .selectAdminProducts(normalizedKeyword, normalizedBankCode, normalizedProductType, offset, pageSize)
                .stream()
                .map(this::toListItem)
                .toList();
        return new PaginatedData<>(items, total, page, pageSize);
    }

    public AdminProductCreateData create(AdminProductRequest request)
    {
        assertCanManageProducts("无权管理金融产品");
        CbFinancialProduct product = buildProduct(request);
        LocalDateTime now = LocalDateTime.now();
        product.setId(nextProductId());
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        productMapper.insertProduct(product);
        auditLogRecorder.record("CREATE", "FINANCIAL_PRODUCT", product.getProductName(), "新增金融产品");
        return new AdminProductCreateData(product.getId());
    }

    public Optional<AdminProductDetailData> detail(Long id)
    {
        assertCanManageProducts("无权管理金融产品");
        CbFinancialProduct product = selectExisting(id);
        return product == null ? Optional.empty() : Optional.of(toDetailData(product));
    }

    public AdminProductUpdateData update(Long id, AdminProductRequest request)
    {
        assertCanManageProducts("无权修改金融产品");
        CbFinancialProduct existing = selectExisting(id);
        if (existing == null)
        {
            throw new AdminProductException(404, "产品不存在");
        }
        CbFinancialProduct updated = buildProduct(request);
        updated.setId(existing.getId());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());
        productMapper.updateProduct(updated);
        auditLogRecorder.record("UPDATE", "FINANCIAL_PRODUCT", updated.getProductName(), "编辑金融产品");
        return new AdminProductUpdateData(existing.getId(), true);
    }

    public AdminProductDeleteData delete(Long id)
    {
        assertCanManageProducts("无权管理金融产品");
        CbFinancialProduct existing = selectExisting(id);
        if (existing == null)
        {
            throw new AdminProductException(404, "产品不存在");
        }
        productMapper.deleteProductById(existing.getId());
        auditLogRecorder.record("DELETE", "FINANCIAL_PRODUCT", existing.getProductName(), "删除金融产品");
        return new AdminProductDeleteData(true);
    }

    public byte[] importTemplate() throws IOException
    {
        assertCanManageProducts("无权下载金融产品模板");
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            Sheet sheet = workbook.createSheet("金融产品导入模板");
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            for (int index = 0; index < TEMPLATE_HEADERS.size(); index++)
            {
                Cell cell = headerRow.createCell(index);
                cell.setCellValue(TEMPLATE_HEADERS.get(index));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(index, 22 * 256);
            }
            sheet.createFreezePane(0, 1);
            workbook.write(output);
            return output.toByteArray();
        }
    }

    public AdminProductImportValidateData validateImport(MultipartFile file)
    {
        assertCanManageProducts("无权导入金融产品");
        if (file == null || file.isEmpty())
        {
            throw new AdminProductException(400, "导入文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx"))
        {
            throw new AdminProductException(415, "仅支持 xlsx 文件");
        }
        try
        {
            return validateImport(file.getBytes());
        }
        catch (IOException e)
        {
            throw new AdminProductException(400, "导入文件读取失败");
        }
    }

    public AdminProductImportValidateData validateImport(byte[] content)
    {
        assertCanManageProducts("无权导入金融产品");
        if (content == null || content.length == 0)
        {
            throw new AdminProductException(400, "导入文件不能为空");
        }
        List<CbFinancialProduct> validProducts = new ArrayList<>();
        List<AdminProductImportError> errors = new ArrayList<>();
        Set<Integer> invalidRows = new LinkedHashSet<>();
        int totalCount = 0;
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content)))
        {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++)
            {
                Row row = sheet.getRow(rowIndex);
                List<String> values = readImportRow(row, formatter);
                if (values.stream().allMatch(this::isBlank))
                {
                    continue;
                }
                totalCount++;
                int rowNumber = rowIndex + 1;
                List<AdminProductImportError> rowErrors = validateImportRow(rowNumber, values);
                if (rowErrors.isEmpty())
                {
                    validProducts.add(buildImportedProduct(values));
                }
                else
                {
                    errors.addAll(rowErrors);
                    invalidRows.add(rowNumber);
                }
            }
        }
        catch (IOException | RuntimeException e)
        {
            if (e instanceof AdminProductException adminProductException)
            {
                throw adminProductException;
            }
            throw new AdminProductException(400, "导入文件解析失败");
        }
        String token = nextImportToken();
        importSessions.put(token, new ImportSession(validProducts, invalidRows.size()));
        return new AdminProductImportValidateData(token, totalCount, validProducts.size(), invalidRows.size(), errors);
    }

    public AdminProductImportCommitData commitImport(String importToken)
    {
        assertCanManageProducts("无权导入金融产品");
        if (isBlank(importToken))
        {
            throw new AdminProductException(409, "导入任务已提交或已失效");
        }
        ImportSession session = importSessions.remove(importToken.trim());
        if (session == null)
        {
            throw new AdminProductException(409, "导入任务已提交或已失效");
        }
        LocalDateTime now = LocalDateTime.now();
        for (CbFinancialProduct product : session.validProducts())
        {
            product.setId(nextProductId());
            product.setCreatedAt(now);
            product.setUpdatedAt(now);
            productMapper.insertProduct(product);
        }
        auditLogRecorder.record("IMPORT", "FINANCIAL_PRODUCT", "金融产品",
                "导入 " + session.validProducts().size() + " 条金融产品数据");
        return new AdminProductImportCommitData(session.validProducts().size(), session.skippedCount());
    }

    private CbFinancialProduct buildProduct(AdminProductRequest request)
    {
        validateRequest(request);
        OptionItem bank = fixedOptionsService.findBank(request.bankCode())
                .orElseThrow(() -> new AdminProductException(400, "银行机构不在固定列表中"));
        OptionItem productType = fixedOptionsService.findProductType(request.productType())
                .orElseThrow(() -> new AdminProductException(400, "产品类型不合法"));
        List<AdminProductRequest.ProductContact> contacts = normalizedContacts(request);
        CbFinancialProduct product = new CbFinancialProduct();
        product.setBankCode(bank.value());
        product.setBankName(bank.label());
        product.setProductName(request.productName().trim());
        product.setProductType(productType.value());
        product.setAdmissionConditions(request.admissionConditions().trim());
        product.setProductIntro(request.productIntro().trim());
        product.setBusinessManager(contacts.stream().map(AdminProductRequest.ProductContact::businessManager).toList()
                .stream().reduce((left, right) -> left + "\n" + right).orElse(""));
        product.setContactInfo(contacts.stream().map(AdminProductRequest.ProductContact::contactInfo).toList()
                .stream().reduce((left, right) -> left + "\n" + right).orElse(""));
        return product;
    }

    private CbFinancialProduct buildImportedProduct(List<String> values)
    {
        OptionItem bank = fixedOptionsService.findBankByValueOrLabel(values.get(0))
                .orElseThrow(() -> new AdminProductException(400, "银行机构不在固定列表中"));
        OptionItem productType = fixedOptionsService.findProductTypeByValueOrLabel(values.get(2))
                .orElseThrow(() -> new AdminProductException(400, "产品类型不合法"));
        return buildProduct(new AdminProductRequest(bank.value(), values.get(1), productType.value(), values.get(3),
                values.get(4), values.get(5), values.get(6)));
    }

    private List<String> readImportRow(Row row, DataFormatter formatter)
    {
        List<String> values = new ArrayList<>(TEMPLATE_HEADERS.size());
        for (int index = 0; index < TEMPLATE_HEADERS.size(); index++)
        {
            values.add(row == null ? "" : formatter.formatCellValue(row.getCell(index)).trim());
        }
        return values;
    }

    private List<AdminProductImportError> validateImportRow(int rowNumber, List<String> values)
    {
        List<AdminProductImportError> errors = new ArrayList<>();
        requireImportText(errors, rowNumber, "银行机构", values.get(0));
        requireImportText(errors, rowNumber, "产品名称", values.get(1));
        requireImportText(errors, rowNumber, "类型", values.get(2));
        requireImportText(errors, rowNumber, "准入条件", values.get(3));
        requireImportText(errors, rowNumber, "产品介绍", values.get(4));
        requireImportText(errors, rowNumber, "业务经办人", values.get(5));
        requireImportText(errors, rowNumber, "联系方式", values.get(6));
        if (errors.stream().noneMatch(error -> "银行机构".equals(error.field()))
                && fixedOptionsService.findBankByValueOrLabel(values.get(0)).isEmpty())
        {
            errors.add(new AdminProductImportError(rowNumber, "银行机构", values.get(0), "银行机构不在固定列表中"));
        }
        if (errors.stream().noneMatch(error -> "类型".equals(error.field()))
                && fixedOptionsService.findProductTypeByValueOrLabel(values.get(2)).isEmpty())
        {
            errors.add(new AdminProductImportError(rowNumber, "类型", values.get(2), "产品类型不合法"));
        }
        return errors;
    }

    private void requireImportText(List<AdminProductImportError> errors, int rowNumber, String field, String value)
    {
        if (isBlank(value))
        {
            errors.add(new AdminProductImportError(rowNumber, field, "", field + "不能为空"));
        }
    }

    private void assertCanManageProducts(String message)
    {
        Long userId = operatorContext.currentUserId();
        if (userId != null && userId == 1L)
        {
            return;
        }
        CbAccountExtension extension = accountExtensionMapper.selectByUserId(userId);
        if (extension != null && Boolean.TRUE.equals(extension.getEnabled())
                && ("ADMIN".equals(extension.getRole()) || PRODUCT_MANAGER_OFFICE.equals(extension.getOfficeCode())))
        {
            return;
        }
        throw new AdminProductException(403, message);
    }

    private CbFinancialProduct selectExisting(Long id)
    {
        if (id == null || id <= 0)
        {
            return null;
        }
        return productMapper.selectProductById(id);
    }

    private void validateRequest(AdminProductRequest request)
    {
        if (request == null)
        {
            throw new AdminProductException(400, "请求体不能为空");
        }
        requireText(request.bankCode(), "银行机构不能为空");
        requireText(request.productName(), "产品名称不能为空");
        requireText(request.productType(), "类型不能为空");
        requireText(request.admissionConditions(), "准入条件不能为空");
        requireText(request.productIntro(), "产品介绍不能为空");
        normalizedContacts(request);
    }

    private List<AdminProductRequest.ProductContact> normalizedContacts(AdminProductRequest request)
    {
        List<AdminProductRequest.ProductContact> contacts = request.contacts();
        if (contacts == null || contacts.isEmpty())
        {
            contacts = List.of(new AdminProductRequest.ProductContact(request.businessManager(), request.contactInfo()));
        }
        if (contacts.size() > MAX_CONTACTS)
        {
            throw new AdminProductException(400, "业务经办人与联系方式最多添加 5 组");
        }
        List<AdminProductRequest.ProductContact> normalized = new ArrayList<>();
        for (AdminProductRequest.ProductContact contact : contacts)
        {
            String businessManager = contact == null ? null : contact.businessManager();
            String contactInfo = contact == null ? null : contact.contactInfo();
            requireText(businessManager, "业务经办人不能为空");
            requireText(contactInfo, "联系方式不能为空");
            normalized.add(new AdminProductRequest.ProductContact(businessManager.trim(), contactInfo.trim()));
        }
        return normalized;
    }

    private void validateBankFilter(String bankCode)
    {
        if (!isBlank(bankCode) && fixedOptionsService.findBank(bankCode).isEmpty())
        {
            throw new AdminProductException(400, "银行机构不在固定列表中");
        }
    }

    private void validateProductTypeFilter(String productType)
    {
        if (!isBlank(productType) && fixedOptionsService.findProductType(productType).isEmpty())
        {
            throw new AdminProductException(400, "产品类型不合法");
        }
    }

    private void validatePage(int page, int pageSize)
    {
        if (page < 1 || pageSize < 1 || pageSize > 100)
        {
            throw new AdminProductException(400, "分页参数不合法");
        }
    }

    private void requireText(String value, String message)
    {
        if (isBlank(value))
        {
            throw new AdminProductException(400, message);
        }
    }

    private AdminProductListItem toListItem(CbFinancialProduct product)
    {
        return new AdminProductListItem(product.getId(), product.getProductName(), product.getBankCode(),
                product.getBankName(), product.getProductType(), format(product.getUpdatedAt()));
    }

    private AdminProductDetailData toDetailData(CbFinancialProduct product)
    {
        return new AdminProductDetailData(product.getId(), product.getBankCode(), product.getBankName(),
                product.getProductName(), product.getProductType(), product.getAdmissionConditions(),
                product.getProductIntro(), product.getBusinessManager(), product.getContactInfo(),
                format(product.getUpdatedAt()));
    }

    private String format(LocalDateTime time)
    {
        return time == null ? null : time.atOffset(CHINA_OFFSET).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private Long nextProductId()
    {
        return System.currentTimeMillis() * 1000L + ThreadLocalRandom.current().nextInt(1000);
    }

    private String nextImportToken()
    {
        return "import-" + LocalDateTime.now().format(IMPORT_TOKEN_TIME) + "-"
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private String normalizeBlank(String value)
    {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }

    private record ImportSession(List<CbFinancialProduct> validProducts, int skippedCount)
    {
    }
}
