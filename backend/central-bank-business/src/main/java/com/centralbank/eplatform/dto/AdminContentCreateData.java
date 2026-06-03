package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminContentCreateData(Long id, @JsonProperty("published_at") String publishedAt)
{
}
