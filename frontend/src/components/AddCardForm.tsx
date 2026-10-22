import { useState } from 'react'
import { createCard } from '../api'

interface Props {
  listId: string
  onCreated: () => void
}

export default function AddCardForm({ listId, onCreated }: Props) {
  const [open, setOpen] = useState(false)
  const [title, setTitle] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const close = () => {
    setOpen(false)
    setTitle('')
    setError(null)
  }

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = title.trim()
    if (!trimmed || submitting) return
    setSubmitting(true)
    setError(null)
    try {
      await createCard(listId, trimmed)
      setTitle('')
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
