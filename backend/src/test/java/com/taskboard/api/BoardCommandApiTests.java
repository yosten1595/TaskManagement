package com.taskboard.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

// ボードを API で作って検証する。作成したボードは終了後に API で削除する（開発用データには触れない）
@SpringBootTest
@AutoConfigureMockMvc
class BoardCommandApiTests {

	private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

	@Autowired
	MockMvc mockMvc;

	// 途中で失敗しても残らないよう、作成したボードを記録して後始末する
	private final List<String> createdBoardIds = new ArrayList<>();

	@AfterEach
	void deleteCreatedBoards() throws Exception {
		for (String id : createdBoardIds) {
			mockMvc.perform(delete("/api/boards/" + id));
		}
	}

	@Test
	void createBoardWithDefaultLists() throws Exception {
		ResultActions result = createBoard("  新しいボード  ")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title", is("新しいボード")));
		String id = idOf(result);

		mockMvc.perform(get("/api/boards/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lists[*].title", contains("未着手", "進行中", "完了")))
				.andExpect(jsonPath("$.lists[*].position", contains(0, 1, 2)));
		mockMvc.perform(get("/api/boards")).andExpect(jsonPath("$[*].id", hasItem(id)));
	}

	@Test
	void renameChangesOnlyTheTitle() throws Exception {
		String id = idOf(createBoard("変更前"));
		mockMvc.perform(put("/api/boards/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\" 変更後 \"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(id)))
				.andExpect(jsonPath("$.title", is("変更後")));

		mockMvc.perform(get("/api/boards/" + id))
				.andExpect(jsonPath("$.title", is("変更後")))
				.andExpect(jsonPath("$.lists", hasSize(3)));
	}

	@Test
	void deleteRemovesBoardListsAndCards() throws Exception {
		String id = idOf(createBoard("削除対象"));
		String board = mockMvc.perform(get("/api/boards/" + id)).andReturn().getResponse().getContentAsString();
		String listId = JsonPath.read(board, "$.lists[0].id");
		String card = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\",\"title\":\"消えるカード\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		String cardId = JsonPath.read(card, "$.id");
		mockMvc.perform(post("/api/cards/" + cardId + "/checklist").contentType(MediaType.APPLICATION_JSON)
				.content("{\"text\":\"消える項目\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(delete("/api/boards/" + id)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/boards/" + id)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/cards/" + cardId)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/boards")).andExpect(jsonPath("$[*].id", not(hasItem(id))));
	}

	@Test
	void blankTitleReturns400() throws Exception {
		createBoard("   ").andExpect(status().isBadRequest());
		// DB の列は 255 文字まで。超える入力は 500 ではなく 400
		createBoard("a".repeat(256)).andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/boards").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());

		String id = idOf(createBoard("そのまま"));
		mockMvc.perform(put("/api/boards/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/boards/" + id)).andExpect(jsonPath("$.title", is("そのまま")));
	}

	@Test
	void unknownBoardReturns404() throws Exception {
		mockMvc.perform(put("/api/boards/" + UNKNOWN_ID).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"x\"}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/boards/" + UNKNOWN_ID)).andExpect(status().isNotFound());
	}

	private ResultActions createBoard(String title) throws Exception {
		return mockMvc.perform(post("/api/boards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"" + title + "\"}"));
	}

	// 作成結果から ID を取り出し、後始末の対象に記録する
	private String idOf(ResultActions created) throws Exception {
		String id = JsonPath.read(created.andReturn().getResponse().getContentAsString(), "$.id");
		createdBoardIds.add(id);
		return id;
	}
}
