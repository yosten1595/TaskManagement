package com.taskboard.api.controller;

import com.taskboard.api.dto.LabelDto;
import com.taskboard.api.service.LabelQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelController {

	private final LabelQueryService labelQueryService;

	@GetMapping
	public List<LabelDto> list() {
		return labelQueryService.findAll();
	}
}
