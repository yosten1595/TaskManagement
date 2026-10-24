import { useState } from 'react'
import { createList } from '../api'
import type { BoardList } from '../types'

interface Props {
  boardId: string
  onCreated: (list: BoardList) => void
}

export default function AddListForm({ boardId, onCreated }: Props) {
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
      onCreated(await createList(boardId, trimmed))
      close()
    } catch (err) {
      setError(`追加できませんでした: ${(err as Error).message}`)
    } finally {
      setSubmitting(false)
    }
  }

  if (!open) {
    return (
      <button type="button" className="list-add-open" onClick={() => setOpen(true)}>
        ＋リスト追加
      </button>
    )
  }

  return (
    <form className="list-add-form" onSubmit={submit}>
      <input
        autoFocus
        aria-label="リスト名"
        placeholder="リスト名を入力"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        onKeyDown={(e) => e.key === 'Escape' && close()}
      />
      {error && <p className="message error">{error}</p>}
      <div className="column-tools">
        <button type="submit" className="primary" disabled={!title.trim() || submitting}>
          追加
        </button>
        <button type="button" disabled={submitting} onClick={close}>
          キャンセル
        </button>
      </div>
    </form>
  )
}
