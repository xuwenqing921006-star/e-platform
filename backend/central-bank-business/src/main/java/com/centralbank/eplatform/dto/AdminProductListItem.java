package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminProductListItem(
        Long id,
        @JsonProperty("product_name") String productName,
        @JsonProperty("bank_code") String bankCode,
        @JsonProperty("bank_name") String bankName,
        @JsonProperty("product_type") String productType,
        @JsonProperty("updated_at") String updatedAt)
{
}
