package com.centralbank.eplatform.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminContentDetailData(
        Long id,
        String title,
        String category,
        @JsonProperty("office_code") String officeCode,
        @JsonProperty("office_name") String officeName,
        @JsonProperty("rich_text_html") String richTextHtml,
        @JsonProperty("published_at") String publishedAt,
        List<PublicAttachment> attachments)
{
}
