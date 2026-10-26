package com.taskboard.api.dto;

import com.taskboard.api.entity.Priority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// title 以外は任意。warnDays の省略時は 3。
// description / checklist / position は、削除したカードを元に戻すときに使う（position を省略すると末尾に追加）
public record CreateCardRequest(
		@NotNull UUID listId,
		@NotBlank @Size(max = 255) String title,
		LocalDate dueDate,
		Priority priority,
		Set<UUID> labelIds,
		@Min(0) @Max(365) Integer warnDays,
		String description,
		@Valid @Size(max = 200) List<@Valid @NotNull CheckItem> checklist,
		@Min(0) Integer position) {

	public record CheckItem(@NotBlank @Size(max = 255) String text, boolean done) {
	}
}
