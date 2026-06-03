package com.centralbank.eplatform.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminProductImportValidateData(
        @JsonProperty("import_token") String importToken,
        @JsonProperty("total_count") int totalCount,
        @JsonProperty("valid_count") int validCount,
        @JsonProperty("invalid_count") int invalidCount,
        List<AdminProductImportError> errors)
{
}
