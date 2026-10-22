import { useState } from 'react'
import { createCard } from '../api'
import type { Label, Priority } from '../types'

interface Props {
  listId: string
  labels: Label[]
  onCreated: () => void
}

export default function AddCardForm({ listId, labels, onCreated }: Props) {
  const [open, setOpen] = useState(false)
  const [title, setTitle] = useState('')
  const [dueDate, setDueDate] = useState('')
  const [priority, setPriority] = useState<Priority | ''>('')
  const [labelIds, setLabelIds] = useState<string[]>([])
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const reset = () => {
    setTitle('')
    setDueDate('')
    setPriority('')
    setLabelIds([])
    setError(null)
  }

  const close = () => {
    setOpen(false)
    reset()
  }

  const toggleLabel = (id: string) =>
    setLabelIds((ids) => (ids.includes(id) ? ids.filter((x) => x !== id) : [...ids, id]))

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = title.trim()
    if (!trimmed || submitting) return
    setSubmitting(true)
    setError(null)
    try {
      await createCard(listId, {
        title: trimmed,
        dueDate: dueDate || undefined,
        priority: priority || undefined,
        labelIds: labelIds.length > 0 ? labelIds : undefined,
      })
      reset()
      onCreated()
    } catch (err) {
      setError(`作成できませんでした: ${(err as Error).message}`)
    } finally {
      setSubmitting(false)
    }
  }

  if (!open) {
    return (
      <button type="button" className="add-card-open" onClick={() => setOpen(true)}>
        ＋ カードを追加
      </button>
    )
  }

  return (
    <form className="add-card" onSubmit={submit}>
      <input
        autoFocus
        aria-label="カードのタイトル"
        placeholder="タイトルを入力"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        onKeyDown={(e) => e.key === 'Escape' && close()}
      />
      <div className="add-card-fields">
        <label>
          期限
          <input type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
        </label>
        <label>
          優先度
          <select value={priority} onChange={(e) => setPriority(e.target.value as Priority | '')}>
            <option value="">未設定</option>
            <option value="HIGH">高</option>
            <option value="MID">中</option>
            <option value="LOW">低</option>
          </select>
        </label>
      </div>
      {labels.length > 0 && (
        <fieldset className="add-card-labels">
          <legend>ラベル</legend>
          {labels.map((label) => (
            <label key={label.id} className="label-choice">
              <input
                type="checkbox"
                checked={labelIds.includes(label.id)}
                onChange={() => toggleLabel(label.id)}
              />
              <span className="label" style={{ background: label.color }}>
                {label.name}
              </span>
            </label>
          ))}
        </fieldset>
      )}
      {error && <p className="message error">{error}</p>}
      <div className="add-card-actions">
        <button type="submit" disabled={!title.trim() || submitting}>
          追加
        </button>
        <button type="button" onClick={close}>
          キャンセル
        </button>
      </div>
    </form>
  )
}
