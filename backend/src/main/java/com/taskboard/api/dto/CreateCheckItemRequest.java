package com.taskboard.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCheckItemRequest(@NotBlank String text) {
}
