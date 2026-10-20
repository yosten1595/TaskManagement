package com.taskboard.api.dto;

import com.taskboard.api.entity.TaskList;
import java.util.List;
import java.util.UUID;

public record ListDto(UUID id, String title, int position, List<CardDto> cards) {

	public static ListDto from(TaskList list) {
		return new ListDto(list.getId(), list.getTitle(), list.getPosition(),
				list.getCards().stream().map(CardDto::from).toList());
	}
}
