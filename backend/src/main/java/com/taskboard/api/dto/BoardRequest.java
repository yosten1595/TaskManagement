package com.taskboard.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ボードの作成・名前変更で共通
public record BoardRequest(@NotBlank @Size(max = 255) String title) {
}
