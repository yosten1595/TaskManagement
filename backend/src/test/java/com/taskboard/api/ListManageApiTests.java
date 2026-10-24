package com.taskboard.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.taskboard.api.repository.BoardRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

// 専用のボード（リスト A・B）を作って、リストの追加・名前変更・削除を検証する。終了後にボードごと削除する
@SpringBootTest
@AutoConfigureMockMvc
class ListManageApiTests {

	private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	BoardRepository boardRepository;

	TestBoardFixture fixture;

	@BeforeEach
	void createBoard() {
		fixture = TestBoardFixture.create(boardRepository, "A", "B");
	}

	@AfterEach
	void deleteBoard() {
		fixture.delete();
	}

	@Test
	void addAppendsListToEndOfBoard() throws Exception {
		addList(fixture.boardId(), "  C  ")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title", is("C")))
				.andExpect(jsonPath("$.position", is(2)))
				.andExpect(jsonPath("$.cards.length()", is(0)));

		assertListTitles("A", "B", "C");
		mockMvc.perform(get("/api/boards/" + fixture.boardId()))
				.andExpect(jsonPath("$.lists[*].position", contains(0, 1, 2)));
	}

	@Test
	void renameChangesOnlyTheTitle() throws Exception {
		String cardId = createCard(fixture.listIds().get(0), "残るカード");

		mockMvc.perform(put("/api/lists/" + fixture.listIds().get(0)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\" 改名 \"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(fixture.listIds().get(0))))
				.andExpect(jsonPath("$.title", is("改名")))
				.andExpect(jsonPath("$.position", is(0)))
				.andExpect(jsonPath("$.cards[0].id", is(cardId)));

		assertListTitles("改名", "B");
	}

	@Test
	void deleteRemovesListCardsAndCompactsPositions() throws Exception {
		String listC = idOf(addList(fixture.boardId(), "C"));
		String first = fixture.listIds().get(0);
		String cardInFirst = createCard(first, "消えるカード");
		String cardInB = createCard(fixture.listIds().get(1), "残るカード");
		mockMvc.perform(post("/api/cards/" + cardInFirst + "/checklist").contentType(MediaType.APPLICATION_JSON)
				.content("{\"text\":\"消える項目\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(delete("/api/lists/" + first)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/cards/" + cardInFirst)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/cards/" + cardInB)).andExpect(status().isOk());
		assertListTitles("B", "C");
		mockMvc.perform(get("/api/boards/" + fixture.boardId()))
				.andExpect(jsonPath("$.lists[*].position", contains(0, 1)))
				.andExpect(jsonPath("$.lists[1].id", is(listC)));

		// 削除後に追加したリストの position が既存リストと重複しない
		addList(fixture.boardId(), "D").andExpect(jsonPath("$.position", is(2)));
	}

	@Test
	void blankTitleReturns400() throws Exception {
		addList(fixture.boardId(), "   ").andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/boards/" + fixture.boardId() + "/lists")
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(put("/api/lists/" + fixture.listIds().get(0)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"\"}"))
				.andExpect(status().isBadRequest());

		assertListTitles("A", "B");
	}

	@Test
	void unknownBoardOrListReturns404() throws Exception {
		addList(UNKNOWN_ID, "x").andExpect(status().isNotFound());
		mockMvc.perform(put("/api/lists/" + UNKNOWN_ID).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"x\"}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/lists/" + UNKNOWN_ID)).andExpect(status().isNotFound());
	}

	private ResultActions addList(String boardId, String title) throws Exception {
		return mockMvc.perform(post("/api/boards/" + boardId + "/lists").contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"" + title + "\"}"));
	}

	private String idOf(ResultActions result) throws Exception {
		return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
	}

	private String createCard(String listId, String title) throws Exception {
		String json = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\",\"title\":\"" + title + "\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(json, "$.id");
	}

	private void assertListTitles(String... titles) throws Exception {
		mockMvc.perform(get("/api/boards/" + fixture.boardId()))
				.andExpect(jsonPath("$.lists[*].title", contains(titles)));
	}
}
