package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminProductRequest(
        @JsonProperty("bank_code") String bankCode,
        @JsonProperty("product_name") String productName,
        @JsonProperty("product_type") String productType,
        @JsonProperty("admission_conditions") String admissionConditions,
        @JsonProperty("product_intro") String productIntro,
        @JsonProperty("business_manager") String businessManager,
        @JsonProperty("contact_info") String contactInfo)
{
}
