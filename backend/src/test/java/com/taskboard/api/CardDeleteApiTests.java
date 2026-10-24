package com.taskboard.api;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

// docker-compose の PostgreSQL に接続し、専用のボードとリストを作って検証する（開発用データに依存しない）。終了後にボードごと削除する
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.seed.enabled=true")
class CardDeleteApiTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	BoardRepository boardRepository;

	TestBoardFixture fixture;

	@BeforeEach
	void createBoard() {
		fixture = TestBoardFixture.create(boardRepository, "リスト");
	}

	@AfterEach
	void deleteBoard() {
		fixture.delete();
	}

	@Test
	void deleteRemovesCardAndCompactsPositions() throws Exception {
		String listId = firstListId();
		String c1 = create(listId, "削除対象");
		String c2 = create(listId, "残る1");
		String c3 = create(listId, "残る2");
		int base = position(c1);

		mockMvc.perform(delete("/api/cards/" + c1)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/cards/" + c1)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/cards/" + c2)).andExpect(jsonPath("$.position", is(base)));
		mockMvc.perform(get("/api/cards/" + c3)).andExpect(jsonPath("$.position", is(base + 1)));

		// 削除後に作成したカードの position が既存カードと重複しない
		String c4 = create(listId, "新規");
		mockMvc.perform(get("/api/cards/" + c4)).andExpect(jsonPath("$.position", is(base + 2)));
	}

	@Test
	void deleteUnknownCardReturns404() throws Exception {
		mockMvc.perform(delete("/api/cards/00000000-0000-0000-0000-000000000000"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteCardWithLabels() throws Exception {
		String listId = firstListId();
		String labels = mockMvc.perform(get("/api/labels")).andReturn().getResponse().getContentAsString();
		String labelId = JsonPath.read(labels, "$[0].id");
		String created = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\",\"title\":\"ラベル付き\",\"labelIds\":[\"" + labelId + "\"]}"))
				.andReturn().getResponse().getContentAsString();
		String id = JsonPath.read(created, "$.id");
		mockMvc.perform(delete("/api/cards/" + id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/cards/" + id)).andExpect(status().isNotFound());
	}

	private String create(String listId, String title) throws Exception {
		String json = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\",\"title\":\"" + title + "\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(json, "$.id");
	}

	private int position(String cardId) throws Exception {
		String json = mockMvc.perform(get("/api/cards/" + cardId)).andReturn().getResponse().getContentAsString();
		return JsonPath.read(json, "$.position");
	}

	private String firstListId() {
		return fixture.firstListId();
	}
}
