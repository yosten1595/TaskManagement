package com.taskboard.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCardFromTemplateRequest(@NotNull UUID listId) {
}
