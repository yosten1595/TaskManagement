package com.taskboard.api.dto;

import com.taskboard.api.entity.TemplateCheckItem;
import java.util.UUID;

// テンプレート側のチェック項目は未完了でコピーするため、done を持たない
public record TemplateCheckItemDto(UUID id, String text, int position) {

	public static TemplateCheckItemDto from(TemplateCheckItem item) {
		return new TemplateCheckItemDto(item.getId(), item.getText(), item.getPosition());
	}
}
