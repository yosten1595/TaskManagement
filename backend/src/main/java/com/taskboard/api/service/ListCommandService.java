package com.taskboard.api.service;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.dto.ListDto;
import com.taskboard.api.dto.ListRequest;
import com.taskboard.api.entity.Board;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.CardSortKey;
import com.taskboard.api.entity.TaskList;
import com.taskboard.api.repository.BoardRepository;
import com.taskboard.api.repository.CardRepository;
import com.taskboard.api.repository.TaskListRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
	private final BoardRepository boardRepository;

	// 追加したリストはボードの末尾に置く
	public ListDto create(UUID boardId, ListRequest request) {
		Board board = boardRepository.findById(boardId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found: " + boardId));
		TaskList list = new TaskList();
		list.setBoard(board);
		list.setTitle(request.title().strip());
		list.setPosition(taskListRepository.countByBoardId(boardId));
		return ListDto.from(taskListRepository.save(list));
	}

	public ListDto rename(UUID id, ListRequest request) {
		TaskList list = find(id);
		list.setTitle(request.title().strip());
		return ListDto.from(list);
	}

	// 配下のカード・チェック項目も削除され、残りのリストの position は 0 から連番に詰め直す
	public void delete(UUID id) {
		TaskList list = find(id);
		UUID boardId = list.getBoard().getId();
		taskListRepository.delete(list);
		taskListRepository.flush();
		List<TaskList> rest = taskListRepository.findByBoardIdOrderByPosition(boardId);
		for (int i = 0; i < rest.size(); i++) {
			rest.get(i).setPosition(i);
		}
	}

	private TaskList find(UUID id) {
		return taskListRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found: " + id));
	}

	// 並べ替えた結果を position に 0 から連番で保存する。完全に同順位のカードは現在の並びを保つ（安定ソート）
	public List<CardDto> sort(UUID listId, CardSortKey key) {
		if (!taskListRepository.existsById(listId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found: " + listId);
		}
		List<Card> cards = new ArrayList<>(cardRepository.findByListIdOrderByPosition(listId));
		cards.sort(key == CardSortKey.DUE_DATE ? BY_DUE_DATE : BY_PRIORITY);
		for (int i = 0; i < cards.size(); i++) {
			cards.get(i).setPosition(i);
		}
		return cards.stream().map(CardDto::from).toList();
	}
}
