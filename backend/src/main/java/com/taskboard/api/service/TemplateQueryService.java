package com.taskboard.api.service;

import com.taskboard.api.dto.TemplateDto;
import com.taskboard.api.repository.TemplateRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateQueryService {

	private final TemplateRepository templateRepository;

	// 新しく登録したものから順に返す
	public List<TemplateDto> findAll() {
		return templateRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
				.map(TemplateDto::from).toList();
	}
}
