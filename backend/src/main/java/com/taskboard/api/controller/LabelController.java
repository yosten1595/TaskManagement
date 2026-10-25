package com.taskboard.api.controller;

import com.taskboard.api.dto.LabelDto;
import com.taskboard.api.dto.LabelRequest;
import com.taskboard.api.service.LabelCommandService;
import com.taskboard.api.service.LabelQueryService;
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
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelController {

	private final LabelQueryService labelQueryService;
	private final LabelCommandService labelCommandService;

	@GetMapping
	public List<LabelDto> list() {
		return labelQueryService.findAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public LabelDto create(@Valid @RequestBody LabelRequest request) {
		return labelCommandService.create(request);
	}

	@PutMapping("/{id}")
	public LabelDto update(@PathVariable UUID id, @Valid @RequestBody LabelRequest request) {
		return labelCommandService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		labelCommandService.delete(id);
	}
}
