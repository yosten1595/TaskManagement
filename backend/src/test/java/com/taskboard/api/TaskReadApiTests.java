package com.taskboard.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taskboard.api.entity.Board;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.CardLabel;
import com.taskboard.api.entity.CheckItem;
import com.taskboard.api.entity.Label;
import com.taskboard.api.entity.Priority;
import com.taskboard.api.entity.TaskList;
import com.taskboard.api.repository.BoardRepository;
import com.taskboard.api.repository.LabelRepository;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

// 専用のボード・カード・ラベルを作って読み取り API を検証する（開発用データに依存しない）。終了後に削除する
@SpringBootTest
@AutoConfigureMockMvc
class TaskReadApiTests {

	// 検索で他のカードに当たらないよう、このテスト専用の語を使う
	private static final String KEYWORD = "読み取りテスト専用語";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	BoardRepository boardRepository;

	@Autowired
	LabelRepository labelRepository;

	UUID boardId;
	UUID labelId;
	String labelName;
	String cardId;

	@BeforeEach
	void createData() {
		Label label = new Label();
		labelName = "読み取りテスト用ラベル";
		label.setName(labelName);
		label.setColor("#123456");
		label = labelRepository.save(label);
		labelId = label.getId();

		Board board = new Board();
		board.setTitle("読み取りテスト用ボード");
		TaskList list = new TaskList();
		list.setBoard(board);
		list.setTitle("読み取りテスト用リスト");
		list.setPosition(0);
		board.getLists().add(list);

		Card card = new Card();
		card.setList(list);
		card.setTitle("Docker で " + KEYWORD);
		card.setPriority(Priority.HIGH);
		card.setPosition(0);
		card.getLabels().add(new CardLabel(card, label));
		for (int i = 0; i < 3; i++) {
			CheckItem item = new CheckItem();
			item.setCard(card);
			item.setText("項目" + i);
			item.setPosition(i);
			card.getChecklist().add(item);
		}
		list.getCards().add(card);

		Board saved = boardRepository.save(board);
		boardId = saved.getId();
		cardId = saved.getLists().get(0).getCards().get(0).getId().toString();
	}

	@AfterEach
	void deleteData() {
		boardRepository.deleteById(boardId);
		labelRepository.deleteById(labelId);
	}

	@Test
	void listBoards() throws Exception {
		mockMvc.perform(get("/api/boards"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].id", hasItem(boardId.toString())))
				.andExpect(jsonPath("$[*].title", hasItem("読み取りテスト用ボード")));
	}

	@Test
	void boardDetailContainsListsAndCards() throws Exception {
		mockMvc.perform(get("/api/boards/" + boardId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lists", hasSize(1)))
				.andExpect(jsonPath("$.lists[0].title", is("読み取りテスト用リスト")))
				.andExpect(jsonPath("$.lists[0].cards[0].id", is(cardId)))
				.andExpect(jsonPath("$.lists[0].cards[0].labels[*].name", contains(labelName)))
				.andExpect(jsonPath("$.lists[0].cards[0].checklist", hasSize(3)));
	}

	@Test
	void searchCardsByKeywordAndPriority() throws Exception {
		mockMvc.perform(get("/api/cards").param("keyword", KEYWORD.toLowerCase()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].id", contains(cardId)));
		mockMvc.perform(get("/api/cards").param("keyword", "docker").param("priority", "HIGH"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].id", hasItem(cardId)));
		mockMvc.perform(get("/api/cards").param("keyword", KEYWORD).param("priority", "LOW"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void searchCardsByLabel() throws Exception {
		mockMvc.perform(get("/api/cards").param("labelId", labelId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].id", contains(cardId)));
		mockMvc.perform(get("/api/cards").param("labelId", labelId.toString()).param("priority", "LOW"))
				.andExpect(jsonPath("$", hasSize(0)));
		mockMvc.perform(get("/api/cards").param("labelId", "00000000-0000-0000-0000-000000000000"))
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void unknownIdsReturn404() throws Exception {
		String unknown = "00000000-0000-0000-0000-000000000000";
		mockMvc.perform(get("/api/boards/" + unknown)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/cards/" + unknown)).andExpect(status().isNotFound());
	}
}
