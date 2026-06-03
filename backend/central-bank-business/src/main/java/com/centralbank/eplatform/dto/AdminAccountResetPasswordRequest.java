package com.centralbank.eplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminAccountResetPasswordRequest(@JsonProperty("new_password") String newPassword)
{
}
