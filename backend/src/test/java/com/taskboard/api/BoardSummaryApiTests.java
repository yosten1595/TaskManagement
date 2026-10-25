package com.taskboard.api;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taskboard.api.repository.BoardRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// ボード一覧に含まれる、カード数と期限別の件数（ホーム画面のタイルのバッジ用）を検証する。専用ボードを作って終了後に削除する
@SpringBootTest
@AutoConfigureMockMvc
class BoardSummaryApiTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	BoardRepository boardRepository;

	TestBoardFixture fixture;

	@BeforeEach
	void createBoard() {
		fixture = TestBoardFixture.create(boardRepository, "未着手", "完了");
	}

	@AfterEach
	void deleteBoard() {
		fixture.delete();
	}

	@Test
	void countsCardsAndDueStatesExcludingDoneList() throws Exception {
		LocalDate today = LocalDate.now();
		String todo = fixture.listIds().get(0);
		String done = fixture.listIds().get(1);
		createCard(todo, "期限切れ", today.minusDays(1), null);
		createCard(todo, "期限当日", today, null);
		createCard(todo, "警告日数内", today.plusDays(3), null);
		createCard(todo, "警告日数を広げて内", today.plusDays(7), 7);
		createCard(todo, "余裕あり", today.plusDays(10), null);
		createCard(todo, "期限なし", null, null);
		createCard(done, "完了の期限切れ", today.minusDays(5), null);

		String path = "$[?(@.id=='" + fixture.boardId() + "')]";
		mockMvc.perform(get("/api/boards"))
				.andExpect(status().isOk())
				.andExpect(jsonPath(path + ".cardCount", contains(7)))
				.andExpect(jsonPath(path + ".overdueCount", contains(1)))
				.andExpect(jsonPath(path + ".soonCount", contains(3)));
	}

	@Test
	void emptyBoardHasZeroCounts() throws Exception {
		String path = "$[?(@.id=='" + fixture.boardId() + "')]";
		mockMvc.perform(get("/api/boards"))
				.andExpect(jsonPath(path + ".cardCount", contains(0)))
				.andExpect(jsonPath(path + ".overdueCount", contains(0)))
				.andExpect(jsonPath(path + ".soonCount", contains(0)));
	}

	private void createCard(String listId, String title, LocalDate dueDate, Integer warnDays) throws Exception {
		String json = "{\"listId\":\"" + listId + "\",\"title\":\"" + title + "\""
				+ (dueDate == null ? "" : ",\"dueDate\":\"" + dueDate + "\"")
				+ (warnDays == null ? "" : ",\"warnDays\":" + warnDays) + "}";
		mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isCreated());
	}
}
