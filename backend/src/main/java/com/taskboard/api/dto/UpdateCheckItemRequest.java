package com.taskboard.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// PUT なので text と done の両方を指定する。並び順（position）は変えない
public record UpdateCheckItemRequest(@NotBlank String text, @NotNull Boolean done) {
}
