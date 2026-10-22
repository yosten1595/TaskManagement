package com.taskboard.api.dto;

import com.taskboard.api.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

// dueDate / priority / labelIds は任意
public record CreateCardRequest(
		@NotNull UUID listId,
		@NotBlank String title,
		LocalDate dueDate,
		Priority priority,
		Set<UUID> labelIds) {
}
