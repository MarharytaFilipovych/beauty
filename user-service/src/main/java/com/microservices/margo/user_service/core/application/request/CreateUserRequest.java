package com.microservices.margo.user_service.core.application.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank String surname,
        String phone,
        @NotNull LocalDate birthDate,
        @NotBlank @Email String email
) {}