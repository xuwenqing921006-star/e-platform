package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminProductImportCommitRequest(@JsonProperty("import_token") String importToken)
{
}
