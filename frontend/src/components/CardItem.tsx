import { useState } from 'react'
import type { Card, Label, Priority } from '../types'
import EditCardForm from './EditCardForm'

const PRIORITY_LABEL: Record<Priority, string> = { HIGH: '高', MID: '中', LOW: '低' }

interface Props {
  card: Card
  labels: Label[]
  onSaved: () => void
  // 削除モード中のみ、選択用のチェックボックスを表示する
  selectable?: boolean
  selected?: boolean
  onToggle?: () => void
}

export default function CardItem({ card, labels, onSaved, selectable = false, selected = false, onToggle }: Props) {
  const [editing, setEditing] = useState(false)
  const doneCount = card.checklist.filter((item) => item.done).length

  if (editing) {
    return (
      <EditCardForm
        card={card}
        labels={labels}
        onSaved={() => {
          setEditing(false)
          onSaved()
        }}
        onCancel={() => setEditing(false)}
      />
    )
  }

  return (
    <article
      className={`card${selected ? ' card-selected' : ''}${selectable ? '' : ' card-editable'}`}
      {...(selectable
        ? {}
        : {
            role: 'button',
            tabIndex: 0,
            'aria-label': `「${card.title}」を編集`,
            onClick: () => setEditing(true),
            onKeyDown: (e: React.KeyboardEvent) => e.key === 'Enter' && setEditing(true),
          })}
    >
      {selectable && (
        <label className="card-select">
          <input type="checkbox" checked={selected} onChange={onToggle} />
          選択
        </label>
      )}
      {card.labels.length > 0 && (
        <div className="card-labels">
          {card.labels.map((label) => (
            <span key={label.id} className="label" style={{ background: label.color }}>
              {label.name}
            </span>
          ))}
        </div>
      )}
      <h3>{card.title}</h3>
      <div className="card-meta">
        {card.priority && (
          <span className={`priority priority-${card.priority.toLowerCase()}`}>
            優先度 {PRIORITY_LABEL[card.priority]}
          </span>
        )}
        {card.dueDate && <span>期限 {card.dueDate}</span>}
        {card.checklist.length > 0 && (
          <span>
            ✓ {doneCount}/{card.checklist.length}
          </span>
        )}
      </div>
    </article>
  )
}
