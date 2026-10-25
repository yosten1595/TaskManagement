package com.taskboard.api.service;

import com.taskboard.api.dto.LabelDto;
import com.taskboard.api.dto.LabelRequest;
import com.taskboard.api.entity.Label;
import com.taskboard.api.repository.LabelRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class LabelCommandService {

	private final LabelRepository labelRepository;

	public LabelDto create(LabelRequest request) {
		Label label = new Label();
		apply(label, request);
		return LabelDto.from(labelRepository.save(label));
	}

	public LabelDto update(UUID id, LabelRequest request) {
		Label label = find(id);
		apply(label, request);
		return LabelDto.from(label);
	}

	// card_labels / template_labels の紐付けは DB の ON DELETE CASCADE で一緒に削除される
	public void delete(UUID id) {
		labelRepository.delete(find(id));
	}

	private void apply(Label label, LabelRequest request) {
		label.setName(request.name().strip());
		label.setColor(request.color().toLowerCase());
	}

	private Label find(UUID id) {
		return labelRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Label not found: " + id));
	}
}
