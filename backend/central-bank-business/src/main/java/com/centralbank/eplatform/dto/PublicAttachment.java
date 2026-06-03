package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PublicAttachment(
        Long id,
        @JsonProperty("file_name") String fileName,
        @JsonProperty("file_type") String fileType,
        @JsonProperty("file_size") Long fileSize,
        @JsonProperty("download_url") String downloadUrl)
{
}
