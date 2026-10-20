package com.taskboard.api.dto;

import com.taskboard.api.entity.Board;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BoardDetailDto(UUID id, String title, Instant createdAt, List<ListDto> lists) {

	public static BoardDetailDto from(Board board) {
		return new BoardDetailDto(board.getId(), board.getTitle(), board.getCreatedAt(),
				board.getLists().stream().map(ListDto::from).toList());
	}
}
