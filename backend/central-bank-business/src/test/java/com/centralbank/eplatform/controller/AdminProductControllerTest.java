package com.centralbank.eplatform.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.centralbank.eplatform.controller.admin.AdminProductController;
import com.centralbank.eplatform.dto.AdminProductCreateData;
import com.centralbank.eplatform.dto.AdminProductDeleteData;
import com.centralbank.eplatform.dto.AdminProductDetailData;
import com.centralbank.eplatform.dto.AdminProductImportCommitData;
import com.centralbank.eplatform.dto.AdminProductImportError;
import com.centralbank.eplatform.dto.AdminProductImportValidateData;
import com.centralbank.eplatform.dto.AdminProductListItem;
import com.centralbank.eplatform.dto.AdminProductRequest;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.service.AdminProductException;
import com.centralbank.eplatform.service.AdminProductService;

class AdminProductControllerTest
{
    @Test
    void listEndpointReturnsContractPaginationWithoutRate() throws Exception
    {
        AdminProductService service = mock(AdminProductService.class);
        when(service.list("惠农", "ABC", "AGRICULTURAL", 1, 20))
                .thenReturn(new PaginatedData<>(List.of(new AdminProductListItem(
                        2001L,
                        "惠农e贷",
                        "ABC",
                        "农业银行",
                        "AGRICULTURAL",
                        "2026-05-30T09:30:00+08:00")), 1, 1, 20));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(service)).build();

        mockMvc.perform(get("/api/admin/products")
                        .param("keyword", "惠农")
                        .param("bank_code", "ABC")
                        .param("product_type", "AGRICULTURAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[0].product_name").value("惠农e贷"))
                .andExpect(jsonPath("$.data.items[0].bank_code").value("ABC"))
                .andExpect(jsonPath("$.data.items[0].updated_at").value("2026-05-30T09:30:00+08:00"))
                .andExpect(jsonPath("$.data.items[0].reference_rate").doesNotExist());

