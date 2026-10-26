import { useCallback, useEffect, useState } from 'react'
import { fetchBoards } from '../api'
import AddBoardForm from '../components/AddBoardForm'
import BackupControls from '../components/BackupControls'
import BoardTile from '../components/BoardTile'
import type { BoardSummary } from '../types'

export default function HomePage() {
  const [boards, setBoards] = useState<BoardSummary[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(
    (signal?: AbortSignal) =>
      fetchBoards(signal)
        .then(setBoards)
        .catch((e: Error) => {
          if (!signal?.aborted) setError(e.message)
        }),
    [],
  )

  // 取得中に画面を離れたら中断し、結果は反映しない
  useEffect(() => {
    const controller = new AbortController()
    load(controller.signal)
    return () => controller.abort()
  }, [load])

  if (error) return <p className="message error">ボードを取得できませんでした: {error}</p>
  if (!boards) return <p className="message">読み込み中…</p>

  return (
    <section>
      <div className="home-head">
        <h1>ボード</h1>
        <BackupControls onImported={load} />
      </div>
      {boards.length === 0 && <p className="message">ボードがありません。</p>}
      <ul className="board-grid">
        {boards.map((board) => (
          <li key={board.id}>
            <BoardTile
              board={board}
              onRenamed={(renamed) => setBoards(boards.map((b) => (b.id === renamed.id ? renamed : b)))}
              onDeleted={(id) => setBoards(boards.filter((b) => b.id !== id))}
              onRestored={load}
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
