package com.taskboard.api.seed;

import com.taskboard.api.entity.Board;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.CardLabel;
import com.taskboard.api.entity.CheckItem;
import com.taskboard.api.entity.Label;
import com.taskboard.api.entity.Priority;
import com.taskboard.api.entity.TaskList;
import com.taskboard.api.repository.BoardRepository;
import com.taskboard.api.repository.LabelRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 動作確認用のテストデータ。boards が空のときだけ投入する。app.seed.enabled=false で無効化
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
public class TestDataSeeder implements CommandLineRunner {

	private final BoardRepository boardRepository;
	private final LabelRepository labelRepository;

	@Override
	@Transactional
	public void run(String... args) {
		if (boardRepository.count() > 0) {
			return;
		}
		Label backend = labelRepository.save(label("バックエンド", "#2563eb"));
		Label frontend = labelRepository.save(label("フロントエンド", "#16a34a"));
		Label bug = labelRepository.save(label("バグ", "#dc2626"));

		Board board = new Board();
		board.setTitle("サンプルプロジェクト");

		TaskList todo = list(board, "未着手", 0);
		TaskList doing = list(board, "進行中", 1);
		TaskList done = list(board, "完了", 2);

		Card api = card(todo, "タスク読み取りAPIを実装する", "ボード・リスト・カードを取得するREST APIを作る",
				LocalDate.now().plusDays(2), Priority.HIGH, 0);
		attach(api, backend);
		check(api, "エンティティ確認", true, 0);
		check(api, "Controller 実装", false, 1);
		check(api, "動作確認", false, 2);

		Card ui = card(todo, "ボード画面のワイヤーフレームを作る", null, LocalDate.now().plusDays(10), Priority.LOW, 1);
		attach(ui, frontend);

		Card tz = card(doing, "日付がずれるバグを調査する", "期限日のタイムゾーン処理を確認",
				LocalDate.now().minusDays(1), Priority.MID, 0);
		attach(tz, bug);
		attach(tz, backend);
		check(tz, "再現手順の整理", true, 0);

		Card db = card(done, "PostgreSQL を Docker で構築する", "docker-compose で起動できるようにした",
				LocalDate.now().minusDays(5), Priority.MID, 0);
		attach(db, backend);

		boardRepository.save(board);
	}

	private Label label(String name, String color) {
		Label l = new Label();
		l.setName(name);
		l.setColor(color);
		return l;
	}

	private TaskList list(Board board, String title, int position) {
		TaskList l = new TaskList();
		l.setBoard(board);
		l.setTitle(title);
		l.setPosition(position);
		board.getLists().add(l);
		return l;
	}

	private Card card(TaskList list, String title, String description, LocalDate due, Priority priority, int position) {
		Card c = new Card();
		c.setList(list);
		c.setTitle(title);
		c.setDescription(description);
		c.setDueDate(due);
		c.setPriority(priority);
		c.setPosition(position);
		list.getCards().add(c);
		return c;
	}

	private void attach(Card card, Label label) {
		card.getLabels().add(new CardLabel(card, label));
	}

	private void check(Card card, String text, boolean done, int position) {
		CheckItem item = new CheckItem();
		item.setCard(card);
		item.setText(text);
		item.setDone(done);
		item.setPosition(position);
		card.getChecklist().add(item);
	}
}
