package com.centralbank.eplatform.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminProductRequest(
        @JsonProperty("bank_code") String bankCode,
        @JsonProperty("product_name") String productName,
        @JsonProperty("product_type") String productType,
        @JsonProperty("admission_conditions") String admissionConditions,
        @JsonProperty("product_intro") String productIntro,
        @JsonProperty("business_manager") String businessManager,
        @JsonProperty("contact_info") String contactInfo,
        List<ProductContact> contacts)
{
    public AdminProductRequest(String bankCode, String productName, String productType, String admissionConditions,
            String productIntro, String businessManager, String contactInfo)
    {
        this(bankCode, productName, productType, admissionConditions, productIntro, businessManager, contactInfo, null);
    }

    public record ProductContact(
            @JsonProperty("business_manager") String businessManager,
            @JsonProperty("contact_info") String contactInfo)
    {
    }
}
