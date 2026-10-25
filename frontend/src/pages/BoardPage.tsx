import {
  closestCorners,
  DndContext,
  DragOverlay,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
  type DragOverEvent,
  type DragStartEvent,
} from '@dnd-kit/core'
import { arrayMove, sortableKeyboardCoordinates } from '@dnd-kit/sortable'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { deleteCard, fetchBoard, fetchLabels, moveCard, searchCards, sortList } from '../api'
import AddCardForm from '../components/AddCardForm'
import CardColumn from '../components/CardColumn'
import CardItem from '../components/CardItem'
import SortableCard from '../components/SortableCard'
import type { BoardDetail, Card, CardSortKey, Label, Priority } from '../types'

const SEARCH_DELAY_MS = 300

// このタイトルのリストに置かれたカードは完了扱いにする
const DONE_LIST_TITLE = '完了'

// リスト ID → そのリストのカード（position 順）
type Columns = Record<string, Card[]>

function findContainer(columns: Columns, id: string): string | null {
  if (id in columns) return id
  return Object.keys(columns).find((listId) => columns[listId].some((c) => c.id === id)) ?? null
}

export default function BoardPage() {
  const { boardId } = useParams<{ boardId: string }>()
  const [board, setBoard] = useState<BoardDetail | null>(null)
  const [cards, setCards] = useState<Card[]>([])
  const [labels, setLabels] = useState<Label[]>([])
  const [keyword, setKeyword] = useState('')
  const [priority, setPriority] = useState<Priority | ''>('')
  const [labelId, setLabelId] = useState('')
  // 期限で絞り込む日付（YYYY-MM-DD）。空なら絞り込まない
  const [dueDate, setDueDate] = useState('')
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
      searchCards({ keyword: keyword.trim(), priority: priority || undefined, labelId: labelId || undefined })
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
  }, [keyword, priority, labelId, reloadKey])

  // 検索 API はボードを指定できないため、このボードのリストに属するカードだけを振り分ける
  const cardsByList = useMemo(() => {
    const map = new Map<string, Card[]>()
    for (const card of cards) {
      if (dueDate && card.dueDate !== dueDate) continue
      map.set(card.listId, [...(map.get(card.listId) ?? []), card])
    }
    return map
  }, [cards, dueDate])

  // ドラッグ＆ドロップ: ドラッグ中だけ列ごとのカード並びを一時的に持つ。
  // 絞り込み中は一部のカードしか表示されず、挿入位置が実際の position とずれるので無効にする
  const filtering = keyword.trim() !== '' || priority !== '' || labelId !== '' || dueDate !== ''
  const dragEnabled = !deleteMode && !filtering
  const baseColumns = useMemo(() => {
    const record: Columns = {}
    for (const list of board?.lists ?? []) record[list.id] = cardsByList.get(list.id) ?? []
    return record
  }, [board, cardsByList])
  const [dragColumns, setDragColumns] = useState<Columns | null>(null)
  const [activeCard, setActiveCard] = useState<Card | null>(null)
  const [moveError, setMoveError] = useState<string | null>(null)
  const dragOrigin = useRef<{ listId: string; index: number } | null>(null)
  const columns = dragColumns ?? baseColumns

  const sensors = useSensors(
    // 少し動かしてからドラッグ扱いにして、単なるクリック（編集）と区別する
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    // Enter はクリックと同じく編集に使うので、キーボードのドラッグは Space で開始・確定する
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
      keyboardCodes: { start: ['Space'], cancel: ['Escape'], end: ['Space'] },
    }),
  )

  const handleDragStart = ({ active }: DragStartEvent) => {
    const id = String(active.id)
    const listId = findContainer(baseColumns, id)
    if (!listId) return
    setMoveError(null)
    setActiveCard(baseColumns[listId].find((c) => c.id === id) ?? null)
    dragOrigin.current = { listId, index: baseColumns[listId].findIndex((c) => c.id === id) }
    setDragColumns(baseColumns)
  }

  // 別の列の上に来たら、その列の位置にカードを仮挿入する
  const handleDragOver = ({ active, over }: DragOverEvent) => {
    if (!over) return
    setDragColumns((prev) => {
      if (!prev) return prev
      const activeId = String(active.id)
      const from = findContainer(prev, activeId)
      const to = findContainer(prev, String(over.id))
      if (!from || !to || from === to) return prev
      const moving = prev[from].find((c) => c.id === activeId)
      if (!moving) return prev
      let index = prev[to].length
      if (String(over.id) !== to) {
        const overIndex = prev[to].findIndex((c) => c.id === String(over.id))
        const translated = active.rect.current.translated
        const below = translated != null && translated.top > over.rect.top + over.rect.height / 2
        index = overIndex + (below ? 1 : 0)
      }
      return {
        ...prev,
        [from]: prev[from].filter((c) => c.id !== activeId),
        [to]: [...prev[to].slice(0, index), { ...moving, listId: to }, ...prev[to].slice(index)],
      }
    })
  }

  const handleDragEnd = async ({ active, over }: DragEndEvent) => {
    const origin = dragOrigin.current
    dragOrigin.current = null
    setActiveCard(null)
    const activeId = String(active.id)
    let next = dragColumns
    if (next && over) {
      const listId = findContainer(next, activeId)
      if (listId) {
        const oldIndex = next[listId].findIndex((c) => c.id === activeId)
        const overId = String(over.id)
        const newIndex = overId === listId ? next[listId].length - 1 : next[listId].findIndex((c) => c.id === overId)
        if (newIndex >= 0 && oldIndex !== newIndex) {
          next = { ...next, [listId]: arrayMove(next[listId], oldIndex, newIndex) }
        }
      }
    }
    const listId = next ? findContainer(next, activeId) : null
    const index = listId && next ? next[listId].findIndex((c) => c.id === activeId) : -1
    if (!next || !over || !origin || !listId || (listId === origin.listId && index === origin.index)) {
      setDragColumns(null)
      return
    }
    setDragColumns(next)
    try {
      await moveCard(activeId, listId, index)
    } catch (e) {
      setMoveError(`移動できませんでした: ${(e as Error).message}`)
    }
    // 成功・失敗どちらでも、サーバー上の並びを取り直して表示を合わせる
    try {
      setCards(await searchCards())
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setDragColumns(null)
    }
  }

  // 並べ替えはサーバー側で position に保存される。終わったら全カードを取り直して表示を合わせる
  const handleSort = async (listId: string, by: CardSortKey) => {
    setMoveError(null)
    try {
      await sortList(listId, by)
    } catch (e) {
      setMoveError(`並べ替えできませんでした: ${(e as Error).message}`)
    }
    try {
      setCards(await searchCards())
    } catch (e) {
      setError((e as Error).message)
    }
  }

  const handleDragCancel = () => {
    dragOrigin.current = null
    setActiveCard(null)
    setDragColumns(null)
  }

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
        <select aria-label="ラベル" value={labelId} onChange={(e) => setLabelId(e.target.value)}>
          <option value="">ラベル: すべて</option>
          {labels.map((label) => (
            <option key={label.id} value={label.id}>
              {label.name}
            </option>
          ))}
        </select>
        <label className="search-due">
          期限
          <input type="date" aria-label="期限" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
        </label>
      </form>


      {filtering && <p className="hint">検索・絞り込み中は、カードの移動・並べ替えはできません。</p>}
      {moveError && <p className="message error">{moveError}</p>}

      <DndContext
        sensors={sensors}
        collisionDetection={closestCorners}
        onDragStart={handleDragStart}
        onDragOver={handleDragOver}
        onDragEnd={handleDragEnd}
        onDragCancel={handleDragCancel}
      >
        <div className="columns">
          {board.lists.map((list) => {
            const listCards = columns[list.id] ?? []
            return (
              <CardColumn
                key={list.id}
                listId={list.id}
                title={list.title}
                cards={listCards}
                onSort={(by) => handleSort(list.id, by)}
                sortDisabled={filtering}
                footer={
                  <AddCardForm listId={list.id} labels={labels} onCreated={() => setReloadKey((k) => k + 1)} />
                }
              >
                {listCards.map((card) => (
                  <SortableCard
                    key={card.id}
                    card={card}
                    labels={labels}
                    onSaved={() => setReloadKey((k) => k + 1)}
                    dragEnabled={dragEnabled}
                    selectable={deleteMode}
                    selected={selectedIds.has(card.id)}
                    onToggle={() => toggleSelected(card.id)}
                    completed={list.title === DONE_LIST_TITLE}
                  />
                ))}
              </CardColumn>
            )
          })}
        </div>
        <DragOverlay>
          {activeCard && (
            <div className="drag-overlay">
              <CardItem
                card={activeCard}
                labels={labels}
                onSaved={() => {}}
                completed={board?.lists.find((l) => l.id === findContainer(columns, activeCard.id))?.title === DONE_LIST_TITLE}
              />
            </div>
          )}
        </DragOverlay>
      </DndContext>

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
