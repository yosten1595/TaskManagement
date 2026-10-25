import { useState } from 'react'
import { createPortal } from 'react-dom'
import type { Card, Label, Priority } from '../types'
import EditCardForm from './EditCardForm'

const PRIORITY_LABEL: Record<Priority, string> = { HIGH: '高', MID: '中', LOW: '低' }

type DueStatus = 'overdue' | 'soon' | 'ok' | 'done'

const DUE_STATUS_LABEL: Record<DueStatus, string> = { overdue: '期限切れ', soon: '期限間近', ok: '余裕あり', done: '完了' }

// dueDate は YYYY-MM-DD。ブラウザのローカル日付の 0 時同士で日数差を取る
function dueStatus(dueDate: string, warnDays: number): Exclude<DueStatus, 'done'> {
  const [y, m, d] = dueDate.split('-').map(Number)
  const today = new Date()
  const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  const days = Math.round((new Date(y, m - 1, d).getTime() - todayStart.getTime()) / 86_400_000)
  if (days < 0) return 'overdue'
  return days <= warnDays ? 'soon' : 'ok'
}

interface Props {
  card: Card
  labels: Label[]
  onSaved: () => void
  // 編集フォームの開閉を親に知らせる（編集中はドラッグを無効にするため）
  onEditingChange?: (editing: boolean) => void
  // 削除モード中のみ、選択用のチェックボックスを表示する
  selectable?: boolean
  selected?: boolean
  onToggle?: () => void
  // 完了リストのカード。期限の警告表示の代わりに「完了」と表示する
  completed?: boolean
}

export default function CardItem({
  card,
  labels,
  onSaved,
  onEditingChange,
  selectable = false,
  selected = false,
  onToggle,
  completed = false,
}: Props) {
  const [editing, setEditingState] = useState(false)
  const setEditing = (value: boolean) => {
    setEditingState(value)
    onEditingChange?.(value)
  }
  // 完了リストでは期限の有無にかかわらず「完了」、それ以外は期限がある場合のみ状態を表示する
  const due: DueStatus | null = completed ? 'done' : card.dueDate ? dueStatus(card.dueDate, card.warnDays) : null
  const doneCount = card.checklist.filter((item) => item.done).length

  return (
    <>
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
          {due && (
            <span className={`due due-${due}`}>
              {card.dueDate ? `期限 ${card.dueDate}（${DUE_STATUS_LABEL[due]}）` : DUE_STATUS_LABEL[due]}
            </span>
          )}
          {card.checklist.length > 0 && (
            <span>
              ✓ {doneCount}/{card.checklist.length}
            </span>
          )}
        </div>
      </article>
      {editing &&
      createPortal(
        <div className="modal-backdrop">
          <div className="modal" role="dialog" aria-modal="true" aria-label={`「${card.title}」の詳細`}>
            <EditCardForm
              card={card}
              labels={labels}
              onSaved={() => {
                setEditing(false)
                onSaved()
              }}
              onCancel={() => setEditing(false)}
            />
          </div>
        </div>,
        document.body,
      )}
    </>
  )
}
