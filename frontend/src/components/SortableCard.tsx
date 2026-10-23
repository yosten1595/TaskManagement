import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { useState } from 'react'
import type { Card, Label } from '../types'
import CardItem from './CardItem'

interface Props {
  card: Card
  labels: Label[]
  onSaved: () => void
  // false の間（削除モード・絞り込み中）はドラッグできない
  dragEnabled: boolean
  selectable: boolean
  selected: boolean
  onToggle: () => void
}

// CardItem をドラッグ可能にするラッパー。編集フォームを開いている間はドラッグしない
export default function SortableCard({ card, dragEnabled, ...itemProps }: Props) {
  const [editing, setEditing] = useState(false)
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: card.id,
    disabled: !dragEnabled || editing,
  })

  // role / tabIndex は内側の CardItem（クリックで編集）が持つので、ラッパーには付けない
  const { role: _role, tabIndex: _tabIndex, ...ariaAttributes } = attributes

  return (
    <div
      ref={setNodeRef}
      className={isDragging ? 'sortable-dragging' : undefined}
      style={{ transform: CSS.Transform.toString(transform), transition }}
      {...ariaAttributes}
      {...listeners}
    >
      <CardItem card={card} {...itemProps} onEditingChange={setEditing} />
    </div>
  )
}
