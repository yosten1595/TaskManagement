package com.taskboard.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "boards")
@Getter
@Setter
@NoArgsConstructor
public class Board {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String title;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("position")
	private List<TaskList> lists = new ArrayList<>();
}
