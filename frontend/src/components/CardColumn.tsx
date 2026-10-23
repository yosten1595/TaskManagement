import { useDroppable } from '@dnd-kit/core'
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable'
import type { ReactNode } from 'react'
import type { Card } from '../types'

interface Props {
  listId: string
  title: string
  cards: Card[]
  children: ReactNode
  footer: ReactNode
}

// リスト 1 列。カードが 0 件でもドロップできるよう、列自体を droppable にする
export default function CardColumn({ listId, title, cards, children, footer }: Props) {
  const { setNodeRef } = useDroppable({ id: listId })

  return (
    <section ref={setNodeRef} className="column" aria-label={title}>
      <h2>
        {title} <span className="count">{cards.length}</span>
      </h2>
      <SortableContext items={cards.map((c) => c.id)} strategy={verticalListSortingStrategy}>
        <div className="column-cards">{children}</div>
      </SortableContext>
      {footer}
    </section>
  )
}
