package com.centralbank.eplatform.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.centralbank.eplatform.controller.publicapi.PublicProductController;
import com.centralbank.eplatform.dto.PaginatedData;
import com.centralbank.eplatform.dto.PublicProductDetailData;
import com.centralbank.eplatform.dto.PublicProductListItem;
import com.centralbank.eplatform.service.PublicProductService;

class PublicProductControllerTest
{
    @Test
    void listEndpointReturnsPublicProductContractJson() throws Exception
    {
        PublicProductService service = mock(PublicProductService.class);
        when(service.list(1, 10)).thenReturn(new PaginatedData<>(List.of(new PublicProductListItem(
                2001L,
                "农业银行",
                "惠农e贷",
                "AGRICULTURAL")), 112, 1, 10));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PublicProductController(service)).build();

        mockMvc.perform(get("/api/public/products")
                        .param("page", "1")
                        .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.total").value(112))
                .andExpect(jsonPath("$.data.page_size").value(10))
                .andExpect(jsonPath("$.data.items[0].bank_name").value("农业银行"))
                .andExpect(jsonPath("$.data.items[0].product_name").value("惠农e贷"))
                .andExpect(jsonPath("$.data.items[0].product_type").value("AGRICULTURAL"))
                .andExpect(jsonPath("$.data.items[0].admission_conditions").doesNotExist());

        verify(service).list(1, 10);
    }

    @Test
    void listEndpointReturnsContracted400JsonForInvalidPagination() throws Exception
    {
        PublicProductService service = mock(PublicProductService.class);
        when(service.list(1, 51)).thenThrow(new IllegalArgumentException("分页参数不合法"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PublicProductController(service)).build();

        mockMvc.perform(get("/api/public/products")
                        .param("page", "1")
                        .param("page_size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("分页参数不合法"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(service).list(1, 51);
    }

    @Test
    void detailEndpointReturnsOnlySevenBusinessFields() throws Exception
    {
        PublicProductService service = mock(PublicProductService.class);
        when(service.detail(2001L)).thenReturn(Optional.of(new PublicProductDetailData(
                2001L,
                "农业银行",
                "惠农e贷",
                "AGRICULTURAL",
                "面向涉农经营主体。",
                "用于农业生产经营流动资金需求。",
                "张经理",
                "0459-0002001")));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PublicProductController(service)).build();

        mockMvc.perform(get("/api/public/products/2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bank_name").value("农业银行"))
                .andExpect(jsonPath("$.data.product_name").value("惠农e贷"))
                .andExpect(jsonPath("$.data.product_type").value("AGRICULTURAL"))
                .andExpect(jsonPath("$.data.admission_conditions").value("面向涉农经营主体。"))
                .andExpect(jsonPath("$.data.product_intro").value("用于农业生产经营流动资金需求。"))
                .andExpect(jsonPath("$.data.business_manager").value("张经理"))
                .andExpect(jsonPath("$.data.contact_info").value("0459-0002001"))
                .andExpect(jsonPath("$.data.reference_rate").doesNotExist())
                .andExpect(jsonPath("$.data.loan_amount").doesNotExist())
                .andExpect(jsonPath("$.data.updated_at").doesNotExist());

        verify(service).detail(2001L);
    }

    @Test
    void detailEndpointReturnsContracted404Json() throws Exception
    {
        PublicProductService service = mock(PublicProductService.class);
        when(service.detail(999L)).thenReturn(Optional.empty());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PublicProductController(service)).build();

        mockMvc.perform(get("/api/public/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("产品不存在"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(service).detail(999L);
    }
}
