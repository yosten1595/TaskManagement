package com.taskboard.api.dto;

import com.taskboard.api.entity.Priority;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// テンプレートの作成・更新で共通（PUT は全項目を置き換える）。title 以外は省略可
// （labelIds・checklist は空、warnDays は 3 になる）。checklist はチェック項目のテキストを表示順に並べる
public record TemplateRequest(
		@NotBlank String title,
		String description,
		Priority priority,
		@Min(0) @Max(365) Integer warnDays,
		Set<UUID> labelIds,
		List<@NotBlank String> checklist) {
}
