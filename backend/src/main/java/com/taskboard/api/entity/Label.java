package com.taskboard.api.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "labels")
@Getter
@Setter
@NoArgsConstructor
public class Label {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	// #rrggbb
	@Column(nullable = false, length = 7)
	private String color;
}
