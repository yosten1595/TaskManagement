package com.taskboard.api.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "check_items")
@Getter
@Setter
@NoArgsConstructor
public class CheckItem {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "card_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Card card;

	@Column(nullable = false)
	private String text;

	@Column(nullable = false)
	private boolean done;

	@Column(nullable = false)
	private int position;
}
