import { useState } from 'react'
import { createBoard } from '../api'
import type { BoardSummary } from '../types'

interface Props {
  onCreated: (board: BoardSummary) => void
}

export default function AddBoardForm({ onCreated }: Props) {
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
      onCreated(await createBoard(trimmed))
      close()
    } catch (err) {
      setError(`作成できませんでした: ${(err as Error).message}`)
    } finally {
      setSubmitting(false)
    }
  }

  if (!open) {
    return (
      <button type="button" className="board-add-open" onClick={() => setOpen(true)}>
        ＋ボード追加
      </button>
    )
  }

  return (
    <form className="board-form" onSubmit={submit}>
      <input
        autoFocus
        aria-label="ボード名"
        placeholder="ボード名を入力"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        onKeyDown={(e) => e.key === 'Escape' && close()}
      />
      {error && <p className="message error">{error}</p>}
      <div className="board-actions">
        <button type="submit" className="primary" disabled={!title.trim() || submitting}>
          作成
        </button>
        <button type="button" disabled={submitting} onClick={close}>
          キャンセル
        </button>
      </div>
    </form>
  )
}
