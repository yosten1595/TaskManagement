package com.taskboard.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

// エクスポート／インポートを検証する。開発用データを増やさないよう、インポートには自分で作った小さなファイルだけを使い、
// このテストが作ったボード・テンプレートは、タイトルで探して終了後に API で削除する
@SpringBootTest
@AutoConfigureMockMvc
class BackupApiTests {

	private static final String TITLE = "バックアップテスト専用";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@AfterEach
	void cleanUp() throws Exception {
		String boards = mockMvc.perform(get("/api/boards")).andReturn().getResponse().getContentAsString();
		for (String id : JsonPath.<List<String>>read(boards, "$[?(@.title=='" + TITLE + "')].id")) {
			mockMvc.perform(delete("/api/boards/" + id));
		}
		String templates = mockMvc.perform(get("/api/templates")).andReturn().getResponse().getContentAsString();
		for (String id : JsonPath.<List<String>>read(templates, "$[?(@.title=='" + TITLE + "')].id")) {
			mockMvc.perform(delete("/api/templates/" + id));
		}
	}

	@Test
	void importAddsBoardsAndTemplatesWithoutTouchingExistingData() throws Exception {
		int before = JsonPath.<List<?>>read(
				mockMvc.perform(get("/api/boards")).andReturn().getResponse().getContentAsString(), "$").size();
		String labelName = JsonPath.read(
				mockMvc.perform(get("/api/labels")).andReturn().getResponse().getContentAsString(), "$[0].name");

		mockMvc.perform(post("/api/import").contentType(MediaType.APPLICATION_JSON).content(sample(labelName)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.boards", is(1)))
				.andExpect(jsonPath("$.cards", is(2)))
				.andExpect(jsonPath("$.templates", is(1)));

		String boards = mockMvc.perform(get("/api/boards")).andReturn().getResponse().getContentAsString();
		org.junit.jupiter.api.Assertions.assertEquals(before + 1, JsonPath.<List<?>>read(boards, "$").size());
		String id = JsonPath.<List<String>>read(boards, "$[?(@.title=='" + TITLE + "')].id").get(0);
		mockMvc.perform(get("/api/boards/" + id))
				.andExpect(jsonPath("$.lists[*].title", contains("未着手", "完了")))
				.andExpect(jsonPath("$.lists[0].cards[*].title", contains("カード1", "カード2")))
				.andExpect(jsonPath("$.lists[0].cards[0].description", is("説明")))
				.andExpect(jsonPath("$.lists[0].cards[0].dueDate", is("2027-02-03")))
				.andExpect(jsonPath("$.lists[0].cards[0].priority", is("HIGH")))
				.andExpect(jsonPath("$.lists[0].cards[0].warnDays", is(7)))
				.andExpect(jsonPath("$.lists[0].cards[0].labels[*].name", contains(labelName)))
				.andExpect(jsonPath("$.lists[0].cards[0].checklist[*].text", contains("項目A", "項目B")))
				.andExpect(jsonPath("$.lists[0].cards[0].checklist[*].done", contains(true, false)))
				.andExpect(jsonPath("$.lists[0].cards[1].warnDays", is(3)))
				.andExpect(jsonPath("$.lists[0].cards[1].dueDate", nullValue()))
				.andExpect(jsonPath("$.lists[0].cards[1].labels", hasSize(0)));
		mockMvc.perform(get("/api/templates"))
				.andExpect(jsonPath("$[?(@.title=='" + TITLE + "')].checklist[*].text", contains("手順1", "手順2")));
	}

	@Test
	void exportedBoardCanBeImportedAgain() throws Exception {
		String labelName = JsonPath.read(
				mockMvc.perform(get("/api/labels")).andReturn().getResponse().getContentAsString(), "$[0].name");
		mockMvc.perform(post("/api/import").contentType(MediaType.APPLICATION_JSON).content(sample(labelName)))
				.andExpect(status().isCreated());

		String exported = mockMvc.perform(get("/api/export"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.version", is(1)))
				.andExpect(jsonPath("$.boards[*].title", hasItem(TITLE)))
				.andReturn().getResponse().getContentAsString();

		// 書き出した中から、このテストのボードだけを取り出して、もう一度インポートする
		String path = "$.boards[?(@.title=='" + TITLE + "')]";
		List<Object> mine = JsonPath.read(exported, path);
		org.junit.jupiter.api.Assertions.assertEquals(1, mine.size());
		String file = "{\"version\":1,\"boards\":" + objectMapper.writeValueAsString(mine) + "}";
		mockMvc.perform(post("/api/import").contentType(MediaType.APPLICATION_JSON).content(file))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.boards", is(1)))
				.andExpect(jsonPath("$.cards", is(2)));

		String after = mockMvc.perform(get("/api/export")).andReturn().getResponse().getContentAsString();
		List<Object> copies = JsonPath.read(after, path);
		org.junit.jupiter.api.Assertions.assertEquals(2, copies.size());
		org.junit.jupiter.api.Assertions.assertEquals(copies.get(0), copies.get(1));
	}

	@Test
	void invalidFilesReturn400AndAddNothing() throws Exception {
		String before = mockMvc.perform(get("/api/boards")).andReturn().getResponse().getContentAsString();

		// バージョン違い、バージョンなし、タイトルが空、警告日数が範囲外、不正な優先度
		String[] bad = {
				"{\"version\":2,\"boards\":[]}",
				"{\"boards\":[]}",
				"{\"version\":1,\"boards\":[{\"title\":\"  \",\"lists\":[]}]}",
				"{\"version\":1,\"boards\":[{\"title\":\"" + TITLE + "\",\"lists\":[{\"title\":\"A\",\"cards\":[{\"title\":\"x\",\"warnDays\":999}]}]}]}",
				"{\"version\":1,\"boards\":[{\"title\":\"" + TITLE + "\",\"lists\":[{\"title\":\"A\",\"cards\":[{\"title\":\"x\",\"priority\":\"URGENT\"}]}]}]}",
				// タイトルが 255 文字を超える、ボードが上限（100 件）を超える
				"{\"version\":1,\"boards\":[{\"title\":\"" + "a".repeat(256) + "\",\"lists\":[]}]}",
				"{\"version\":1,\"boards\":[" + "{\"title\":\"x\",\"lists\":[]},".repeat(100) + "{\"title\":\"x\",\"lists\":[]}]}",
				"not json",
		};
		for (String json : bad) {
			mockMvc.perform(post("/api/import").contentType(MediaType.APPLICATION_JSON).content(json))
					.andExpect(status().isBadRequest());
		}
		// 1 つ目が正しく、2 つ目が不正なファイルでも、1 つ目は追加されない（全か無か）
		mockMvc.perform(post("/api/import").contentType(MediaType.APPLICATION_JSON).content(
				"{\"version\":1,\"boards\":[{\"title\":\"" + TITLE + "\",\"lists\":[]},{\"title\":\"\",\"lists\":[]}]}"))
				.andExpect(status().isBadRequest());

		String after = mockMvc.perform(get("/api/boards")).andReturn().getResponse().getContentAsString();
		org.junit.jupiter.api.Assertions.assertEquals(before, after);
	}

	private static String sample(String labelName) {
		return "{\"version\":1,\"boards\":[{\"title\":\"" + TITLE + "\",\"lists\":["
				+ "{\"title\":\"未着手\",\"cards\":["
				+ "{\"title\":\"カード1\",\"description\":\"説明\",\"dueDate\":\"2027-02-03\",\"priority\":\"HIGH\",\"warnDays\":7,"
				+ "\"labels\":[\"" + labelName + "\",\"存在しないラベル\"],"
				+ "\"checklist\":[{\"text\":\"項目A\",\"done\":true},{\"text\":\"項目B\",\"done\":false}]},"
				+ "{\"title\":\"カード2\"}]},"
				+ "{\"title\":\"完了\",\"cards\":[]}]}],"
				+ "\"templates\":[{\"title\":\"" + TITLE + "\",\"checklist\":[\"手順1\",\"手順2\"]}]}";
	}
}
