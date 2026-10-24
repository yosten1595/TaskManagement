package com.taskboard.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

// docker-compose の PostgreSQL に接続し、専用のボードとリストを作って検証する（開発用データに依存しない）。終了後にボードごと削除する
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.seed.enabled=true")
class CardUpdateApiTests {

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
	void updateReplacesFieldsAndKeepsListAndPosition() throws Exception {
		String listId = firstListId();
		String labels = mockMvc.perform(get("/api/labels")).andReturn().getResponse().getContentAsString();
		String label1 = JsonPath.read(labels, "$[0].id");
		String label2 = JsonPath.read(labels, "$[1].id");
		String created = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\",\"title\":\"編集前\",\"priority\":\"LOW\",\"labelIds\":[\"" + label1 + "\"]}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		String id = JsonPath.read(created, "$.id");
		int position = JsonPath.read(created, "$.position");

		mockMvc.perform(put("/api/cards/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"  編集後  \",\"description\":\"説明文\",\"dueDate\":\"2027-01-15\","
						+ "\"priority\":\"HIGH\",\"labelIds\":[\"" + label2 + "\"]}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("編集後")))
				.andExpect(jsonPath("$.description", is("説明文")))
				.andExpect(jsonPath("$.dueDate", is("2027-01-15")))
				.andExpect(jsonPath("$.priority", is("HIGH")))
				.andExpect(jsonPath("$.labels", hasSize(1)))
				.andExpect(jsonPath("$.labels[0].id", is(label2)))
				.andExpect(jsonPath("$.listId", is(listId)))
				.andExpect(jsonPath("$.position", is(position)));

		mockMvc.perform(get("/api/cards/" + id))
				.andExpect(jsonPath("$.title", is("編集後")))
				.andExpect(jsonPath("$.labels[0].id", is(label2)));
	}

	@Test
	void updateWithOmittedFieldsClearsThem() throws Exception {
		String listId = firstListId();
		String labels = mockMvc.perform(get("/api/labels")).andReturn().getResponse().getContentAsString();
		String label1 = JsonPath.read(labels, "$[0].id");
		String created = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\",\"title\":\"全部入り\",\"dueDate\":\"2027-01-01\","
						+ "\"priority\":\"MID\",\"labelIds\":[\"" + label1 + "\"]}"))
				.andReturn().getResponse().getContentAsString();
		String id = JsonPath.read(created, "$.id");

		mockMvc.perform(put("/api/cards/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"全部入り\",\"description\":\"  \",\"labelIds\":[]}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.description", nullValue()))
				.andExpect(jsonPath("$.dueDate", nullValue()))
				.andExpect(jsonPath("$.priority", nullValue()))
				.andExpect(jsonPath("$.labels", hasSize(0)));
	}

	@Test
	void invalidRequests() throws Exception {
		String listId = firstListId();
		String created = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\",\"title\":\"対象\"}"))
				.andReturn().getResponse().getContentAsString();
		String id = JsonPath.read(created, "$.id");
		String unknown = "00000000-0000-0000-0000-000000000000";

		mockMvc.perform(put("/api/cards/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"  \"}")).andExpect(status().isBadRequest());
		mockMvc.perform(put("/api/cards/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"x\",\"priority\":\"URGENT\"}")).andExpect(status().isBadRequest());
		mockMvc.perform(put("/api/cards/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"x\",\"dueDate\":\"2027/01/01\"}")).andExpect(status().isBadRequest());
		mockMvc.perform(put("/api/cards/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"x\",\"labelIds\":[\"" + unknown + "\"]}")).andExpect(status().isNotFound());
		mockMvc.perform(put("/api/cards/" + unknown).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"x\"}")).andExpect(status().isNotFound());
	}

	private String firstListId() {
		return fixture.firstListId();
	}
}
