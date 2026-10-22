package com.taskboard.api.service;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.dto.CreateCardRequest;
import com.taskboard.api.dto.UpdateCardRequest;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.CardLabel;
import com.taskboard.api.entity.Label;
import com.taskboard.api.entity.TaskList;
import com.taskboard.api.repository.CardRepository;
import com.taskboard.api.repository.LabelRepository;
import com.taskboard.api.repository.TaskListRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class CardCommandService {

	private final CardRepository cardRepository;
	private final TaskListRepository taskListRepository;
	private final LabelRepository labelRepository;

	// 作成したカードは同一リストの末尾に置く
	public CardDto create(CreateCardRequest request) {
		TaskList list = taskListRepository.findById(request.listId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found: " + request.listId()));
		Card card = new Card();
		card.setList(list);
		card.setTitle(request.title().strip());
		card.setDueDate(request.dueDate());
		card.setPriority(request.priority());
		card.setPosition(cardRepository.countByListId(list.getId()));
		attachLabels(card, request.labelIds());
		return CardDto.from(cardRepository.save(card));
	}

	// 所属リストと position は変えない（移動は別機能）。ラベルは指定した集合に置き換える
	public CardDto update(UUID id, UpdateCardRequest request) {
		Card card = cardRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found: " + id));
		Set<UUID> labelIds = request.labelIds() == null ? Set.of() : request.labelIds();
		List<Label> labels = labelRepository.findAllById(labelIds);
		if (labels.size() != labelIds.size()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Label not found in: " + labelIds);
		}
		card.setTitle(request.title().strip());
		card.setDescription(request.description() == null || request.description().isBlank() ? null : request.description());
		card.setDueDate(request.dueDate());
		card.setPriority(request.priority());
		card.getLabels().removeIf(cl -> !labelIds.contains(cl.getLabel().getId()));
		Set<UUID> attached = card.getLabels().stream().map(cl -> cl.getLabel().getId()).collect(Collectors.toSet());
		labels.stream()
				.filter(label -> !attached.contains(label.getId()))
				.forEach(label -> card.getLabels().add(new CardLabel(card, label)));
		return CardDto.from(card);
	}

	// 削除後は同一リストの position を 0 から連番に詰め直し、末尾追加時の重複を防ぐ
	public void delete(UUID id) {
		Card card = cardRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found: " + id));
		UUID listId = card.getList().getId();
		cardRepository.delete(card);
		cardRepository.flush();
		List<Card> rest = cardRepository.findByListIdOrderByPosition(listId);
		for (int i = 0; i < rest.size(); i++) {
			rest.get(i).setPosition(i);
		}
	}

	// 指定された ID のラベルが 1 件でも存在しなければ 404
	private void attachLabels(Card card, Set<UUID> labelIds) {
		if (labelIds == null || labelIds.isEmpty()) {
			return;
		}
		List<Label> labels = labelRepository.findAllById(labelIds);
		if (labels.size() != labelIds.size()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Label not found in: " + labelIds);
		}
		labels.forEach(label -> card.getLabels().add(new CardLabel(card, label)));
	}
}
