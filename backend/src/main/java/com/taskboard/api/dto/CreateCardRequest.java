package com.taskboard.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCardRequest(
		@NotNull UUID listId,
		@NotBlank String title) {
}
