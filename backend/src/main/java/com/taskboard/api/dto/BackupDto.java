package com.taskboard.api.dto;

import com.taskboard.api.entity.Priority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

// エクスポート／インポートで共通のバックアップ形式。ID は持たず、並び順は配列の順で表す。
// ラベルは名前で対応づける（インポート先に同名のラベルがなければ無視する）
public record BackupDto(
		@NotNull Integer version,
		Instant exportedAt,
		@Valid List<@Valid @NotNull Board> boards,
		@Valid List<@Valid @NotNull Template> templates) {

	// 形式を変えたときに上げる。インポートでは、この値と一致しないファイルを受け付けない
	public static final int CURRENT_VERSION = 1;

	public record Board(@NotBlank String title, @Valid List<@Valid @NotNull ListItem> lists) {
	}

	public record ListItem(@NotBlank String title, @Valid List<@Valid @NotNull Card> cards) {
	}

	public record Card(
			@NotBlank String title,
			String description,
			LocalDate dueDate,
			Priority priority,
			@Min(0) @Max(365) Integer warnDays,
			List<String> labels,
			@Valid List<@Valid @NotNull CheckItem> checklist) {
	}

	public record CheckItem(@NotBlank String text, boolean done) {
	}

	public record Template(
			@NotBlank String title,
			String description,
			Priority priority,
			@Min(0) @Max(365) Integer warnDays,
			List<String> labels,
			List<@NotBlank String> checklist) {
	}
}
