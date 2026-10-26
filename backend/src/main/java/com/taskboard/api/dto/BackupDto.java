package com.taskboard.api.dto;

import com.taskboard.api.entity.Priority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

// エクスポート／インポートで共通のバックアップ形式。ID は持たず、並び順は配列の順で表す。
// ラベルは名前で対応づける（インポート先に同名のラベルがなければ無視する）
public record BackupDto(
		@NotNull Integer version,
		Instant exportedAt,
		@Valid @Size(max = MAX_BOARDS) List<@Valid @NotNull Board> boards,
		@Valid @Size(max = MAX_TEMPLATES) List<@Valid @NotNull Template> templates) {

	// 形式を変えたときに上げる。インポートでは、この値と一致しないファイルを受け付けない
	public static final int CURRENT_VERSION = 1;

	// 極端に大きなファイルでサーバーが重くならないための上限
	public static final int MAX_BOARDS = 100;
	public static final int MAX_LISTS = 10;
	public static final int MAX_CARDS = 1000;
	public static final int MAX_CHECK_ITEMS = 200;
	public static final int MAX_TEMPLATES = 500;

	public record Board(@NotBlank @Size(max = 255) String title,
			@Valid @Size(max = MAX_LISTS) List<@Valid @NotNull ListItem> lists) {
	}

	public record ListItem(@NotBlank @Size(max = 255) String title,
			@Valid @Size(max = MAX_CARDS) List<@Valid @NotNull Card> cards) {
	}

	public record Card(
			@NotBlank @Size(max = 255) String title,
			String description,
			LocalDate dueDate,
			Priority priority,
			@Min(0) @Max(365) Integer warnDays,
			List<String> labels,
			@Valid @Size(max = MAX_CHECK_ITEMS) List<@Valid @NotNull CheckItem> checklist) {
	}

	public record CheckItem(@NotBlank @Size(max = 255) String text, boolean done) {
	}

	public record Template(
			@NotBlank @Size(max = 255) String title,
			String description,
			Priority priority,
			@Min(0) @Max(365) Integer warnDays,
			List<String> labels,
			@Size(max = MAX_CHECK_ITEMS) List<@NotBlank @Size(max = 255) String> checklist) {
	}
}
