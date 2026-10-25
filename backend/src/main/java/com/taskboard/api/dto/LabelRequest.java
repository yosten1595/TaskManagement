package com.taskboard.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// ラベルの作成・変更で共通。color は #rrggbb
public record LabelRequest(@NotBlank String name, @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String color) {
}
