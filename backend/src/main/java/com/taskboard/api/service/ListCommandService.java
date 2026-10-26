package com.taskboard.api.service;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.CardSortKey;
import com.taskboard.api.exception.ResourceNotFoundException;
import com.taskboard.api.repository.CardRepository;
import com.taskboard.api.repository.TaskListRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ListCommandService {

	// 期限・優先度が未設定のカードは常に末尾
	private static final Comparator<LocalDate> DUE_DATE_ORDER = Comparator.nullsLast(Comparator.naturalOrder());

	private static final Comparator<Card> BY_DUE_DATE = Comparator.comparing(Card::getDueDate, DUE_DATE_ORDER);

	// Priority は HIGH, MID, LOW の宣言順なので、自然順がそのまま「高い順」になる
	private static final Comparator<Card> BY_PRIORITY = Comparator
			.comparing(Card::getPriority, Comparator.nullsLast(Comparator.naturalOrder()))
			.thenComparing(BY_DUE_DATE);

	private final CardRepository cardRepository;
	private final TaskListRepository taskListRepository;

	// 並べ替えた結果を position に 0 から連番で保存する。完全に同順位のカードは現在の並びを保つ（安定ソート）
	public List<CardDto> sort(UUID listId, CardSortKey key) {
		if (!taskListRepository.existsById(listId)) {
			throw new ResourceNotFoundException("List not found: " + listId);
		}
		List<Card> cards = new ArrayList<>(cardRepository.findByListIdOrderByPosition(listId));
		cards.sort(key == CardSortKey.DUE_DATE ? BY_DUE_DATE : BY_PRIORITY);
		for (int i = 0; i < cards.size(); i++) {
			cards.get(i).setPosition(i);
		}
		return cards.stream().map(CardDto::from).toList();
	}
}