        verify(service).list("惠农", "ABC", "AGRICULTURAL", 1, 20);
    }

    @Test
    void createEndpointReturnsContractData() throws Exception
    {
        AdminProductService service = mock(AdminProductService.class);
        when(service.create(org.mockito.ArgumentMatchers.any())).thenReturn(new AdminProductCreateData(2009L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(service)).build();

        mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bank_code":"ABC",
                                  "product_name":"惠农e贷",
                                  "product_type":"AGRICULTURAL",
                                  "admission_conditions":"面向涉农经营主体。",
                                  "product_intro":"用于农业生产经营。",
                                  "business_manager":"张经理",
                                  "contact_info":"0459-0002001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(2009));
    }

    @Test
    void createEndpointAcceptsMultipleProductContacts() throws Exception
    {
        AdminProductService service = mock(AdminProductService.class);
        ArgumentCaptor<AdminProductRequest> requestCaptor = ArgumentCaptor.forClass(AdminProductRequest.class);
        when(service.create(org.mockito.ArgumentMatchers.any())).thenReturn(new AdminProductCreateData(2010L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(service)).build();

        mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bank_code":"ABC",
                                  "product_name":"惠农e贷",
                                  "product_type":"AGRICULTURAL",
                                  "admission_conditions":"面向涉农经营主体。",
                                  "product_intro":"用于农业生产经营。",
                                  "business_manager":"张经理\\n李经理",
                                  "contact_info":"0459-0002001\\n0459-0002002",
                                  "contacts":[
                                    {"business_manager":"张经理","contact_info":"0459-0002001"},
                                    {"business_manager":"李经理","contact_info":"0459-0002002"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2010));

        verify(service).create(requestCaptor.capture());
        AdminProductRequest request = requestCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(request.contacts()).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(request.contacts().get(0).businessManager()).isEqualTo("张经理");
        org.assertj.core.api.Assertions.assertThat(request.contacts().get(1).contactInfo()).isEqualTo("0459-0002002");
    }

    @Test
    void detailEndpointReturnsSevenBusinessFieldsAndNoRate() throws Exception
    {
        AdminProductService service = mock(AdminProductService.class);
        when(service.detail(2001L)).thenReturn(Optional.of(new AdminProductDetailData(
                2001L,
                "ABC",
                "农业银行",
                "惠农e贷",
                "AGRICULTURAL",
                "面向涉农经营主体。",
                "用于农业生产经营。",
                "张经理",
                "0459-0002001",
                "2026-05-30T09:30:00+08:00")));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(service)).build();

        mockMvc.perform(get("/api/admin/products/2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bank_code").value("ABC"))
                .andExpect(jsonPath("$.data.bank_name").value("农业银行"))
                .andExpect(jsonPath("$.data.product_name").value("惠农e贷"))
                .andExpect(jsonPath("$.data.product_type").value("AGRICULTURAL"))
                .andExpect(jsonPath("$.data.admission_conditions").value("面向涉农经营主体。"))
                .andExpect(jsonPath("$.data.product_intro").value("用于农业生产经营。"))
                .andExpect(jsonPath("$.data.business_manager").value("张经理"))
                .andExpect(jsonPath("$.data.contact_info").value("0459-0002001"))
                .andExpect(jsonPath("$.data.reference_rate").doesNotExist());
    }

    @Test
    void detailEndpointReturnsContracted404Json() throws Exception
    {
        AdminProductService service = mock(AdminProductService.class);
        when(service.detail(999L)).thenReturn(Optional.empty());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(service)).build();

        mockMvc.perform(get("/api/admin/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("产品不存在"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void deleteEndpointReturnsContractData() throws Exception
    {
        AdminProductService service = mock(AdminProductService.class);
        when(service.delete(2001L)).thenReturn(new AdminProductDeleteData(true));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(service)).build();

        mockMvc.perform(delete("/api/admin/products/2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    void forbiddenEndpointReturnsContractJson() throws Exception
    {
        AdminProductService service = mock(AdminProductService.class);
        when(service.delete(2001L)).thenThrow(new AdminProductException(403, "无权管理金融产品"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(service)).build();

        mockMvc.perform(delete("/api/admin/products/2001"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权管理金融产品"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void downloadTemplateReturnsXlsxAttachment() throws Exception
    {
        AdminProductService service = mock(AdminProductService.class);
        when(service.importTemplate()).thenReturn(new byte[] { 1, 2, 3 });
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(service)).build();

        mockMvc.perform(get("/api/admin/products/import-template/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"financial-product-import-template.xlsx\""));
    }

    @Test
    void validateImportEndpointReturnsContractSummaryAndErrors() throws Exception
    {
        AdminProductService service = mock(AdminProductService.class);
        MockMultipartFile file = new MockMultipartFile("file", "products.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[] { 1, 2, 3 });
        when(service.validateImport(file)).thenReturn(new AdminProductImportValidateData(
                "import-20260602-001",
                112,
                110,
                2,
                List.of(new AdminProductImportError(38, "银行机构", "阳光惠农贷", "银行机构字段存在特殊来源值"))));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(service)).build();

        mockMvc.perform(multipart("/api/admin/products/import/validate").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.import_token").value("import-20260602-001"))
                .andExpect(jsonPath("$.data.total_count").value(112))
                .andExpect(jsonPath("$.data.valid_count").value(110))
                .andExpect(jsonPath("$.data.invalid_count").value(2))
                .andExpect(jsonPath("$.data.errors[0].row_number").value(38))
                .andExpect(jsonPath("$.data.errors[0].field").value("银行机构"))
                .andExpect(jsonPath("$.data.errors[0].raw_value").value("阳光惠农贷"))
                .andExpect(jsonPath("$.data.errors[0].message").value("银行机构字段存在特殊来源值"));

        verify(service).validateImport(file);
    }

    @Test
    void commitImportEndpointReturnsImportedAndSkippedCounts() throws Exception
    {
        AdminProductService service = mock(AdminProductService.class);
        when(service.commitImport("import-20260602-001"))
                .thenReturn(new AdminProductImportCommitData(110, 2));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(service)).build();

        mockMvc.perform(post("/api/admin/products/import/commit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "import_token":"import-20260602-001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imported_count").value(110))
                .andExpect(jsonPath("$.data.skipped_count").value(2));

        verify(service).commitImport("import-20260602-001");
    }

    @Test
    void validateImportRejectsNonXlsxAsUnsupportedMediaType() throws Exception
    {
        AdminProductService service = mock(AdminProductService.class);
        MockMultipartFile file = new MockMultipartFile("file", "products.xls",
                "application/vnd.ms-excel", new byte[] { 1, 2, 3 });
        when(service.validateImport(file)).thenThrow(new AdminProductException(415, "仅支持 xlsx 文件"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(service)).build();

        mockMvc.perform(multipart("/api/admin/products/import/validate").file(file))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value(415))
                .andExpect(jsonPath("$.message").value("仅支持 xlsx 文件"));
    }
}
