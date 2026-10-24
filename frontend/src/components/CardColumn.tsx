import { useDroppable } from '@dnd-kit/core'
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable'
import { useState } from 'react'
import type { ReactNode } from 'react'
import type { Card, CardSortKey } from '../types'

interface Props {
  listId: string
  title: string
  cards: Card[]
  children: ReactNode
  footer: ReactNode
  onSort: (by: CardSortKey) => void
  sortDisabled: boolean
  // 名前変更を確定する。実行できたら true、取りやめなら false（入力欄は開いたままにする）。失敗時は例外
  onRename: (title: string) => Promise<boolean>
  onDelete: () => Promise<void>
  deleteDisabled: boolean
}

// リスト 1 列。カードが 0 件でもドロップできるよう、列自体を droppable にする
export default function CardColumn({
  listId,
  title,
  cards,
  children,
  footer,
  onSort,
  sortDisabled,
  onRename,
  onDelete,
  deleteDisabled,
}: Props) {
  const { setNodeRef } = useDroppable({ id: listId })
  const [renaming, setRenaming] = useState(false)
  const [draft, setDraft] = useState(title)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const startRename = () => {
    setDraft(title)
    setError(null)
    setRenaming(true)
  }

  const submitRename = async (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = draft.trim()
    if (!trimmed || busy) return
    if (trimmed === title) {
      setRenaming(false)
      return
    }
    setBusy(true)
    setError(null)
    try {
      if (await onRename(trimmed)) setRenaming(false)
    } catch (err) {
      setError(`名前を変更できませんでした: ${(err as Error).message}`)
    } finally {
      setBusy(false)
    }
  }

  const remove = async () => {
    if (busy) return
    setBusy(true)
    setError(null)
    try {
      await onDelete()
    } catch (err) {
      setError(`削除できませんでした: ${(err as Error).message}`)
    } finally {
      setBusy(false)
    }
  }

  return (
    <section ref={setNodeRef} className="column" aria-label={title}>
      {renaming ? (
        <form className="column-rename" onSubmit={submitRename}>
          <input
            autoFocus
            aria-label="リスト名"
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
            onKeyDown={(e) => e.key === 'Escape' && setRenaming(false)}
          />
          <div className="column-tools">
            <button type="submit" className="primary" disabled={!draft.trim() || busy}>
              保存
            </button>
            <button type="button" disabled={busy} onClick={() => setRenaming(false)}>
              キャンセル
            </button>
          </div>
        </form>
      ) : (
        <>
          <div className="column-head">
            <h2>
              {title} <span className="count">{cards.length}</span>
            </h2>
            {/* 選ぶと並べ替えを実行して position に保存する。「手動順」は置かず、選択後は初期表示に戻す */}
            <select
              className="column-sort"
              aria-label={`${title}を並べ替え`}
              value=""
              disabled={sortDisabled || cards.length < 2}
              onChange={(e) => e.target.value && onSort(e.target.value as CardSortKey)}
            >
              <option value="">並べ替え</option>
              <option value="DUE_DATE">期限が近い順</option>
              <option value="PRIORITY">優先度が高い順</option>
            </select>
          </div>
          <div className="column-tools">
            <button type="button" disabled={busy} onClick={startRename}>
              名前変更
            </button>
            <button
              type="button"
              className="danger"
              disabled={busy || deleteDisabled}
              title={deleteDisabled ? '検索・絞り込み中は削除できません' : undefined}
              onClick={remove}
            >
              削除
            </button>
          </div>
        </>
      )}
      {error && <p className="message error">{error}</p>}
      <SortableContext items={cards.map((c) => c.id)} strategy={verticalListSortingStrategy}>
        <div className="column-cards">{children}</div>
      </SortableContext>
      {footer}
    </section>
  )
}
