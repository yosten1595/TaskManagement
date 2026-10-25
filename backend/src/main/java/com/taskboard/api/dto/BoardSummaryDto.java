package com.taskboard.api.dto;

import com.taskboard.api.entity.Board;
import com.taskboard.api.entity.Card;
import com.taskboard.api.entity.TaskList;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// cardCount は全カード数。overdueCount / soonCount は画面のカード表示（期限切れ・期限間近）と同じ基準で数える。
// 「完了」リストのカードは期限の警告表示をしないので数えない
public record BoardSummaryDto(
		UUID id, String title, Instant createdAt, int cardCount, int overdueCount, int soonCount) {

	public static BoardSummaryDto from(Board board, LocalDate today) {
		int cardCount = 0;
		int overdue = 0;
		int soon = 0;
		for (TaskList list : board.getLists()) {
			boolean done = TaskList.DONE_TITLE.equals(list.getTitle());
			for (Card card : list.getCards()) {
				cardCount++;
				if (done || card.getDueDate() == null) {
					continue;
				}
				if (card.getDueDate().isBefore(today)) {
					overdue++;
				} else if (!card.getDueDate().isAfter(today.plusDays(card.getWarnDays()))) {
					soon++;
				}
			}
		}
		return new BoardSummaryDto(board.getId(), board.getTitle(), board.getCreatedAt(), cardCount, overdue, soon);
	}
}
