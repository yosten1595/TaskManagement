package com.taskboard.api.dto;

import com.taskboard.api.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

// PUT なので全項目を置き換える。title 以外は null / 省略で未設定（labelIds は空）に戻る
public record UpdateCardRequest(
		@NotBlank String title,
		String description,
		LocalDate dueDate,
		Priority priority,
		Set<UUID> labelIds) {
}
