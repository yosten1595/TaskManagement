import type { BoardDetail, BoardSummary, Card, CardSearchParams } from './types'

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

export async function createCard(listId: string, title: string): Promise<Card> {
  const res = await fetch('/api/cards', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ listId, title }),
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<Card>
}
