import { useEffect, useState } from 'react'
import { fetchBoards } from '../api'
import AddBoardForm from '../components/AddBoardForm'
import BoardTile from '../components/BoardTile'
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
      {boards.length === 0 && <p className="message">ボードがありません。</p>}
      <ul className="board-grid">
        {boards.map((board) => (
          <li key={board.id}>
            <BoardTile
              board={board}
              onRenamed={(renamed) => setBoards(boards.map((b) => (b.id === renamed.id ? renamed : b)))}
              onDeleted={(id) => setBoards(boards.filter((b) => b.id !== id))}
            />
          </li>
        ))}
        <li>
          <AddBoardForm onCreated={(created) => setBoards([...boards, created])} />
        </li>
      </ul>
    </section>
  )
}
