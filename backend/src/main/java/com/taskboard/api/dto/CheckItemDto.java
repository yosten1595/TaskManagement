package com.taskboard.api.dto;

import com.taskboard.api.entity.CheckItem;
import java.util.UUID;

public record CheckItemDto(UUID id, String text, boolean done, int position) {

	public static CheckItemDto from(CheckItem item) {
		return new CheckItemDto(item.getId(), item.getText(), item.isDone(), item.getPosition());
	}
}
