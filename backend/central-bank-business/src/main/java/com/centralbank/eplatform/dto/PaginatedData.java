package com.centralbank.eplatform.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PaginatedData<T>(
        List<T> items,
        int total,
        int page,
        @JsonProperty("page_size") int pageSize)
{
}
