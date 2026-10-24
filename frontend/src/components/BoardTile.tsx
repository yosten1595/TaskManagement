import { useState } from 'react'
import { Link } from 'react-router-dom'
import { deleteBoard, renameBoard } from '../api'
import type { BoardSummary } from '../types'

interface Props {
  board: BoardSummary
  onRenamed: (board: BoardSummary) => void
  onDeleted: (id: string) => void
}

export default function BoardTile({ board, onRenamed, onDeleted }: Props) {
  const [renaming, setRenaming] = useState(false)
  const [title, setTitle] = useState(board.title)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const startRename = () => {
    setTitle(board.title)
    setError(null)
    setRenaming(true)
  }

  const submitRename = async (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = title.trim()
    if (!trimmed || busy) return
    setBusy(true)
    setError(null)
    try {
      onRenamed(await renameBoard(board.id, trimmed))
      setRenaming(false)
    } catch (err) {
      setError(`名前を変更できませんでした: ${(err as Error).message}`)
    } finally {
      setBusy(false)
    }
  }

  const remove = async () => {
    if (busy) return
    if (!window.confirm(`ボード「${board.title}」を削除しますか？ リスト・カードもすべて削除されます。`)) return
    setBusy(true)
    setError(null)
    try {
      await deleteBoard(board.id)
      onDeleted(board.id)
    } catch (err) {
      setError(`削除できませんでした: ${(err as Error).message}`)
      setBusy(false)
    }
  }

  if (renaming) {
    return (
      <form className="board-form" onSubmit={submitRename}>
        <input
          autoFocus
          aria-label="ボード名"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          onKeyDown={(e) => e.key === 'Escape' && setRenaming(false)}
        />
        {error && <p className="message error">{error}</p>}
        <div className="board-actions">
          <button type="submit" className="primary" disabled={!title.trim() || busy}>
            保存
          </button>
          <button type="button" disabled={busy} onClick={() => setRenaming(false)}>
            キャンセル
          </button>
        </div>
      </form>
    )
  }

  return (
    <div className="board-card">
      <Link to={`/boards/${board.id}`} className="board-tile">
        {board.title}
      </Link>
      <div className="board-actions">
        <button type="button" disabled={busy} onClick={startRename}>
          名前変更
        </button>
        <button type="button" className="danger" disabled={busy} onClick={remove}>
          削除
        </button>
      </div>
      {error && <p className="message error">{error}</p>}
    </div>
  )
}
