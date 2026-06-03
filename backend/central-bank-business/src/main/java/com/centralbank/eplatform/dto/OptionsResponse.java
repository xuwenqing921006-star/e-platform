package com.centralbank.eplatform.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OptionsResponse(
        @JsonProperty("content_categories") List<OptionItem> contentCategories,
        @JsonProperty("product_types") List<OptionItem> productTypes,
        List<OptionItem> offices,
        List<OptionItem> banks)
{
}
