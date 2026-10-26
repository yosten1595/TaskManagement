package com.taskboard.api.service;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.Priority;
import com.taskboard.api.exception.ResourceNotFoundException;
import com.taskboard.api.repository.CardRepository;
import com.taskboard.api.util.Texts;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardQueryService {

	// LIKE のエスケープ文字。Texts.escapeLike と合わせる
	private static final char LIKE_ESCAPE = '\\';

	private final CardRepository cardRepository;

	// keyword はタイトル・説明の部分一致（大文字小文字を区別しない）。boardId はそのボードのカードだけに絞る。null の条件は無視する
	public List<CardDto> search(String keyword, Priority priority, UUID listId, UUID labelId, UUID boardId) {
		Specification<Card> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (keyword != null && !keyword.isBlank()) {
				String pattern = "%" + Texts.escapeLike(keyword.strip().toLowerCase()) + "%";
				predicates.add(cb.or(
						cb.like(cb.lower(root.<String>get("title")), pattern, LIKE_ESCAPE),
						cb.like(cb.lower(cb.coalesce(root.<String>get("description"), "")), pattern, LIKE_ESCAPE)));
			}
			if (priority != null) {
				predicates.add(cb.equal(root.get("priority"), priority));
			}
			if (listId != null) {
				predicates.add(cb.equal(root.get("list").get("id"), listId));
			}
			if (boardId != null) {
				predicates.add(cb.equal(root.get("list").get("board").get("id"), boardId));
			}
			if (labelId != null) {
				predicates.add(cb.equal(root.join("labels").get("label").get("id"), labelId));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
		Sort sort = Sort.by("list.position", "position");
		return cardRepository.findAll(spec, sort).stream().map(CardDto::from).toList();
	}

	public CardDto findById(UUID id) {
		return cardRepository.findById(id)
				.map(CardDto::from)
				.orElseThrow(() -> new ResourceNotFoundException("Card not found: " + id));
	}
}
