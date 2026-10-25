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
  labelId?: string
}

// テンプレート。チェック項目は未完了でコピーするため done を持たない
export interface Template {
  id: string
  title: string
  description: string | null
  priority: Priority | null
  warnDays: number
  labels: Label[]
  checklist: { id: string; text: string; position: number }[]
}

// テンプレート作成時の入力。checklist は項目のテキストを表示順に並べる
export interface NewTemplate {
  title: string
  description: string | null
  priority: Priority | null
  warnDays: number
  labelIds: string[]
  checklist: string[]
}

// カード作成時の入力。title 以外は任意
export interface NewCard {
  title: string
  dueDate?: string
  priority?: Priority
  labelIds?: string[]
  warnDays?: number
}

// カード編集時の入力（全項目を置き換える）。null は未設定に戻す
export interface CardUpdate {
  title: string
  description: string | null
  dueDate: string | null
  priority: Priority | null
  labelIds: string[]
  warnDays: number
}

// リスト内のカードを並べ替える基準（バックエンドの CardSortKey に対応）
export type CardSortKey = 'DUE_DATE' | 'PRIORITY'
