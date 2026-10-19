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

// Template と Label の多対多の中間テーブル。DB 側で ON DELETE CASCADE を効かせるため Entity として定義する
@Entity
@Table(name = "template_labels")
@Getter
@Setter
@NoArgsConstructor
public class TemplateLabel {

	@EmbeddedId
	private Id id = new Id();

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("templateId")
	@JoinColumn(name = "template_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Template template;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("labelId")
	@JoinColumn(name = "label_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Label label;

	public TemplateLabel(Template template, Label label) {
		this.template = template;
		this.label = label;
	}

	@Embeddable
	@Getter
	@Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class Id implements Serializable {

		@Column(name = "template_id")
		private UUID templateId;

		@Column(name = "label_id")
		private UUID labelId;
	}
}
