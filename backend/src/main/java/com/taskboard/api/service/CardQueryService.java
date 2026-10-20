package com.taskboard.api.service;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.Priority;
import com.taskboard.api.repository.CardRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardQueryService {

	private final CardRepository cardRepository;

	// keyword はタイトル・説明の部分一致（大文字小文字を区別しない）。null の条件は無視する
	public List<CardDto> search(String keyword, Priority priority, UUID listId) {
		Specification<Card> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (keyword != null && !keyword.isBlank()) {
				String pattern = "%" + keyword.strip().toLowerCase() + "%";
				predicates.add(cb.or(
						cb.like(cb.lower(root.<String>get("title")), pattern),
						cb.like(cb.lower(cb.coalesce(root.<String>get("description"), "")), pattern)));
			}
			if (priority != null) {
				predicates.add(cb.equal(root.get("priority"), priority));
			}
			if (listId != null) {
				predicates.add(cb.equal(root.get("list").get("id"), listId));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
		Sort sort = Sort.by("list.position", "position");
		return cardRepository.findAll(spec, sort).stream().map(CardDto::from).toList();
	}

	public CardDto findById(UUID id) {
		return cardRepository.findById(id)
				.map(CardDto::from)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found: " + id));
	}
}
