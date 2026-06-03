package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminDashboardRecentContent(
        Long id,
        String title,
        String category,
        @JsonProperty("published_at") String publishedAt)
{
}
