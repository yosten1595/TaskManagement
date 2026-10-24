package com.taskboard.api.service;

import com.taskboard.api.dto.CardDto;
import com.taskboard.api.dto.CreateCheckItemRequest;
import com.taskboard.api.dto.UpdateCheckItemRequest;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.CheckItem;
import com.taskboard.api.repository.CardRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class ChecklistCommandService {

	private final CardRepository cardRepository;

	// 追加した項目はチェックリストの末尾に置く
	public CardDto add(UUID cardId, CreateCheckItemRequest request) {
		Card card = findCard(cardId);
		CheckItem item = new CheckItem();
		item.setCard(card);
		item.setText(request.text().strip());
		item.setPosition(card.getChecklist().size());
		card.getChecklist().add(item);
		cardRepository.flush();
		return CardDto.from(card);
	}

	public CardDto update(UUID cardId, UUID itemId, UpdateCheckItemRequest request) {
		Card card = findCard(cardId);
		CheckItem item = findItem(card, itemId);
		item.setText(request.text().strip());
		item.setDone(request.done());
		return CardDto.from(card);
	}

	// 削除後は残りの項目の position を 0 から連番に詰め直す
	public void delete(UUID cardId, UUID itemId) {
		Card card = findCard(cardId);
		CheckItem item = findItem(card, itemId);
		card.getChecklist().remove(item);
		List<CheckItem> rest = card.getChecklist();
		for (int i = 0; i < rest.size(); i++) {
			rest.get(i).setPosition(i);
		}
	}

	private Card findCard(UUID cardId) {
		return cardRepository.findById(cardId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found: " + cardId));
	}

	// 別カードの項目 ID を指定した場合も 404
	private CheckItem findItem(Card card, UUID itemId) {
		return card.getChecklist().stream()
				.filter(item -> item.getId().equals(itemId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Check item not found: " + itemId));
	}
}
