package com.taskboard.api;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

// docker-compose の PostgreSQL に接続し、TestDataSeeder が投入したリストにカードを作ってチェックリストを操作する
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.seed.enabled=true")
class ChecklistApiTests {

	private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

	@Autowired
	MockMvc mockMvc;

	@Test
	void addAppendsItemsInOrder() throws Exception {
		String cardId = createCard();

		addItem(cardId, "  一つ目  ")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.checklist.length()", is(1)))
				.andExpect(jsonPath("$.checklist[0].text", is("一つ目")))
				.andExpect(jsonPath("$.checklist[0].done", is(false)))
				.andExpect(jsonPath("$.checklist[0].position", is(0)));
		addItem(cardId, "二つ目")
				.andExpect(jsonPath("$.checklist.length()", is(2)))
				.andExpect(jsonPath("$.checklist[1].text", is("二つ目")))
				.andExpect(jsonPath("$.checklist[1].position", is(1)));

		mockMvc.perform(get("/api/cards/" + cardId))
				.andExpect(jsonPath("$.checklist.length()", is(2)));
	}

	@Test
	void updateChangesTextAndDone() throws Exception {
		String cardId = createCard();
		String itemId = itemId(addItem(cardId, "変更前").andReturn().getResponse().getContentAsString(), 0);

		mockMvc.perform(put(itemUrl(cardId, itemId)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"text\":\"変更後\",\"done\":true}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.checklist[0].text", is("変更後")))
				.andExpect(jsonPath("$.checklist[0].done", is(true)));

		mockMvc.perform(get("/api/cards/" + cardId))
				.andExpect(jsonPath("$.checklist[0].done", is(true)));
	}

	@Test
	void deleteRemovesItemAndCompactsPositions() throws Exception {
		String cardId = createCard();
		addItem(cardId, "A");
		addItem(cardId, "B");
		String json = addItem(cardId, "C").andReturn().getResponse().getContentAsString();

		mockMvc.perform(delete(itemUrl(cardId, itemId(json, 0)))).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/cards/" + cardId))
				.andExpect(jsonPath("$.checklist.length()", is(2)))
				.andExpect(jsonPath("$.checklist[0].text", is("B")))
				.andExpect(jsonPath("$.checklist[0].position", is(0)))
				.andExpect(jsonPath("$.checklist[1].text", is("C")))
				.andExpect(jsonPath("$.checklist[1].position", is(1)));

		// 削除後に追加した項目の position が既存項目と重複しない
		addItem(cardId, "D").andExpect(jsonPath("$.checklist[2].position", is(2)));
	}

	@Test
	void blankTextReturns400() throws Exception {
		String cardId = createCard();
		String itemId = itemId(addItem(cardId, "項目").andReturn().getResponse().getContentAsString(), 0);

		addItem(cardId, "   ").andExpect(status().isBadRequest());
		mockMvc.perform(put(itemUrl(cardId, itemId)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"text\":\"\",\"done\":false}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(put(itemUrl(cardId, itemId)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"text\":\"項目\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void unknownCardOrItemReturns404() throws Exception {
		String cardId = createCard();
		String json = addItem(cardId, "項目").andReturn().getResponse().getContentAsString();
		String itemId = itemId(json, 0);
		String body = "{\"text\":\"x\",\"done\":false}";

		addItem(UNKNOWN_ID, "x").andExpect(status().isNotFound());
		mockMvc.perform(put(itemUrl(UNKNOWN_ID, itemId)).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isNotFound());
		mockMvc.perform(put(itemUrl(cardId, UNKNOWN_ID)).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete(itemUrl(cardId, UNKNOWN_ID))).andExpect(status().isNotFound());
	}

	@Test
	void itemOfAnotherCardReturns404() throws Exception {
		String cardA = createCard();
		String cardB = createCard();
		String itemOfA = itemId(addItem(cardA, "Aの項目").andReturn().getResponse().getContentAsString(), 0);

		mockMvc.perform(put(itemUrl(cardB, itemOfA)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"text\":\"x\",\"done\":true}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete(itemUrl(cardB, itemOfA))).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/cards/" + cardA))
				.andExpect(jsonPath("$.checklist.length()", is(1)))
				.andExpect(jsonPath("$.checklist[0].done", is(false)));
	}

	@Test
	void deletingCardRemovesItsChecklist() throws Exception {
		String cardId = createCard();
		addItem(cardId, "項目");

		mockMvc.perform(delete("/api/cards/" + cardId)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/cards/" + cardId)).andExpect(status().isNotFound());
	}

	private org.springframework.test.web.servlet.ResultActions addItem(String cardId, String text) throws Exception {
		return mockMvc.perform(post("/api/cards/" + cardId + "/checklist").contentType(MediaType.APPLICATION_JSON)
				.content("{\"text\":\"" + text + "\"}"));
	}

	private String itemUrl(String cardId, String itemId) {
		return "/api/cards/" + cardId + "/checklist/" + itemId;
	}

	private String itemId(String cardJson, int index) {
		return JsonPath.read(cardJson, "$.checklist[" + index + "].id");
	}

	private String createCard() throws Exception {
		String json = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + firstListId() + "\",\"title\":\"チェックリスト用\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(json, "$.id");
	}

	private String firstListId() throws Exception {
		String boards = mockMvc.perform(get("/api/boards")).andReturn().getResponse().getContentAsString();
		String boardId = JsonPath.read(boards, "$[0].id");
		String board = mockMvc.perform(get("/api/boards/" + boardId)).andReturn().getResponse().getContentAsString();
		return JsonPath.read(board, "$.lists[0].id");
	}
}
