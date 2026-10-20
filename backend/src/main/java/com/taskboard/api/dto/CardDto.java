package com.taskboard.api.dto;

import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.CardLabel;
import com.taskboard.api.entity.Label;
import com.taskboard.api.entity.Priority;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record CardDto(
		UUID id,
		UUID listId,
		String title,
		String description,
		LocalDate dueDate,
		Priority priority,
		int warnDays,
		int position,
		Instant createdAt,
		List<LabelDto> labels,
		List<CheckItemDto> checklist) {

	public static CardDto from(Card card) {
		return new CardDto(
				card.getId(),
				card.getList().getId(),
				card.getTitle(),
				card.getDescription(),
				card.getDueDate(),
				card.getPriority(),
				card.getWarnDays(),
				card.getPosition(),
				card.getCreatedAt(),
				card.getLabels().stream()
						.map(CardLabel::getLabel)
						.sorted(Comparator.comparing(Label::getName))
						.map(LabelDto::from)
						.toList(),
				card.getChecklist().stream().map(CheckItemDto::from).toList());
	}
}
