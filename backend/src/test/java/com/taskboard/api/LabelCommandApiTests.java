package com.taskboard.api;

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

// ラベルを API で作って検証する。作成したラベル・ボードは終了後に API で削除する（開発用データには触れない）
@SpringBootTest
@AutoConfigureMockMvc
class LabelCommandApiTests {

	private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

	@Autowired
	MockMvc mockMvc;

	private final List<String> createdLabelIds = new ArrayList<>();
	private final List<String> createdBoardIds = new ArrayList<>();

	@AfterEach
	void cleanUp() throws Exception {
		for (String id : createdBoardIds) {
			mockMvc.perform(delete("/api/boards/" + id));
		}
		for (String id : createdLabelIds) {
			mockMvc.perform(delete("/api/labels/" + id));
		}
	}

	@Test
	void createLabelStripsNameAndLowercasesColor() throws Exception {
		String id = idOf(createLabel("  テスト用  ", "#AABBCC")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name", is("テスト用")))
				.andExpect(jsonPath("$.color", is("#aabbcc"))));

		mockMvc.perform(get("/api/labels")).andExpect(jsonPath("$[*].id", hasItem(id)));
	}

	@Test
	void updateChangesNameAndColor() throws Exception {
		String id = idOf(createLabel("変更前", "#111111"));
		mockMvc.perform(put("/api/labels/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"変更後\",\"color\":\"#222222\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(id)))
				.andExpect(jsonPath("$.name", is("変更後")))
				.andExpect(jsonPath("$.color", is("#222222")));
	}

	@Test
	void deleteRemovesLabelButKeepsCard() throws Exception {
		String labelId = idOf(createLabel("消えるラベル", "#333333"));
		String boardId = createBoard();
		String board = mockMvc.perform(get("/api/boards/" + boardId)).andReturn().getResponse().getContentAsString();
		String listId = JsonPath.read(board, "$.lists[0].id");
		String card = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\",\"title\":\"残るカード\",\"labelIds\":[\"" + labelId + "\"]}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.labels", hasSize(1)))
				.andReturn().getResponse().getContentAsString();
		String cardId = JsonPath.read(card, "$.id");

		mockMvc.perform(delete("/api/labels/" + labelId)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/labels")).andExpect(jsonPath("$[*].id", not(hasItem(labelId))));
		mockMvc.perform(get("/api/cards/" + cardId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("残るカード")))
				.andExpect(jsonPath("$.labels", hasSize(0)));
	}

	@Test
	void invalidInputReturns400() throws Exception {
		createLabel("   ", "#444444").andExpect(status().isBadRequest());
		createLabel("色なし", "").andExpect(status().isBadRequest());
		createLabel("不正な色", "red").andExpect(status().isBadRequest());
		createLabel("桁が違う", "#12345").andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/labels").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());

		String id = idOf(createLabel("そのまま", "#555555"));
		mockMvc.perform(put("/api/labels/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"\",\"color\":\"#666666\"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/labels")).andExpect(jsonPath("$[?(@.id=='" + id + "')].name", hasItem("そのまま")));
	}

	@Test
	void unknownLabelReturns404() throws Exception {
		mockMvc.perform(put("/api/labels/" + UNKNOWN_ID).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"x\",\"color\":\"#777777\"}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/labels/" + UNKNOWN_ID)).andExpect(status().isNotFound());
	}

	private ResultActions createLabel(String name, String color) throws Exception {
		return mockMvc.perform(post("/api/labels").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"" + name + "\",\"color\":\"" + color + "\"}"));
	}

	private String createBoard() throws Exception {
		String body = mockMvc.perform(post("/api/boards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"ラベルテスト用\"}"))
				.andReturn().getResponse().getContentAsString();
		String id = JsonPath.read(body, "$.id");
		createdBoardIds.add(id);
		return id;
	}

	// 作成結果から ID を取り出し、後始末の対象に記録する
	private String idOf(ResultActions created) throws Exception {
		String body = created.andReturn().getResponse().getContentAsString();
		String id = JsonPath.read(body, "$.id");
		createdLabelIds.add(id);
		return id;
	}
}
