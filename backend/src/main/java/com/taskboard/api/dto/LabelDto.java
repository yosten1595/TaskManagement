package com.taskboard.api.dto;

import com.taskboard.api.entity.Label;
import java.util.UUID;

public record LabelDto(UUID id, String name, String color) {

	public static LabelDto from(Label label) {
		return new LabelDto(label.getId(), label.getName(), label.getColor());
	}
}
