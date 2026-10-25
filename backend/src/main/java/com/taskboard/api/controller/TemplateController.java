package com.taskboard.api.controller;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.dto.CreateCardFromTemplateRequest;
import com.taskboard.api.dto.TemplateDto;
import com.taskboard.api.dto.TemplateRequest;
import com.taskboard.api.service.TemplateCommandService;
import com.taskboard.api.service.TemplateQueryService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

	private final TemplateQueryService templateQueryService;
	private final TemplateCommandService templateCommandService;

	@GetMapping
	public List<TemplateDto> list() {
		return templateQueryService.findAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TemplateDto create(@Valid @RequestBody TemplateRequest request) {
		return templateCommandService.create(request);
	}

	@PutMapping("/{id}")
	public TemplateDto update(@PathVariable UUID id, @Valid @RequestBody TemplateRequest request) {
		return templateCommandService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		templateCommandService.delete(id);
	}

	// テンプレートの内容をコピーして、指定リストに新しいカードを作る
	@PostMapping("/{id}/cards")
	@ResponseStatus(HttpStatus.CREATED)
	public CardDto createCard(@PathVariable UUID id, @Valid @RequestBody CreateCardFromTemplateRequest request) {
		return templateCommandService.createCard(id, request.listId());
	}
}
