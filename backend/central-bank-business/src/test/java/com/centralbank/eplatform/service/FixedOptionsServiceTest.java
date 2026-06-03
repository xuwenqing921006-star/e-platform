package com.centralbank.eplatform.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import com.centralbank.eplatform.dto.OptionsResponse;

class FixedOptionsServiceTest
{
    private final FixedOptionsService service = new FixedOptionsService();

    @Test
    void returnsAllContractOptions()
    {
        OptionsResponse options = service.getOptions();

        assertThat(options.contentCategories()).extracting("value")
                .containsExactly("SERVICE_GUIDE", "POLICY_PROMOTION");
        assertThat(options.productTypes()).extracting("value")
                .containsExactly("AGRICULTURAL", "SMALL_MICRO");
        assertThat(options.offices()).hasSize(14);
        assertThat(options.offices()).extracting("value")
                .contains("MONETARY_CREDIT", "CURRENCY_GOLD_SILVER", "ZHAOZHOU", "DUMENG");
        assertThat(options.banks()).hasSize(17);
        assertThat(options.banks()).extracting("value")
                .contains("ADBC_DAQING", "ABC", "SUNSHINE_AGRICULTURE", "LONGJIANG_BANK");
    }
}
