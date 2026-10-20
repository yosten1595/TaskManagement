package com.taskboard.api.repository;

import com.taskboard.api.entity.Label;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, UUID> {
}
