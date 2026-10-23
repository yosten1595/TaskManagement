package com.taskboard.api.dto;

import com.taskboard.api.entity.CardSortKey;
import jakarta.validation.constraints.NotNull;

public record SortListRequest(@NotNull CardSortKey by) {
}
