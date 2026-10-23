import { useDroppable } from '@dnd-kit/core'
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable'
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
}

// リスト 1 列。カードが 0 件でもドロップできるよう、列自体を droppable にする
export default function CardColumn({ listId, title, cards, children, footer, onSort, sortDisabled }: Props) {
  const { setNodeRef } = useDroppable({ id: listId })

  return (
    <section ref={setNodeRef} className="column" aria-label={title}>
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
      <SortableContext items={cards.map((c) => c.id)} strategy={verticalListSortingStrategy}>
        <div className="column-cards">{children}</div>
      </SortableContext>
      {footer}
    </section>
  )
}
