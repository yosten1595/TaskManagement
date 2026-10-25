package com.taskboard.api.dto;

import com.taskboard.api.entity.Label;
import com.taskboard.api.entity.Priority;
import com.taskboard.api.entity.Template;
import com.taskboard.api.entity.TemplateLabel;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record TemplateDto(
		UUID id,
		String title,
		String description,
		Priority priority,
		int warnDays,
		Instant createdAt,
		List<LabelDto> labels,
		List<TemplateCheckItemDto> checklist) {

	public static TemplateDto from(Template template) {
		return new TemplateDto(
				template.getId(),
				template.getTitle(),
				template.getDescription(),
				template.getPriority(),
				template.getWarnDays(),
				template.getCreatedAt(),
				template.getLabels().stream()
						.map(TemplateLabel::getLabel)
						.sorted(Comparator.comparing(Label::getName))
						.map(LabelDto::from)
						.toList(),
				template.getChecklist().stream().map(TemplateCheckItemDto::from).toList());
	}
}
