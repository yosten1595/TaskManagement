package com.taskboard.api.service;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.dto.CreateCardRequest;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.TaskList;
import com.taskboard.api.repository.CardRepository;
import com.taskboard.api.repository.TaskListRepository;
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

	// 作成したカードは同一リストの末尾に置く
	public CardDto create(CreateCardRequest request) {
		TaskList list = taskListRepository.findById(request.listId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found: " + request.listId()));
		Card card = new Card();
		card.setList(list);
		card.setTitle(request.title().strip());
		card.setPosition(cardRepository.countByListId(list.getId()));
		return CardDto.from(cardRepository.save(card));
	}
}
