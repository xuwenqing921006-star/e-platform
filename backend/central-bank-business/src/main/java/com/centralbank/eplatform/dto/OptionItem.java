package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OptionItem(String value, String label, @JsonProperty("county_code") String countyCode)
{
    public static OptionItem of(String value, String label)
    {
        return new OptionItem(value, label, null);
    }

    public static OptionItem countyOffice(String value, String label, String countyCode)
    {
        return new OptionItem(value, label, countyCode);
    }
}
