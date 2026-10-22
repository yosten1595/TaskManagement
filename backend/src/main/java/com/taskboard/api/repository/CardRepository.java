package com.taskboard.api.repository;

import com.taskboard.api.entity.Card;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CardRepository extends JpaRepository<Card, UUID>, JpaSpecificationExecutor<Card> {

	int countByListId(UUID listId);
}
