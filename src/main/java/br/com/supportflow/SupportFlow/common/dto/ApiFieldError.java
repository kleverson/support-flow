package br.com.supportflow.SupportFlow.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApiFieldError(
        @Schema(example = "name") String field,
        @Schema(example = "name é obrigatório") String message
) {
}
