package com.taskboard.api.service;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.dto.CreateCardRequest;
import com.taskboard.api.dto.MoveCardRequest;
import com.taskboard.api.dto.UpdateCardRequest;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.CardLabel;
import com.taskboard.api.entity.CheckItem;
import com.taskboard.api.entity.Label;
import com.taskboard.api.entity.TaskList;
import com.taskboard.api.exception.ResourceNotFoundException;
import com.taskboard.api.repository.CardRepository;
import com.taskboard.api.repository.LabelRepository;
import com.taskboard.api.repository.TaskListRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CardCommandService {

	// 要件定義書 3.4: 警告日数の既定値
	private static final int DEFAULT_WARN_DAYS = 3;

	private final CardRepository cardRepository;
	private final TaskListRepository taskListRepository;
	private final LabelRepository labelRepository;

	// 作成したカードは、position を指定しなければ同一リストの末尾に置く。指定したときは、その位置に挿入して、後ろのカードを 1 つずつずらす
	public CardDto create(CreateCardRequest request) {
		TaskList list = taskListRepository.findById(request.listId())
				.orElseThrow(() -> new ResourceNotFoundException("List not found: " + request.listId()));
		Card card = new Card();
		card.setList(list);
		card.setTitle(request.title().strip());
		card.setDueDate(request.dueDate());
		card.setPriority(request.priority());
		if (request.warnDays() != null) {
			card.setWarnDays(request.warnDays());
		}
		card.setDescription(request.description() == null || request.description().isBlank() ? null : request.description());
		int count = cardRepository.countByListId(list.getId());
		int position = request.position() == null ? count : Math.min(request.position(), count);
		if (position < count) {
			cardRepository.findByListIdOrderByPosition(list.getId()).stream()
					.filter(other -> other.getPosition() >= position)
					.forEach(other -> other.setPosition(other.getPosition() + 1));
		}
		card.setPosition(position);
		attachLabels(card, request.labelIds());
		List<CreateCardRequest.CheckItem> items = request.checklist() == null ? List.of() : request.checklist();
		for (int i = 0; i < items.size(); i++) {
			CheckItem item = new CheckItem();
			item.setCard(card);
			item.setText(items.get(i).text().strip());
			item.setDone(items.get(i).done());
			item.setPosition(i);
			card.getChecklist().add(item);
		}
		return CardDto.from(cardRepository.save(card));
	}

	// 所属リストと position は変えない（移動は別機能）。ラベルは指定した集合に置き換える
	public CardDto update(UUID id, UpdateCardRequest request) {
		Card card = cardRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Card not found: " + id));
		Set<UUID> labelIds = request.labelIds() == null ? Set.of() : request.labelIds();
		List<Label> labels = labelRepository.findAllById(labelIds);
		if (labels.size() != labelIds.size()) {
			throw new ResourceNotFoundException("Label not found in: " + labelIds);
		}
		card.setTitle(request.title().strip());
		card.setDescription(request.description() == null || request.description().isBlank() ? null : request.description());
		card.setDueDate(request.dueDate());
		card.setPriority(request.priority());
		card.setWarnDays(request.warnDays() == null ? DEFAULT_WARN_DAYS : request.warnDays());
		card.getLabels().removeIf(cl -> !labelIds.contains(cl.getLabel().getId()));
		Set<UUID> attached = card.getLabels().stream().map(cl -> cl.getLabel().getId()).collect(Collectors.toSet());
		labels.stream()
				.filter(label -> !attached.contains(label.getId()))
				.forEach(label -> card.getLabels().add(new CardLabel(card, label)));
		return CardDto.from(card);
	}

	// 移動先リストの指定位置に挿入し、移動元・移動先の position を 0 から連番に振り直す
	public CardDto move(UUID id, MoveCardRequest request) {
		Card card = cardRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Card not found: " + id));
		TaskList target = taskListRepository.findById(request.listId())
				.orElseThrow(() -> new ResourceNotFoundException("List not found: " + request.listId()));
		UUID sourceId = card.getList().getId();

		List<Card> targetCards = new ArrayList<>(cardRepository.findByListIdOrderByPosition(target.getId()));
		targetCards.removeIf(c -> c.getId().equals(id));
		int index = request.position() == null ? targetCards.size() : Math.min(request.position(), targetCards.size());
		card.setList(target);
		targetCards.add(index, card);
		renumber(targetCards);

		if (!sourceId.equals(target.getId())) {
			List<Card> sourceCards = new ArrayList<>(cardRepository.findByListIdOrderByPosition(sourceId));
			sourceCards.removeIf(c -> c.getId().equals(id));
			renumber(sourceCards);
		}
		return CardDto.from(card);
	}

	private void renumber(List<Card> cards) {
		for (int i = 0; i < cards.size(); i++) {
			cards.get(i).setPosition(i);
		}
	}

	// 削除後は同一リストの position を 0 から連番に詰め直し、末尾追加時の重複を防ぐ
	public void delete(UUID id) {
		Card card = cardRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Card not found: " + id));
		UUID listId = card.getList().getId();
		cardRepository.delete(card);
		cardRepository.flush();
		renumber(cardRepository.findByListIdOrderByPosition(listId));
	}

	// 指定された ID のラベルが 1 件でも存在しなければ 404
	private void attachLabels(Card card, Set<UUID> labelIds) {
		if (labelIds == null || labelIds.isEmpty()) {
			return;
		}
		List<Label> labels = labelRepository.findAllById(labelIds);
		if (labels.size() != labelIds.size()) {
			throw new ResourceNotFoundException("Label not found in: " + labelIds);
		}
		labels.forEach(label -> card.getLabels().add(new CardLabel(card, label)));
	}
}
