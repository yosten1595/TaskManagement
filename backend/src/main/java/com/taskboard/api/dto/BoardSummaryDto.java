package com.taskboard.api.dto;

import com.taskboard.api.entity.Board;
import java.time.Instant;
import java.util.UUID;

public record BoardSummaryDto(UUID id, String title, Instant createdAt) {

	public static BoardSummaryDto from(Board board) {
		return new BoardSummaryDto(board.getId(), board.getTitle(), board.getCreatedAt());
	}
}
