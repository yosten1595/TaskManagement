package com.taskboard.api.service;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.dto.CreateCardRequest;
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
