package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminAccountDetailData(
        Long id,
        String username,
        @JsonProperty("display_name") String displayName,
        String role,
        @JsonProperty("office_code") String officeCode,
        @JsonProperty("office_name") String officeName,
        boolean enabled)
{
}
