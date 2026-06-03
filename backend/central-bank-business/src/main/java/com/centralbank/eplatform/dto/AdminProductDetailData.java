package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminProductDetailData(
        Long id,
        @JsonProperty("bank_code") String bankCode,
        @JsonProperty("bank_name") String bankName,
        @JsonProperty("product_name") String productName,
        @JsonProperty("product_type") String productType,
        @JsonProperty("admission_conditions") String admissionConditions,
        @JsonProperty("product_intro") String productIntro,
        @JsonProperty("business_manager") String businessManager,
        @JsonProperty("contact_info") String contactInfo,
        @JsonProperty("updated_at") String updatedAt)
{
}
