package com.taskboard.api.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// java.util.List との衝突を避けるため、テーブル名 lists に対するクラス名は TaskList とする
@Entity
@Table(name = "lists")
@Getter
@Setter
@NoArgsConstructor
public class TaskList {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "board_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Board board;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private int position;

	@OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("position")
	private List<Card> cards = new ArrayList<>();
}
