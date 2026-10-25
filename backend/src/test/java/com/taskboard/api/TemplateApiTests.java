package com.taskboard.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.taskboard.api.repository.BoardRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

// テンプレートを API で作って検証する。専用のボードを使い、作成したテンプレートは終了後に API で削除する
@SpringBootTest
@AutoConfigureMockMvc
class TemplateApiTests {

	private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	BoardRepository boardRepository;

	TestBoardFixture fixture;
	String labelId;
	String labelName;
	private final List<String> createdTemplateIds = new ArrayList<>();

	@BeforeEach
	void setUp() throws Exception {
		fixture = TestBoardFixture.create(boardRepository, "A");
		String labels = mockMvc.perform(get("/api/labels")).andReturn().getResponse().getContentAsString();
		labelId = JsonPath.read(labels, "$[0].id");
		labelName = JsonPath.read(labels, "$[0].name");
	}

	@AfterEach
	void cleanUp() throws Exception {
		for (String id : createdTemplateIds) {
			mockMvc.perform(delete("/api/templates/" + id));
		}
		fixture.delete();
	}

	@Test
	void createAndListTemplate() throws Exception {
		String id = idOf(createTemplate("{\"title\":\"  週次レポート  \",\"description\":\"毎週\",\"priority\":\"HIGH\","
				+ "\"warnDays\":5,\"labelIds\":[\"" + labelId + "\"],\"checklist\":[\"集計\",\" 送付 \"]}")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title", is("週次レポート")))
				.andExpect(jsonPath("$.description", is("毎週")))
				.andExpect(jsonPath("$.priority", is("HIGH")))
				.andExpect(jsonPath("$.warnDays", is(5)))
				.andExpect(jsonPath("$.labels[0].id", is(labelId)))
				.andExpect(jsonPath("$.checklist[*].text", contains("集計", "送付")))
				.andExpect(jsonPath("$.checklist[*].position", contains(0, 1))));

		mockMvc.perform(get("/api/templates"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].id", hasItem(id)));
	}

	@Test
	void createWithOnlyTitleUsesDefaults() throws Exception {
		createTemplate("{\"title\":\"最小\"}")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.description", nullValue()))
				.andExpect(jsonPath("$.priority", nullValue()))
				.andExpect(jsonPath("$.warnDays", is(3)))
				.andExpect(jsonPath("$.labels", hasSize(0)))
				.andExpect(jsonPath("$.checklist", hasSize(0)));
	}

	@Test
	void updateReplacesAllFields() throws Exception {
		String id = idOf(createTemplate("{\"title\":\"更新前\",\"priority\":\"LOW\",\"labelIds\":[\"" + labelId
				+ "\"],\"checklist\":[\"古い項目\"]}"));

		// 同じラベルを指定し直しても、紐付けの重複で失敗しない
		mockMvc.perform(put("/api/templates/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"更新後\",\"labelIds\":[\"" + labelId + "\"],\"checklist\":[\"新1\",\"新2\"]}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(id)))
				.andExpect(jsonPath("$.title", is("更新後")))
				.andExpect(jsonPath("$.priority", nullValue()))
				.andExpect(jsonPath("$.labels[0].id", is(labelId)))
				.andExpect(jsonPath("$.checklist[*].text", contains("新1", "新2")));

		mockMvc.perform(put("/api/templates/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"空にする\"}"))
				.andExpect(jsonPath("$.labels", hasSize(0)))
				.andExpect(jsonPath("$.checklist", hasSize(0)));
	}

	@Test
	void deleteKeepsCardsCreatedFromTemplate() throws Exception {
		String id = idOf(createTemplate("{\"title\":\"消える\",\"checklist\":[\"項目\"]}"));
		String cardId = JsonPath.read(mockMvc.perform(post("/api/templates/" + id + "/cards")
				.contentType(MediaType.APPLICATION_JSON).content("{\"listId\":\"" + fixture.firstListId() + "\"}"))
				.andReturn().getResponse().getContentAsString(), "$.id");

		mockMvc.perform(delete("/api/templates/" + id)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/templates")).andExpect(jsonPath("$[*].id", not(hasItem(id))));
		mockMvc.perform(get("/api/cards/" + cardId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.checklist", hasSize(1)));
	}

	@Test
	void createCardCopiesTemplateContent() throws Exception {
		String id = idOf(createTemplate("{\"title\":\"定例\",\"description\":\"説明\",\"priority\":\"MID\","
				+ "\"warnDays\":7,\"labelIds\":[\"" + labelId + "\"],\"checklist\":[\"一\",\"二\"]}"));
		String listId = fixture.firstListId();
		mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\",\"title\":\"既存カード\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/templates/" + id + "/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.listId", is(listId)))
				.andExpect(jsonPath("$.title", is("定例")))
				.andExpect(jsonPath("$.description", is("説明")))
				.andExpect(jsonPath("$.priority", is("MID")))
				.andExpect(jsonPath("$.warnDays", is(7)))
				.andExpect(jsonPath("$.dueDate", nullValue()))
				.andExpect(jsonPath("$.position", is(1)))
				.andExpect(jsonPath("$.labels[0].name", is(labelName)))
				.andExpect(jsonPath("$.checklist[*].text", contains("一", "二")))
				.andExpect(jsonPath("$.checklist[*].done", contains(false, false)));
	}

	@Test
	void invalidRequests() throws Exception {
		createTemplate("{\"title\":\"  \"}").andExpect(status().isBadRequest());
		createTemplate("{}").andExpect(status().isBadRequest());
		createTemplate("{\"title\":\"x\",\"warnDays\":-1}").andExpect(status().isBadRequest());
		createTemplate("{\"title\":\"x\",\"warnDays\":366}").andExpect(status().isBadRequest());
		createTemplate("{\"title\":\"x\",\"checklist\":[\"  \"]}").andExpect(status().isBadRequest());
		createTemplate("{\"title\":\"x\",\"priority\":\"URGENT\"}").andExpect(status().isBadRequest());
		createTemplate("{\"title\":\"x\",\"labelIds\":[\"" + UNKNOWN_ID + "\"]}").andExpect(status().isNotFound());

		String id = idOf(createTemplate("{\"title\":\"そのまま\"}"));
		mockMvc.perform(put("/api/templates/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/templates/" + id + "/cards").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/templates")).andExpect(jsonPath("$[?(@.id=='" + id + "')].title", hasItem("そのまま")));
	}

	@Test
	void unknownIdsReturn404() throws Exception {
		String id = idOf(createTemplate("{\"title\":\"対象\"}"));
		mockMvc.perform(put("/api/templates/" + UNKNOWN_ID).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"x\"}")).andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/templates/" + UNKNOWN_ID)).andExpect(status().isNotFound());
		mockMvc.perform(post("/api/templates/" + UNKNOWN_ID + "/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + fixture.firstListId() + "\"}")).andExpect(status().isNotFound());
		mockMvc.perform(post("/api/templates/" + id + "/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + UNKNOWN_ID + "\"}")).andExpect(status().isNotFound());
	}

	private ResultActions createTemplate(String json) throws Exception {
		return mockMvc.perform(post("/api/templates").contentType(MediaType.APPLICATION_JSON).content(json));
	}

	// 作成結果から ID を取り出し、後始末の対象に記録する
	private String idOf(ResultActions created) throws Exception {
		String id = JsonPath.read(created.andReturn().getResponse().getContentAsString(), "$.id");
		createdTemplateIds.add(id);
		return id;
	}
}
