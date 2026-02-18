package br.com.supportflow.SupportFlow.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record  ApiError(
        @Schema(example = "VALIDATION_ERROR") String code,
        @Schema(example = "Required field") String message,
        List<ApiFieldError> fieldErrors
) {
    public ApiError(String code, String message) {
        this(code, message, List.of());
    }
}


