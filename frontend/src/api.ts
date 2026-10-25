import type { BoardDetail, BoardSummary, Card, CardSearchParams, CardSortKey, CardUpdate, Label, NewCard, NewTemplate, Template } from './types'

async function getJson<T>(url: string): Promise<T> {
  const res = await fetch(url)
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}: ${url}`)
  }
  return res.json() as Promise<T>
}

export const fetchBoards = () => getJson<BoardSummary[]>('/api/boards')

export const fetchBoard = (id: string) => getJson<BoardDetail>(`/api/boards/${id}`)

export function searchCards({ keyword, priority, listId, labelId }: CardSearchParams = {}) {
  const params = new URLSearchParams()
  if (keyword) params.set('keyword', keyword)
  if (priority) params.set('priority', priority)
  if (listId) params.set('listId', listId)
  if (labelId) params.set('labelId', labelId)
  const query = params.toString()
  return getJson<Card[]>(`/api/cards${query ? `?${query}` : ''}`)
}

export const fetchCard = (id: string) => getJson<Card>(`/api/cards/${id}`)

export const fetchLabels = () => getJson<Label[]>('/api/labels')

export async function createCard(listId: string, card: NewCard): Promise<Card> {
  const res = await fetch('/api/cards', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ listId, ...card }),
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<Card>
}

export async function deleteCard(id: string): Promise<void> {
  const res = await fetch(`/api/cards/${id}`, { method: 'DELETE' })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
}

export async function updateCard(id: string, card: CardUpdate): Promise<Card> {
  const res = await fetch(`/api/cards/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(card),
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<Card>
}

// position は移動先リスト内での 0 始まりの挿入位置
export async function moveCard(id: string, listId: string, position: number): Promise<Card> {
  const res = await fetch(`/api/cards/${id}/move`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ listId, position }),
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<Card>
}

// 並べ替え結果は position に保存され、並べ替え後のカード一覧が返る
export async function sortList(listId: string, by: CardSortKey): Promise<Card[]> {
  const res = await fetch(`/api/lists/${listId}/sort`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ by }),
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<Card[]>
}

// チェックリストの追加・更新は、更新後のカードが返る
export async function addCheckItem(cardId: string, text: string): Promise<Card> {
  const res = await fetch(`/api/cards/${cardId}/checklist`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text }),
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<Card>
}

export async function updateCheckItem(cardId: string, itemId: string, text: string, done: boolean): Promise<Card> {
  const res = await fetch(`/api/cards/${cardId}/checklist/${itemId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text, done }),
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<Card>
}

export async function deleteCheckItem(cardId: string, itemId: string): Promise<void> {
  const res = await fetch(`/api/cards/${cardId}/checklist/${itemId}`, { method: 'DELETE' })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
}

export async function createBoard(title: string): Promise<BoardSummary> {
  const res = await fetch('/api/boards', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title }),
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<BoardSummary>
}

export async function renameBoard(id: string, title: string): Promise<BoardSummary> {
  const res = await fetch(`/api/boards/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title }),
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<BoardSummary>
}

// ボードに属するリスト・カードも一緒に削除される
export async function deleteBoard(id: string): Promise<void> {
  const res = await fetch(`/api/boards/${id}`, { method: 'DELETE' })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
}

export const fetchTemplates = () => getJson<Template[]>('/api/templates')

export async function createTemplate(template: NewTemplate): Promise<Template> {
  const res = await fetch('/api/templates', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(template),
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<Template>
}

// テンプレートの内容をコピーして、指定リストの末尾にカードを作る
export async function createCardFromTemplate(templateId: string, listId: string): Promise<Card> {
  const res = await fetch(`/api/templates/${templateId}/cards`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ listId }),
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<Card>
}

export async function updateTemplate(id: string, template: NewTemplate): Promise<Template> {
  const res = await fetch(`/api/templates/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(template),
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<Template>
}

// テンプレートから作成済みのカードは残る
export async function deleteTemplate(id: string): Promise<void> {
  const res = await fetch(`/api/templates/${id}`, { method: 'DELETE' })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
}
