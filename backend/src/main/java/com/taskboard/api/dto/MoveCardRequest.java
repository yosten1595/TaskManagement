package com.taskboard.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

// position は移動先リスト内での 0 始まりの挿入位置。省略・範囲外なら末尾に置く
public record MoveCardRequest(
		@NotNull UUID listId,
		@PositiveOrZero Integer position) {
}
