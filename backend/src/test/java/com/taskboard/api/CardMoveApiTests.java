package com.taskboard.api;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

// docker-compose の PostgreSQL に接続し、TestDataSeeder が投入したリストを使って移動・並べ替えを検証する
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.seed.enabled=true")
class CardMoveApiTests {

	@Autowired
	MockMvc mockMvc;

	@Test
	void reorderWithinList() throws Exception {
		String listId = listIds().get(0);
		String a = create(listId, "並べ替えA");
		String b = create(listId, "並べ替えB");
		String c = create(listId, "並べ替えC");

		// C を B の前（A の直後）へ
		int posA = position(a);
		move(c, listId, posA + 1).andExpect(status().isOk()).andExpect(jsonPath("$.position", is(posA + 1)));

		assertPositionsContiguous(listId);
		assertOrder(listId, a, c, b);
	}

	@Test
	void moveToAnotherListInsertsAndRenumbersBothLists() throws Exception {
		List<String> lists = listIds();
		String source = lists.get(0);
		String target = lists.get(1);
		String a = create(source, "移動元A");
		String b = create(source, "移動元B");
		String t = create(target, "移動先T");
		int tPos = position(t);

		// A を移動先リストの T の前に挿入
		move(a, target, tPos).andExpect(status().isOk())
				.andExpect(jsonPath("$.listId", is(target)))
				.andExpect(jsonPath("$.position", is(tPos)));

		assertPositionsContiguous(source);
		assertPositionsContiguous(target);
		mockMvc.perform(get("/api/cards/" + t)).andExpect(jsonPath("$.position", is(tPos + 1)));
		mockMvc.perform(get("/api/cards/" + b)).andExpect(jsonPath("$.listId", is(source)));
	}

	@Test
	void outOfRangeOrOmittedPositionGoesToEnd() throws Exception {
		List<String> lists = listIds();
		String source = lists.get(0);
		String target = lists.get(1);
		String a = create(source, "末尾へ1");
		String b = create(source, "末尾へ2");

		move(a, target, 9999).andExpect(status().isOk());
		mockMvc.perform(put("/api/cards/" + b + "/move").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + target + "\"}")).andExpect(status().isOk());

		assertPositionsContiguous(target);
		assertOrder(target, a, b);
		assertPositionsContiguous(source);
	}

	@Test
	void keepsCardContent() throws Exception {
		List<String> lists = listIds();
		String created = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + lists.get(0) + "\",\"title\":\"内容維持\",\"priority\":\"HIGH\",\"dueDate\":\"2027-02-01\"}"))
				.andReturn().getResponse().getContentAsString();
		String id = JsonPath.read(created, "$.id");

		move(id, lists.get(1), 0).andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("内容維持")))
				.andExpect(jsonPath("$.priority", is("HIGH")))
				.andExpect(jsonPath("$.dueDate", is("2027-02-01")));
	}

	@Test
	void invalidRequests() throws Exception {
		String listId = listIds().get(0);
		String id = create(listId, "検証用");
		String unknown = "00000000-0000-0000-0000-000000000000";

		move(unknown, listId, 0).andExpect(status().isNotFound());
		move(id, unknown, 0).andExpect(status().isNotFound());
		move(id, listId, -1).andExpect(status().isBadRequest());
		mockMvc.perform(put("/api/cards/" + id + "/move").contentType(MediaType.APPLICATION_JSON)
				.content("{\"position\":0}")).andExpect(status().isBadRequest());
	}

	private org.springframework.test.web.servlet.ResultActions move(String cardId, String listId, int position)
			throws Exception {
		return mockMvc.perform(put("/api/cards/" + cardId + "/move").contentType(MediaType.APPLICATION_JSON)
				.content("{\"listId\":\"" + listId + "\",\"position\":" + position + "}"));
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

	// リスト内の position が 0 から連番（重複・欠番なし）であること
	private void assertPositionsContiguous(String listId) throws Exception {
		String json = mockMvc.perform(get("/api/cards").param("listId", listId))
				.andReturn().getResponse().getContentAsString();
		List<Integer> positions = JsonPath.read(json, "$[*].position");
		for (int i = 0; i < positions.size(); i++) {
			org.junit.jupiter.api.Assertions.assertEquals(i, positions.get(i), "positions: " + positions);
		}
	}

	// 指定したカードが、この順序で（間に他のカードを挟まず末尾から）並んでいること
	private void assertOrder(String listId, String... ids) throws Exception {
		String json = mockMvc.perform(get("/api/cards").param("listId", listId))
				.andReturn().getResponse().getContentAsString();
		List<String> actual = JsonPath.read(json, "$[*].id");
		int start = actual.indexOf(ids[0]);
		for (int i = 0; i < ids.length; i++) {
			org.junit.jupiter.api.Assertions.assertEquals(ids[i], actual.get(start + i), "order: " + actual);
		}
	}

	private List<String> listIds() throws Exception {
		String boards = mockMvc.perform(get("/api/boards")).andReturn().getResponse().getContentAsString();
		String boardId = JsonPath.read(boards, "$[0].id");
		String board = mockMvc.perform(get("/api/boards/" + boardId)).andReturn().getResponse().getContentAsString();
		return JsonPath.read(board, "$.lists[*].id");
	}
}
