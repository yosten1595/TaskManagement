import type { BoardDetail } from './types'

// ボード 1 件を、インポート API（バックアップ形式）に渡せる JSON にする。削除したボードを元に戻すために使う。
// ID は含めず、並び順は配列の順、ラベルは名前で表す
export function boardToBackupJson(board: BoardDetail): string {
  return JSON.stringify({
    version: 1,
    boards: [
      {
        title: board.title,
        lists: board.lists.map((list) => ({
          title: list.title,
          cards: list.cards.map((card) => ({
            title: card.title,
            description: card.description,
            dueDate: card.dueDate,
            priority: card.priority,
            warnDays: card.warnDays,
            labels: card.labels.map((label) => label.name),
            checklist: card.checklist.map((item) => ({ text: item.text, done: item.done })),
          })),
        })),
      },
    ],
  })
}
