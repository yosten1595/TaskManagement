package com.taskboard.api.dto;

import jakarta.validation.constraints.NotBlank;

// リストの追加・名前変更で共通
public record ListRequest(@NotBlank String title) {
}
