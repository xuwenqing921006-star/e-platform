package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminProductImportCommitData(
        @JsonProperty("imported_count") int importedCount,
        @JsonProperty("skipped_count") int skippedCount)
{
}
