import { useState } from 'react'
import { updateCard } from '../api'
import type { Card, Label, Priority } from '../types'
import CardFields from './CardFields'

interface Props {
  card: Card
  labels: Label[]
  onSaved: () => void
  onCancel: () => void
}

export default function EditCardForm({ card, labels, onSaved, onCancel }: Props) {
  const [title, setTitle] = useState(card.title)
  const [description, setDescription] = useState(card.description ?? '')
  const [dueDate, setDueDate] = useState(card.dueDate ?? '')
  const [priority, setPriority] = useState<Priority | ''>(card.priority ?? '')
  const [labelIds, setLabelIds] = useState<string[]>(card.labels.map((l) => l.id))
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = title.trim()
    if (!trimmed || submitting) return
    setSubmitting(true)
    setError(null)
    try {
      await updateCard(card.id, {
        title: trimmed,
        description: description.trim() || null,
        dueDate: dueDate || null,
        priority: priority || null,
        labelIds,
      })
      onSaved()
    } catch (err) {
      setError(`保存できませんでした: ${(err as Error).message}`)
      setSubmitting(false)
    }
  }

  return (
    <form className="add-card card" onSubmit={submit}>
      <input
        autoFocus
        aria-label="カードのタイトル"
        placeholder="タイトルを入力"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        onKeyDown={(e) => e.key === 'Escape' && onCancel()}
      />
      <textarea
        aria-label="説明"
        placeholder="説明"
        rows={3}
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        onKeyDown={(e) => e.key === 'Escape' && onCancel()}
      />
      <CardFields
        labels={labels}
        dueDate={dueDate}
        priority={priority}
        labelIds={labelIds}
        onDueDateChange={setDueDate}
        onPriorityChange={setPriority}
        onLabelIdsChange={setLabelIds}
      />
      {error && <p className="message error">{error}</p>}
      <div className="add-card-actions">
        <button type="submit" disabled={!title.trim() || submitting}>
          保存
        </button>
        <button type="button" disabled={submitting} onClick={onCancel}>
          キャンセル
        </button>
      </div>
    </form>
  )
}
