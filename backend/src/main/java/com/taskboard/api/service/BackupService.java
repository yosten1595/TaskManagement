package com.taskboard.api.service;

import com.taskboard.api.dto.BackupDto;
import com.taskboard.api.entity.Board;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.CardLabel;
import com.taskboard.api.entity.CheckItem;
import com.taskboard.api.entity.Label;
import com.taskboard.api.entity.TaskList;
import com.taskboard.api.entity.Template;
import com.taskboard.api.entity.TemplateCheckItem;
import com.taskboard.api.entity.TemplateLabel;
import com.taskboard.api.repository.BoardRepository;
import com.taskboard.api.repository.LabelRepository;
import com.taskboard.api.repository.TemplateRepository;
import com.taskboard.api.util.Texts;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// 全ボード・テンプレートの書き出し（エクスポート）と、書き出したファイルからの追加（インポート）
@Service
@RequiredArgsConstructor
public class BackupService {

	private final BoardRepository boardRepository;
	private final TemplateRepository templateRepository;
	private final LabelRepository labelRepository;

	public record ImportResult(int boards, int cards, int templates) {
	}

	@Transactional(readOnly = true)
	public BackupDto export() {
		List<BackupDto.Board> boards = boardRepository.findAllByOrderByCreatedAtAsc().stream()
				.map(board -> new BackupDto.Board(board.getTitle(), board.getLists().stream()
						.map(list -> new BackupDto.ListItem(list.getTitle(), list.getCards().stream()
								.map(BackupService::toBackup).toList()))
						.toList()))
				.toList();
		List<BackupDto.Template> templates = templateRepository
				.findAll(Sort.by(Sort.Direction.ASC, "createdAt")).stream()
				.map(BackupService::toBackup).toList();
		return new BackupDto(BackupDto.CURRENT_VERSION, Instant.now(), boards, templates);
	}

	// 既存のデータは消さず、ファイルの内容を新しいボード・テンプレートとして追加する。途中で失敗したら何も追加しない
	@Transactional
	public ImportResult importBackup(BackupDto backup) {
		if (backup.version() != BackupDto.CURRENT_VERSION) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Unsupported backup version: " + backup.version());
		}
		Map<String, Label> labelsByName = new HashMap<>();
		labelRepository.findAll().forEach(label -> labelsByName.put(label.getName(), label));

		int cardCount = 0;
		List<BackupDto.Board> boards = backup.boards() == null ? List.of() : backup.boards();
		for (BackupDto.Board source : boards) {
			Board board = new Board();
			board.setTitle(source.title().strip());
			List<BackupDto.ListItem> lists = source.lists() == null ? List.of() : source.lists();
			for (int i = 0; i < lists.size(); i++) {
				TaskList list = new TaskList();
				list.setBoard(board);
				list.setTitle(lists.get(i).title().strip());
				list.setPosition(i);
				List<BackupDto.Card> cards = lists.get(i).cards() == null ? List.of() : lists.get(i).cards();
				for (int j = 0; j < cards.size(); j++) {
					list.getCards().add(toCard(cards.get(j), list, j, labelsByName));
					cardCount++;
				}
				board.getLists().add(list);
			}
			boardRepository.save(board);
		}

		List<BackupDto.Template> templates = backup.templates() == null ? List.of() : backup.templates();
		for (BackupDto.Template source : templates) {
			templateRepository.save(toTemplate(source, labelsByName));
		}
		return new ImportResult(boards.size(), cardCount, templates.size());
	}

	private static BackupDto.Card toBackup(Card card) {
		return new BackupDto.Card(
				card.getTitle(),
				card.getDescription(),
				card.getDueDate(),
				card.getPriority(),
				card.getWarnDays(),
				card.getLabels().stream().map(CardLabel::getLabel).map(Label::getName).sorted().toList(),
				card.getChecklist().stream().map(item -> new BackupDto.CheckItem(item.getText(), item.isDone())).toList());
	}

	private static BackupDto.Template toBackup(Template template) {
		return new BackupDto.Template(
				template.getTitle(),
				template.getDescription(),
				template.getPriority(),
				template.getWarnDays(),
				template.getLabels().stream().map(TemplateLabel::getLabel).map(Label::getName).sorted().toList(),
				template.getChecklist().stream().sorted(Comparator.comparingInt(TemplateCheckItem::getPosition))
						.map(TemplateCheckItem::getText).toList());
	}

	private static Card toCard(BackupDto.Card source, TaskList list, int position, Map<String, Label> labelsByName) {
		Card card = new Card();
		card.setList(list);
		card.setTitle(source.title().strip());
		card.setDescription(Texts.blankToNull(source.description()));
		card.setDueDate(source.dueDate());
		card.setPriority(source.priority());
		card.setWarnDays(source.warnDays() == null ? Card.DEFAULT_WARN_DAYS : source.warnDays());
		card.setPosition(position);
		if (source.labels() != null) {
			source.labels().stream().distinct().map(labelsByName::get).filter(java.util.Objects::nonNull)
					.forEach(label -> card.getLabels().add(new CardLabel(card, label)));
		}
		List<BackupDto.CheckItem> items = source.checklist() == null ? List.of() : source.checklist();
		for (int i = 0; i < items.size(); i++) {
			CheckItem item = new CheckItem();
			item.setCard(card);
			item.setText(items.get(i).text().strip());
			item.setDone(items.get(i).done());
			item.setPosition(i);
			card.getChecklist().add(item);
		}
		return card;
	}

	private static Template toTemplate(BackupDto.Template source, Map<String, Label> labelsByName) {
		Template template = new Template();
		template.setTitle(source.title().strip());
		template.setDescription(Texts.blankToNull(source.description()));
		template.setPriority(source.priority());
		template.setWarnDays(source.warnDays() == null ? Card.DEFAULT_WARN_DAYS : source.warnDays());
		if (source.labels() != null) {
			source.labels().stream().distinct().map(labelsByName::get).filter(java.util.Objects::nonNull)
					.forEach(label -> template.getLabels().add(new TemplateLabel(template, label)));
		}
		List<String> texts = source.checklist() == null ? List.of() : source.checklist();
		for (int i = 0; i < texts.size(); i++) {
			TemplateCheckItem item = new TemplateCheckItem();
			item.setTemplate(template);
			item.setText(texts.get(i).strip());
			item.setPosition(i);
			template.getChecklist().add(item);
		}
		return template;
	}
}
