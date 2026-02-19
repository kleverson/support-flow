package br.com.supportflow.SupportFlow.user.dto;

import br.com.supportflow.SupportFlow.user.entity.enums.UserClientRole;
import jakarta.validation.constraints.NotNull;

public record UserClientAccessAssign(
        @NotNull UserClientRole role
) {
}
