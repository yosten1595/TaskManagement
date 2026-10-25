package com.taskboard.api.dto;

import com.taskboard.api.entity.Priority;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

// dueDate / priority / labelIds / warnDays は任意（warnDays の省略時は 3）
public record CreateCardRequest(
		@NotNull UUID listId,
		@NotBlank String title,
		LocalDate dueDate,
		Priority priority,
		Set<UUID> labelIds,
		@Min(0) @Max(365) Integer warnDays) {
}
