package com.taskboard.api.dto;

import jakarta.validation.constraints.NotBlank;

// ボードの作成・名前変更で共通
public record BoardRequest(@NotBlank String title) {
}
