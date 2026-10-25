package com.taskboard.api.service;

import com.taskboard.api.dto.BoardRequest;
import com.taskboard.api.dto.BoardSummaryDto;
import com.taskboard.api.entity.Board;
import com.taskboard.api.entity.TaskList;
import com.taskboard.api.repository.BoardRepository;
import java.time.LocalDate;
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
public class BoardCommandService {

	// 要件定義書 3.2 の初期リスト
	private static final List<String> DEFAULT_LIST_TITLES = List.of("未着手", "進行中", "完了");

	private final BoardRepository boardRepository;

	public BoardSummaryDto create(BoardRequest request) {
		Board board = new Board();
		board.setTitle(request.title().strip());
		for (int i = 0; i < DEFAULT_LIST_TITLES.size(); i++) {
			TaskList list = new TaskList();
			list.setBoard(board);
			list.setTitle(DEFAULT_LIST_TITLES.get(i));
			list.setPosition(i);
			board.getLists().add(list);
		}
		return BoardSummaryDto.from(boardRepository.save(board), LocalDate.now());
	}

	public BoardSummaryDto rename(UUID id, BoardRequest request) {
		Board board = find(id);
		board.setTitle(request.title().strip());
		return BoardSummaryDto.from(board, LocalDate.now());
	}

	// リスト・カード・チェック項目・ラベルの紐付けは、カスケードで一緒に削除される
	public void delete(UUID id) {
		boardRepository.delete(find(id));
	}

	private Board find(UUID id) {
		return boardRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found: " + id));
	}
}
