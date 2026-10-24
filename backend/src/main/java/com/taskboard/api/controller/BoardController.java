package com.taskboard.api.controller;

import com.taskboard.api.dto.BoardDetailDto;
import com.taskboard.api.dto.BoardRequest;
import com.taskboard.api.dto.BoardSummaryDto;
import com.taskboard.api.service.BoardCommandService;
import com.taskboard.api.service.BoardQueryService;
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
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

	private final BoardQueryService boardQueryService;
	private final BoardCommandService boardCommandService;

	@GetMapping
	public List<BoardSummaryDto> list() {
		return boardQueryService.findAll();
	}

	@GetMapping("/{id}")
	public BoardDetailDto get(@PathVariable UUID id) {
		return boardQueryService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BoardSummaryDto create(@Valid @RequestBody BoardRequest request) {
		return boardCommandService.create(request);
	}

	@PutMapping("/{id}")
	public BoardSummaryDto rename(@PathVariable UUID id, @Valid @RequestBody BoardRequest request) {
		return boardCommandService.rename(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		boardCommandService.delete(id);
	}
}
