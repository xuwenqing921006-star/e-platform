package com.centralbank.eplatform.domain;

import java.time.LocalDateTime;

public class CbAuditLog
{
    private Long id;
    private String operatorName;
    private String operationType;
    private Integer businessType;
    private String objectType;
    private String objectName;
    private String description;
    private LocalDateTime operatedAt;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getOperatorName()
    {
        return operatorName;
    }

    public void setOperatorName(String operatorName)
    {
        this.operatorName = operatorName;
    }

    public String getOperationType()
    {
        return operationType;
    }

    public void setOperationType(String operationType)
    {
        this.operationType = operationType;
    }

    public Integer getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(Integer businessType)
    {
        this.businessType = businessType;
    }

    public String getObjectType()
    {
        return objectType;
    }

    public void setObjectType(String objectType)
    {
        this.objectType = objectType;
    }

    public String getObjectName()
    {
        return objectName;
    }

    public void setObjectName(String objectName)
    {
        this.objectName = objectName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public LocalDateTime getOperatedAt()
    {
        return operatedAt;
    }

    public void setOperatedAt(LocalDateTime operatedAt)
    {
        this.operatedAt = operatedAt;
    }
}
