package com.taskboard.api;

import com.taskboard.api.entity.Board;
import com.taskboard.api.entity.TaskList;
import com.taskboard.api.repository.BoardRepository;
import java.util.List;
import java.util.UUID;

// テスト専用のボードとリストを作り、終了後にボードごと削除する。開発用データを読み書きせず、汚さないために使う
final class TestBoardFixture {

	private final BoardRepository boardRepository;
	private final UUID boardId;
	private final List<String> listIds;

	private TestBoardFixture(BoardRepository boardRepository, UUID boardId, List<String> listIds) {
		this.boardRepository = boardRepository;
		this.boardId = boardId;
		this.listIds = listIds;
	}

	static TestBoardFixture create(BoardRepository boardRepository, String... listTitles) {
		Board board = new Board();
		board.setTitle("APIテスト用");
		for (int i = 0; i < listTitles.length; i++) {
			TaskList list = new TaskList();
			list.setBoard(board);
			list.setTitle(listTitles[i]);
			list.setPosition(i);
			board.getLists().add(list);
		}
		Board saved = boardRepository.save(board);
		List<String> ids = saved.getLists().stream().map(l -> l.getId().toString()).toList();
		return new TestBoardFixture(boardRepository, saved.getId(), ids);
	}

	String boardId() {
		return boardId.toString();
	}

	List<String> listIds() {
		return listIds;
	}

	String firstListId() {
		return listIds.get(0);
	}

	void delete() {
		boardRepository.deleteById(boardId);
	}
}
