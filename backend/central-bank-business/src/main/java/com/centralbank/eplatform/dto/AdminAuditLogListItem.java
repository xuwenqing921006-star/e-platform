package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminAuditLogListItem(
        Long id,
        @JsonProperty("operator_name") String operatorName,
        @JsonProperty("operation_type") String operationType,
        @JsonProperty("object_type") String objectType,
        @JsonProperty("object_name") String objectName,
        String description,
        @JsonProperty("operated_at") String operatedAt)
{
}
