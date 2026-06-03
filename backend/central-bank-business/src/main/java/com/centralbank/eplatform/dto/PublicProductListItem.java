package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PublicProductListItem(
        Long id,
        @JsonProperty("bank_name") String bankName,
        @JsonProperty("product_name") String productName,
        @JsonProperty("product_type") String productType)
{
}
