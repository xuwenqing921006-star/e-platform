package com.centralbank.eplatform.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminContentRequest(
        String title,
        String category,
        @JsonProperty("office_code") String officeCode,
        @JsonProperty("rich_text_html") String richTextHtml,
        @JsonProperty("attachment_ids") List<Long> attachmentIds)
{
}
