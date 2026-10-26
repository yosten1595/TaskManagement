package com.taskboard.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCheckItemRequest(@NotBlank @Size(max = 255) String text) {
}
