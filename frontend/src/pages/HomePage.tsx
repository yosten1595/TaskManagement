import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchBoards } from '../api'
import type { BoardSummary } from '../types'

export default function HomePage() {
  const [boards, setBoards] = useState<BoardSummary[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchBoards()
      .then(setBoards)
      .catch((e: Error) => setError(e.message))
  }, [])

  if (error) return <p className="message error">ボードを取得できませんでした: {error}</p>
  if (!boards) return <p className="message">読み込み中…</p>

  return (
    <section>
      <h1>ボード</h1>
      {boards.length === 0 ? (
        <p className="message">ボードがありません。</p>
      ) : (
        <ul className="board-grid">
          {boards.map((board) => (
            <li key={board.id}>
              <Link to={`/boards/${board.id}`} className="board-tile">
                {board.title}
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
