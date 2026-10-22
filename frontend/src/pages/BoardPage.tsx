import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { deleteCard, fetchBoard, fetchLabels, searchCards } from '../api'
import AddCardForm from '../components/AddCardForm'
import CardItem from '../components/CardItem'
import type { BoardDetail, Card, Label, Priority } from '../types'

const SEARCH_DELAY_MS = 300

export default function BoardPage() {
  const { boardId } = useParams<{ boardId: string }>()
  const [board, setBoard] = useState<BoardDetail | null>(null)
  const [cards, setCards] = useState<Card[]>([])
  const [labels, setLabels] = useState<Label[]>([])
  const [keyword, setKeyword] = useState('')
  const [priority, setPriority] = useState<Priority | ''>('')
  const [error, setError] = useState<string | null>(null)
  // カード作成後に検索をやり直すためのカウンタ
  const [reloadKey, setReloadKey] = useState(0)
  // 削除モード: カードを選択してまとめて削除する
  const [deleteMode, setDeleteMode] = useState(false)
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set())
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  useEffect(() => {
    if (!boardId) return
    fetchBoard(boardId)
      .then(setBoard)
      .catch((e: Error) => setError(e.message))
  }, [boardId])

  useEffect(() => {
    fetchLabels()
      .then(setLabels)
      .catch((e: Error) => setError(e.message))
  }, [])

  // 検索条件が変わるたびにカードを検索する。キーワード入力は少し待ってから送る
  useEffect(() => {
    let cancelled = false
    const timer = setTimeout(() => {
      searchCards({ keyword: keyword.trim(), priority: priority || undefined })
        .then((result) => {
          if (!cancelled) setCards(result)
        })
        .catch((e: Error) => {
          if (!cancelled) setError(e.message)
        })
    }, SEARCH_DELAY_MS)
    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [keyword, priority, reloadKey])

  // 検索 API はボードを指定できないため、このボードのリストに属するカードだけを振り分ける
  const cardsByList = useMemo(() => {
    const map = new Map<string, Card[]>()
    for (const card of cards) {
      map.set(card.listId, [...(map.get(card.listId) ?? []), card])
    }
    return map
  }, [cards])

  const exitDeleteMode = () => {
    setDeleteMode(false)
    setSelectedIds(new Set())
    setDeleteError(null)
  }

  const toggleSelected = (id: string) =>
    setSelectedIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })

  // 同一リストの position の詰め直しが競合しないよう、1 件ずつ順に削除する
  const deleteSelected = async () => {
    if (selectedIds.size === 0) return
    if (!window.confirm(`選択した ${selectedIds.size} 件のカードを削除しますか？`)) return
    setDeleting(true)
    setDeleteError(null)
    const failed = new Set<string>()
    for (const id of selectedIds) {
      try {
        await deleteCard(id)
      } catch {
        failed.add(id)
      }
    }
    setReloadKey((k) => k + 1)
    setDeleting(false)
    if (failed.size === 0) {
      exitDeleteMode()
    } else {
      setSelectedIds(failed)
      setDeleteError(`${failed.size} 件を削除できませんでした`)
    }
  }

  if (error) return <p className="message error">取得できませんでした: {error}</p>
  if (!board) return <p className="message">読み込み中…</p>

  return (
    <section>
      <p>
        <Link to="/">← ボード一覧</Link>
      </p>
      <h1>{board.title}</h1>

      <form className="search" role="search" onSubmit={(e) => e.preventDefault()}>
        <input
          type="search"
          placeholder="タイトル・説明で検索"
          aria-label="キーワード"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <select
          aria-label="優先度"
          value={priority}
          onChange={(e) => setPriority(e.target.value as Priority | '')}
        >
          <option value="">優先度: すべて</option>
          <option value="HIGH">高</option>
          <option value="MID">中</option>
          <option value="LOW">低</option>
        </select>
      </form>

      <div className="columns">
        {board.lists.map((list) => {
          const listCards = cardsByList.get(list.id) ?? []
          return (
            <section key={list.id} className="column" aria-label={list.title}>
              <h2>
                {list.title} <span className="count">{listCards.length}</span>
              </h2>
              {listCards.map((card) => (
                <CardItem
                  key={card.id}
                  card={card}
                  selectable={deleteMode}
                  selected={selectedIds.has(card.id)}
                  onToggle={() => toggleSelected(card.id)}
                />
              ))}
              <AddCardForm listId={list.id} labels={labels} onCreated={() => setReloadKey((k) => k + 1)} />
            </section>
          )
        })}
      </div>

      <div className="delete-bar">
        {deleteError && <span className="message error">{deleteError}</span>}
        {deleteMode ? (
          <>
            <button
              type="button"
              className="delete-run"
              disabled={selectedIds.size === 0 || deleting}
              onClick={deleteSelected}
            >
              {selectedIds.size} 件を削除
            </button>
            <button type="button" disabled={deleting} onClick={exitDeleteMode}>
              キャンセル
            </button>
          </>
        ) : (
          <button type="button" onClick={() => setDeleteMode(true)}>
            削除
          </button>
        )}
      </div>
    </section>
  )
}
