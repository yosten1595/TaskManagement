package com.taskboard.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.taskboard.api.entity.Board;
import com.taskboard.api.entity.TaskList;
import com.taskboard.api.repository.BoardRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// 専用のボードとリストを作って並べ替えを検証する（開発用データの並びを変えない）。終了後にボードごと削除する
@SpringBootTest
@AutoConfigureMockMvc
class ListSortApiTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	BoardRepository boardRepository;

	UUID boardId;
	String listId;
	String otherListId;

	@BeforeEach
	void createBoard() {
		Board board = new Board();
		board.setTitle("並べ替えテスト用");
		board.getLists().add(list(board, "A", 0));
		board.getLists().add(list(board, "B", 1));
		Board saved = boardRepository.save(board);
		boardId = saved.getId();
		listId = saved.getLists().get(0).getId().toString();
		otherListId = saved.getLists().get(1).getId().toString();
	}

	@AfterEach
	void deleteBoard() {
		boardRepository.deleteById(boardId);
	}

	@Test
	void sortByDueDateNullsLast() throws Exception {
		String c = card(listId, "c", "2027-03-01", null);
		String none = card(listId, "none", null, null);
		String a = card(listId, "a", "2027-01-01", null);
		String b = card(listId, "b", "2027-02-01", null);
		String other = card(otherListId, "other", "2027-01-01", null);
		String otherFirst = card(otherListId, "other2", null, null);

		sort(listId, "DUE_DATE").andExpect(status().isOk());

		assertOrder(listId, a, b, c, none);
		assertOrder(otherListId, other, otherFirst);
	}

	@Test
	void sortByPriorityThenDueDate() throws Exception {
		String lowEarly = card(listId, "low", "2027-01-01", "LOW");
		String highLate = card(listId, "highLate", "2027-05-01", "HIGH");
		String highEarly = card(listId, "highEarly", "2027-02-01", "HIGH");
		String noneP = card(listId, "noPriority", "2027-01-01", null);
		String midNoDue = card(listId, "mid", null, "MID");
		String highNoDue = card(listId, "highNoDue", null, "HIGH");

		sort(listId, "PRIORITY").andExpect(status().isOk());

		assertOrder(listId, highEarly, highLate, highNoDue, midNoDue, lowEarly, noneP);
	}

	@Test
	void sortIsStableForEqualCards() throws Exception {
		String first = card(listId, "first", "2027-01-01", "HIGH");
		String second = card(listId, "second", "2027-01-01", "HIGH");
		String early = card(listId, "early", "2026-12-01", "HIGH");

		sort(listId, "PRIORITY").andExpect(status().isOk());

		assertOrder(listId, early, first, second);
	}

	@Test
	void invalidRequests() throws Exception {
		sort("00000000-0000-0000-0000-000000000000", "DUE_DATE").andExpect(status().isNotFound());
		mockMvc.perform(put("/api/lists/" + listId + "/sort").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
		sort(listId, "TITLE").andExpect(status().isBadRequest());
	}

	private static TaskList list(Board board, String title, int position) {
		TaskList list = new TaskList();
		list.setBoard(board);
		list.setTitle(title);
		list.setPosition(position);
		return list;
	}

	private org.springframework.test.web.servlet.ResultActions sort(String id, String by) throws Exception {
		return mockMvc.perform(put("/api/lists/" + id + "/sort").contentType(MediaType.APPLICATION_JSON)
				.content("{\"by\":\"" + by + "\"}"));
	}

	private String card(String list, String title, String dueDate, String priority) throws Exception {
		String body = "{\"listId\":\"" + list + "\",\"title\":\"" + title + "\""
				+ (dueDate == null ? "" : ",\"dueDate\":\"" + dueDate + "\"")
				+ (priority == null ? "" : ",\"priority\":\"" + priority + "\"") + "}";
		String json = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(json, "$.id");
	}

	// 取得し直した順序が期待どおりで、position が 0 から連番であること（保存されていることの確認）
	private void assertOrder(String list, String... ids) throws Exception {
		String json = mockMvc.perform(get("/api/cards").param("listId", list))
				.andReturn().getResponse().getContentAsString();
		List<String> actual = JsonPath.read(json, "$[*].id");
		List<Integer> positions = JsonPath.read(json, "$[*].position");
		assertEquals(List.of(ids), actual);
		for (int i = 0; i < positions.size(); i++) {
			assertEquals(i, positions.get(i), "positions: " + positions);
		}
	}
}
