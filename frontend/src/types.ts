// バックエンドの DTO（com.taskboard.api.dto）に対応する型

export type Priority = 'HIGH' | 'MID' | 'LOW'

export interface Label {
  id: string
  name: string
  color: string
}

export interface CheckItem {
  id: string
  text: string
  done: boolean
  position: number
}

export interface Card {
  id: string
  listId: string
  title: string
  description: string | null
  dueDate: string | null
  priority: Priority | null
  warnDays: number
  position: number
  createdAt: string
  labels: Label[]
  checklist: CheckItem[]
}

export interface BoardList {
  id: string
  title: string
  position: number
  cards: Card[]
}

export interface BoardSummary {
  id: string
  title: string
  createdAt: string
}

export interface BoardDetail extends BoardSummary {
  lists: BoardList[]
}

export interface CardSearchParams {
  keyword?: string
  priority?: Priority
  listId?: string
}

// カード作成時の入力。title 以外は任意
export interface NewCard {
  title: string
  dueDate?: string
  priority?: Priority
  labelIds?: string[]
}
