package com.taskboard.api.controller;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.dto.ListDto;
import com.taskboard.api.dto.ListRequest;
import com.taskboard.api.dto.SortListRequest;
import com.taskboard.api.service.ListCommandService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class ListController {

	private final ListCommandService listCommandService;

	@PutMapping("/{id}")
	public ListDto rename(@PathVariable UUID id, @Valid @RequestBody ListRequest request) {
		return listCommandService.rename(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		listCommandService.delete(id);
	}

	// リスト内のカードを並べ替えて position に保存し、並べ替え後のカード一覧を返す
	@PutMapping("/{id}/sort")
	public List<CardDto> sort(@PathVariable UUID id, @Valid @RequestBody SortListRequest request) {
		return listCommandService.sort(id, request.by());
	}
}
