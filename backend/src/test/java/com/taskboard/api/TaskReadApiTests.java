package com.taskboard.api;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

// docker-compose の PostgreSQL に接続し、TestDataSeeder が投入したデータを API 経由で検証する
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.seed.enabled=true")
class TaskReadApiTests {

	@Autowired
	MockMvc mockMvc;

	@Test
	void listBoards() throws Exception {
		mockMvc.perform(get("/api/boards"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$[*].title", hasItem("サンプルプロジェクト")));
	}

	@Test
	void boardDetailContainsListsAndCards() throws Exception {
		String id = firstBoardId();
		mockMvc.perform(get("/api/boards/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lists[0].title", is("未着手")))
				.andExpect(jsonPath("$.lists[0].cards[0].labels[0].name", is("バックエンド")))
				.andExpect(jsonPath("$.lists[0].cards[0].checklist", hasSize(3)));
	}

	@Test
	void searchCardsByKeywordAndPriority() throws Exception {
		mockMvc.perform(get("/api/cards").param("keyword", "docker"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].title", hasItem("PostgreSQL を Docker で構築する")));
		mockMvc.perform(get("/api/cards").param("priority", "HIGH"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].priority", hasItem("HIGH")));
	}

	@Test
	void unknownIdsReturn404() throws Exception {
		String unknown = "00000000-0000-0000-0000-000000000000";
		mockMvc.perform(get("/api/boards/" + unknown)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/cards/" + unknown)).andExpect(status().isNotFound());
	}

	private String firstBoardId() throws Exception {
		String json = mockMvc.perform(get("/api/boards")).andReturn().getResponse().getContentAsString();
		return com.jayway.jsonpath.JsonPath.read(json, "$[0].id");
	}
}
