package com.taskboard.api.repository;

import com.taskboard.api.entity.TaskList;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskListRepository extends JpaRepository<TaskList, UUID> {
}
