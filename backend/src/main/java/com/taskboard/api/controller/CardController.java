package com.taskboard.api.controller;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.dto.CreateCardRequest;
import com.taskboard.api.dto.MoveCardRequest;
import com.taskboard.api.dto.UpdateCardRequest;
import com.taskboard.api.entity.Priority;
import com.taskboard.api.service.CardCommandService;
import com.taskboard.api.service.CardQueryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

	private final CardQueryService cardQueryService;
	private final CardCommandService cardCommandService;

	@GetMapping
	public List<CardDto> search(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Priority priority,
			@RequestParam(required = false) UUID listId,
			@RequestParam(required = false) UUID labelId,
			@RequestParam(required = false) UUID boardId) {
		return cardQueryService.search(keyword, priority, listId, labelId, boardId);
	}

	@GetMapping("/{id}")
	public CardDto get(@PathVariable UUID id) {
		return cardQueryService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CardDto create(@Valid @RequestBody CreateCardRequest request) {
		return cardCommandService.create(request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		cardCommandService.delete(id);
	}

	@PutMapping("/{id}")
	public CardDto update(@PathVariable UUID id, @Valid @RequestBody UpdateCardRequest request) {
		return cardCommandService.update(id, request);
	}

	@PutMapping("/{id}/move")
	public CardDto move(@PathVariable UUID id, @Valid @RequestBody MoveCardRequest request) {
		return cardCommandService.move(id, request);
	}
}
