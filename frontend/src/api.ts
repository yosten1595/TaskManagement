import type { BoardDetail, BoardSummary, Card, CardSearchParams, CardSortKey, CardUpdate, Label, NewCard, NewTemplate, Template } from './types'

// fetch を呼ぶのはここだけ。body は JSON にして送る（文字列はそのまま送る）
function send(url: string, method: string = 'GET', body?: unknown, signal?: AbortSignal): Promise<Response> {
  return fetch(url, {
    method,
    signal,
    ...(body === undefined
      ? {}
      : { headers: { 'Content-Type': 'application/json' }, body: typeof body === 'string' ? body : JSON.stringify(body) }),
  })
}

// 失敗（2xx 以外）は `状態コード 状態テキスト` のメッセージの Error にする
async function request(url: string, method?: string, body?: unknown, signal?: AbortSignal): Promise<Response> {
  const res = await send(url, method, body, signal)
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res
}

async function requestJson<T>(url: string, method?: string, body?: unknown, signal?: AbortSignal): Promise<T> {
  return (await request(url, method, body, signal)).json() as Promise<T>
}

// 取得（GET）。画面を離れるときに signal で中断できる
const getJson = <T>(url: string, signal?: AbortSignal) => requestJson<T>(url, 'GET', undefined, signal)

export const fetchBoards = (signal?: AbortSignal) => getJson<BoardSummary[]>('/api/boards', signal)

export const fetchBoard = (id: string, signal?: AbortSignal) => getJson<BoardDetail>(`/api/boards/${id}`, signal)

export function searchCards({ keyword, priority, listId, labelId, boardId }: CardSearchParams = {}, signal?: AbortSignal) {
  const params = new URLSearchParams()
  if (keyword) params.set('keyword', keyword)
  if (priority) params.set('priority', priority)
  if (listId) params.set('listId', listId)
  if (labelId) params.set('labelId', labelId)
  if (boardId) params.set('boardId', boardId)
  const query = params.toString()
  return getJson<Card[]>(`/api/cards${query ? `?${query}` : ''}`, signal)
}

export const fetchCard = (id: string) => requestJson<Card>(`/api/cards/${id}`)

export const fetchLabels = (signal?: AbortSignal) => getJson<Label[]>('/api/labels', signal)

export const createCard = (listId: string, card: NewCard) => requestJson<Card>('/api/cards', 'POST', { listId, ...card })

export const deleteCard = async (id: string): Promise<void> => void (await request(`/api/cards/${id}`, 'DELETE'))

export const updateCard = (id: string, card: CardUpdate) => requestJson<Card>(`/api/cards/${id}`, 'PUT', card)

// position は移動先リスト内での 0 始まりの挿入位置
export const moveCard = (id: string, listId: string, position: number) =>
  requestJson<Card>(`/api/cards/${id}/move`, 'PUT', { listId, position })

// 並べ替え結果は position に保存され、並べ替え後のカード一覧が返る
export const sortList = (listId: string, by: CardSortKey) => requestJson<Card[]>(`/api/lists/${listId}/sort`, 'PUT', { by })

// チェックリストの追加・更新は、更新後のカードが返る
export const addCheckItem = (cardId: string, text: string) =>
  requestJson<Card>(`/api/cards/${cardId}/checklist`, 'POST', { text })

export const updateCheckItem = (cardId: string, itemId: string, text: string, done: boolean) =>
  requestJson<Card>(`/api/cards/${cardId}/checklist/${itemId}`, 'PUT', { text, done })

export const deleteCheckItem = async (cardId: string, itemId: string): Promise<void> =>
  void (await request(`/api/cards/${cardId}/checklist/${itemId}`, 'DELETE'))

export const createBoard = (title: string) => requestJson<BoardSummary>('/api/boards', 'POST', { title })

export const renameBoard = (id: string, title: string) => requestJson<BoardSummary>(`/api/boards/${id}`, 'PUT', { title })

// ボードに属するリスト・カードも一緒に削除される
export const deleteBoard = async (id: string): Promise<void> => void (await request(`/api/boards/${id}`, 'DELETE'))

export const fetchTemplates = (signal?: AbortSignal) => getJson<Template[]>('/api/templates', signal)

export const createTemplate = (template: NewTemplate) => requestJson<Template>('/api/templates', 'POST', template)

// テンプレートの内容をコピーして、指定リストの末尾にカードを作る
export const createCardFromTemplate = (templateId: string, listId: string) =>
  requestJson<Card>(`/api/templates/${templateId}/cards`, 'POST', { listId })

export const updateTemplate = (id: string, template: NewTemplate) =>
  requestJson<Template>(`/api/templates/${id}`, 'PUT', template)

// テンプレートから作成済みのカードは残る
export const deleteTemplate = async (id: string): Promise<void> => void (await request(`/api/templates/${id}`, 'DELETE'))

// バックアップ用。中身はサーバーが決める形式なので、画面では解釈せず、そのまま保存・送信する
export async function exportBackup(): Promise<string> {
  return (await request('/api/export')).text()
}

export interface ImportResult {
  boards: number
  cards: number
  templates: number
}

// 既存のデータは消さず、ファイルの内容を新しいボード・テンプレートとして追加する。不正なファイルは 400 で、何も追加されない。
// 400 だけ専用の文言にするので、結果の確認は自分で行う
export async function importBackup(json: string): Promise<ImportResult> {
  const res = await send('/api/import', 'POST', json)
  if (res.status === 400) {
    throw new Error('ファイルの形式が正しくありません')
  }
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<ImportResult>
}
