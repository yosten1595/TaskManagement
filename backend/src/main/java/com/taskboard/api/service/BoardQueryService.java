package com.taskboard.api.service;

import com.taskboard.api.dto.BoardDetailDto;
import com.taskboard.api.dto.BoardSummaryDto;
import com.taskboard.api.repository.BoardRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardQueryService {

	private final BoardRepository boardRepository;

	public List<BoardSummaryDto> findAll() {
		return boardRepository.findAllByOrderByCreatedAtAsc().stream().map(BoardSummaryDto::from).toList();
	}

	public BoardDetailDto findById(UUID id) {
		return boardRepository.findById(id)
				.map(BoardDetailDto::from)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found: " + id));
	}
}
