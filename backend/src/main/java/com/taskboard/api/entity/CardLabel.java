package com.taskboard.api.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// Card と Label の多対多の中間テーブル。DB 側で ON DELETE CASCADE を効かせるため Entity として定義する
@Entity
@Table(name = "card_labels")
@Getter
@Setter
@NoArgsConstructor
public class CardLabel {

	@EmbeddedId
	private Id id = new Id();

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("cardId")
	@JoinColumn(name = "card_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Card card;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("labelId")
	@JoinColumn(name = "label_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Label label;

	public CardLabel(Card card, Label label) {
		this.card = card;
		this.label = label;
	}

	@Embeddable
	@Getter
	@Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class Id implements Serializable {

		private static final long serialVersionUID = 1L;

		@Column(name = "card_id")
		private UUID cardId;

		@Column(name = "label_id")
		private UUID labelId;
	}
}
