package com.taskboard.api.seed;

import com.taskboard.api.entity.Label;
import com.taskboard.api.repository.LabelRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 固定の3ラベル（要件定義書 3.5）を、起動時に登録する。ラベルを作成する API や画面はないので、
// テストデータの投入（app.seed.enabled）とは関係なく、本番でも必ず登録する。同名のラベルがあれば追加しない
@Component
@Order(1)
@RequiredArgsConstructor
public class DefaultLabelInitializer implements CommandLineRunner {

	public static final String BACKEND = "バックエンド";
	public static final String FRONTEND = "フロントエンド";
	public static final String BUG = "バグ";

	private static final List<Label> DEFAULTS = List.of(
			label(BACKEND, "#4a6a8a"),
			label(FRONTEND, "#5b7b48"),
			label(BUG, "#a63a2b"));

	private final LabelRepository labelRepository;

	@Override
	@Transactional
	public void run(String... args) {
		for (Label label : DEFAULTS) {
			if (!labelRepository.existsByName(label.getName())) {
				labelRepository.save(label(label.getName(), label.getColor()));
			}
		}
	}

	private static Label label(String name, String color) {
		Label label = new Label();
		label.setName(name);
		label.setColor(color);
		return label;
	}
}
