package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PublicContentListItem(
        Long id,
        String title,
        String category,
        @JsonProperty("office_name") String officeName,
        @JsonProperty("published_at") String publishedAt)
{
}
