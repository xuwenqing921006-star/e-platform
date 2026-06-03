package com.centralbank.eplatform.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PublicContentDetailData(
        Long id,
        String title,
        String category,
        @JsonProperty("office_name") String officeName,
        @JsonProperty("published_at") String publishedAt,
        @JsonProperty("rich_text_html") String richTextHtml,
        List<PublicAttachment> attachments)
{
}
