package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminProductImportError(
        @JsonProperty("row_number") int rowNumber,
        String field,
        @JsonProperty("raw_value") String rawValue,
        String message)
{
}
