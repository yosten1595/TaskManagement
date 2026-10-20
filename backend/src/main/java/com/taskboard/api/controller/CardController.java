package com.taskboard.api.controller;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.entity.Priority;
import com.taskboard.api.service.CardQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

	private final CardQueryService cardQueryService;

	@GetMapping
	public List<CardDto> search(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Priority priority,
			@RequestParam(required = false) UUID listId) {
		return cardQueryService.search(keyword, priority, listId);
	}

	@GetMapping("/{id}")
	public CardDto get(@PathVariable UUID id) {
		return cardQueryService.findById(id);
	}
}
