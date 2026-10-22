package com.taskboard.api;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

// docker-compose の PostgreSQL に接続し、TestDataSeeder が投入したリストにカードを作成して検証する
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.seed.enabled=true")
class CardCreateApiTests {

	@Autowired
	MockMvc mockMvc;

	@Test
	void createCardAppendsToEndOfList() throws Exception {
		String listId = firstListId();
		int before = JsonPath.<java.util.List<?>>read(
				mockMvc.perform(get("/api/cards").param("listId", listId)).andReturn().getResponse().getContentAsString(),
				"$").size();

		String created = mockMvc.perform(post("/api/cards")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\",\"title\":\"  新しいカード  \"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.title", is("新しいカード")))
				.andExpect(jsonPath("$.listId", is(listId)))
				.andExpect(jsonPath("$.position", is(before)))
				.andReturn().getResponse().getContentAsString();

		String id = JsonPath.read(created, "$.id");
		mockMvc.perform(get("/api/cards/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("新しいカード")));
	}

	@Test
	void blankOrMissingFieldsReturn400() throws Exception {
		String listId = firstListId();
		mockMvc.perform(post("/api/cards")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\",\"title\":\"   \"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/cards")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"タイトルのみ\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void unknownListReturns404() throws Exception {
		mockMvc.perform(post("/api/cards")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"00000000-0000-0000-0000-000000000000\",\"title\":\"x\"}"))
				.andExpect(status().isNotFound());
	}

	private String firstListId() throws Exception {
		String boards = mockMvc.perform(get("/api/boards")).andReturn().getResponse().getContentAsString();
		String boardId = JsonPath.read(boards, "$[0].id");
		String board = mockMvc.perform(get("/api/boards/" + boardId)).andReturn().getResponse().getContentAsString();
		return JsonPath.read(board, "$.lists[0].id");
	}
}
