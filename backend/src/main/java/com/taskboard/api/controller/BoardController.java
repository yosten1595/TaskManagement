package com.taskboard.api.controller;

import com.taskboard.api.dto.BoardDetailDto;
import com.taskboard.api.dto.BoardSummaryDto;
import com.taskboard.api.service.BoardQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

	private final BoardQueryService boardQueryService;

	@GetMapping
	public List<BoardSummaryDto> list() {
		return boardQueryService.findAll();
	}

	@GetMapping("/{id}")
	public BoardDetailDto get(@PathVariable UUID id) {
		return boardQueryService.findById(id);
	}
}
