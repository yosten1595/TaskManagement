package com.taskboard.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.taskboard.api.entity.Label;
import com.taskboard.api.repository.LabelRepository;
import com.taskboard.api.seed.DefaultLabelInitializer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// テストデータの投入を止めても、固定の3ラベルは登録される。何度実行しても重複しない
@SpringBootTest(properties = "app.seed.enabled=false")
class DefaultLabelInitializerTests {

	@Autowired
	DefaultLabelInitializer initializer;

	@Autowired
	LabelRepository labelRepository;

	@Test
	void registersTheThreeFixedLabelsWithoutDuplicates() {
		initializer.run();
		initializer.run();

		List<String> names = labelRepository.findAll().stream().map(Label::getName).toList();
		for (String name : List.of(DefaultLabelInitializer.BACKEND, DefaultLabelInitializer.FRONTEND,
				DefaultLabelInitializer.BUG)) {
			assertThat(names.stream().filter(name::equals).count()).as(name).isEqualTo(1);
		}
	}
}
