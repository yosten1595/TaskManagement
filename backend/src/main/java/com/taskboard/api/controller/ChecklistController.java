package com.taskboard.api.controller;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.dto.CreateCheckItemRequest;
import com.taskboard.api.dto.UpdateCheckItemRequest;
import com.taskboard.api.service.ChecklistCommandService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards/{cardId}/checklist")
@RequiredArgsConstructor
public class ChecklistController {

	private final ChecklistCommandService checklistCommandService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CardDto add(@PathVariable UUID cardId, @Valid @RequestBody CreateCheckItemRequest request) {
		return checklistCommandService.add(cardId, request);
	}

	@PutMapping("/{itemId}")
	public CardDto update(
			@PathVariable UUID cardId,
			@PathVariable UUID itemId,
			@Valid @RequestBody UpdateCheckItemRequest request) {
		return checklistCommandService.update(cardId, itemId, request);
	}

	@DeleteMapping("/{itemId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID cardId, @PathVariable UUID itemId) {
		checklistCommandService.delete(cardId, itemId);
	}
}
