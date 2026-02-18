package br.com.supportflow.SupportFlow.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegister(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
