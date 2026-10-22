import type { BoardDetail, BoardSummary, Card, CardSearchParams, CardUpdate, Label, NewCard } from './types'

async function getJson<T>(url: string): Promise<T> {
  const res = await fetch(url)
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}: ${url}`)
  }
  return res.json() as Promise<T>
}

export const fetchBoards = () => getJson<BoardSummary[]>('/api/boards')

export const fetchBoard = (id: string) => getJson<BoardDetail>(`/api/boards/${id}`)

export function searchCards({ keyword, priority, listId }: CardSearchParams = {}) {
  const params = new URLSearchParams()
  if (keyword) params.set('keyword', keyword)
  if (priority) params.set('priority', priority)
  if (listId) params.set('listId', listId)
  const query = params.toString()
  return getJson<Card[]>(`/api/cards${query ? `?${query}` : ''}`)
}

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
