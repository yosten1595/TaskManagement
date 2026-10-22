package com.taskboard.api.service;

import com.taskboard.api.dto.LabelDto;
import com.taskboard.api.repository.LabelRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabelQueryService {

	private final LabelRepository labelRepository;

	public List<LabelDto> findAll() {
		return labelRepository.findAll(Sort.by("name")).stream().map(LabelDto::from).toList();
	}
}
