package com.centralbank.eplatform.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminDashboardSummaryData(
        @JsonProperty("published_content_count") int publishedContentCount,
        @JsonProperty("product_count") int productCount,
        @JsonProperty("account_count") int accountCount,
        @JsonProperty("today_operation_count") int todayOperationCount,
        @JsonProperty("recent_contents") List<AdminDashboardRecentContent> recentContents)
{
}
