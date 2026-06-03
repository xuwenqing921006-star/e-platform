package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminAccountRequest(
        String username,
        @JsonProperty("display_name") String displayName,
        String role,
        @JsonProperty("office_code") String officeCode,
        @JsonProperty("initial_password") String initialPassword,
        Boolean enabled)
{
}
