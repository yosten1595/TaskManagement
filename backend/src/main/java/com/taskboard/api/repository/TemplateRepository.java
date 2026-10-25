package com.taskboard.api.repository;

import com.taskboard.api.entity.Template;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, UUID> {
}
