package com.taskboard.api.service;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.dto.TemplateDto;
import com.taskboard.api.dto.TemplateRequest;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.CardLabel;
import com.taskboard.api.entity.CheckItem;
import com.taskboard.api.entity.Label;
import com.taskboard.api.entity.TaskList;
import com.taskboard.api.entity.Template;
import com.taskboard.api.entity.TemplateCheckItem;
import com.taskboard.api.entity.TemplateLabel;
import com.taskboard.api.exception.ResourceNotFoundException;
import com.taskboard.api.repository.CardRepository;
import com.taskboard.api.repository.LabelRepository;
import com.taskboard.api.repository.TaskListRepository;
import com.taskboard.api.repository.TemplateRepository;
import com.taskboard.api.util.Texts;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateCommandService {

	private final TemplateRepository templateRepository;
	private final LabelRepository labelRepository;
	private final TaskListRepository taskListRepository;
	private final CardRepository cardRepository;

	public TemplateDto create(TemplateRequest request) {
		Template template = new Template();
		apply(template, request);
		return TemplateDto.from(templateRepository.save(template));
	}

	// 全項目を置き換える。ラベルとチェック項目は指定した内容に作り直す
	public TemplateDto update(UUID id, TemplateRequest request) {
		Template template = find(id);
		template.getLabels().clear();
		template.getChecklist().clear();
		// 削除を先に DB へ反映してから、同じラベルの紐付けを作り直す（主キー重複を避ける）
		templateRepository.flush();
		apply(template, request);
		return TemplateDto.from(template);
	}

	// 作成済みのカードには影響しない
	public void delete(UUID id) {
		templateRepository.delete(find(id));
	}

	// 内容をコピーした新しいカードを、指定リストの末尾に作る。期限は設定しない。チェック項目は未完了
	public CardDto createCard(UUID id, UUID listId) {
		Template template = find(id);
		TaskList list = taskListRepository.findById(listId)
				.orElseThrow(() -> new ResourceNotFoundException("List not found: " + listId));
		Card card = new Card();
		card.setList(list);
		card.setTitle(template.getTitle());
		card.setDescription(template.getDescription());
		card.setPriority(template.getPriority());
		card.setWarnDays(template.getWarnDays());
		card.setPosition(cardRepository.countByListId(listId));
		for (TemplateLabel templateLabel : template.getLabels()) {
			card.getLabels().add(new CardLabel(card, templateLabel.getLabel()));
		}
		for (TemplateCheckItem item : template.getChecklist()) {
			CheckItem copy = new CheckItem();
			copy.setCard(card);
			copy.setText(item.getText());
			copy.setDone(false);
			copy.setPosition(item.getPosition());
			card.getChecklist().add(copy);
		}
		return CardDto.from(cardRepository.save(card));
	}

	private void apply(Template template, TemplateRequest request) {
		template.setTitle(request.title().strip());
		template.setDescription(Texts.blankToNull(request.description()));
		template.setPriority(request.priority());
		template.setWarnDays(request.warnDays() == null ? Card.DEFAULT_WARN_DAYS : request.warnDays());
		Set<UUID> labelIds = request.labelIds() == null ? Set.of() : request.labelIds();
		List<Label> labels = labelRepository.findAllById(labelIds);
		if (labels.size() != labelIds.size()) {
			throw new ResourceNotFoundException("Label not found in: " + labelIds);
		}
		labels.forEach(label -> template.getLabels().add(new TemplateLabel(template, label)));
		List<String> texts = request.checklist() == null ? List.of() : request.checklist();
		for (int i = 0; i < texts.size(); i++) {
			TemplateCheckItem item = new TemplateCheckItem();
			item.setTemplate(template);
			item.setText(texts.get(i).strip());
			item.setPosition(i);
			template.getChecklist().add(item);
		}
	}

	private Template find(UUID id) {
		return templateRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
	}
}
