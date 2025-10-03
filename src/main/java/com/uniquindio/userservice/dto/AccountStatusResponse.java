package com.uniquindio.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountStatusResponse (
    @NotNull
    UserAccountStatusEnum account_status
){
}
