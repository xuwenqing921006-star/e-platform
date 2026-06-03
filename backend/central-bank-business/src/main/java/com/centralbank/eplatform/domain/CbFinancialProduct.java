package com.centralbank.eplatform.domain;

import java.time.LocalDateTime;

public class CbFinancialProduct
{
    private Long id;
    private String bankCode;
    private String bankName;
    private String productName;
    private String productType;
    private String admissionConditions;
    private String productIntro;
    private String businessManager;
    private String contactInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getAdmissionConditions() { return admissionConditions; }
    public void setAdmissionConditions(String admissionConditions) { this.admissionConditions = admissionConditions; }
    public String getProductIntro() { return productIntro; }
    public void setProductIntro(String productIntro) { this.productIntro = productIntro; }
    public String getBusinessManager() { return businessManager; }
    public void setBusinessManager(String businessManager) { this.businessManager = businessManager; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
