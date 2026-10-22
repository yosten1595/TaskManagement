import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchBoard, searchCards } from '../api'
import AddCardForm from '../components/AddCardForm'
import CardItem from '../components/CardItem'
import type { BoardDetail, Card, Priority } from '../types'

const SEARCH_DELAY_MS = 300

export default function BoardPage() {
  const { boardId } = useParams<{ boardId: string }>()
  const [board, setBoard] = useState<BoardDetail | null>(null)
  const [cards, setCards] = useState<Card[]>([])
  const [keyword, setKeyword] = useState('')
  const [priority, setPriority] = useState<Priority | ''>('')
  const [error, setError] = useState<string | null>(null)
  // カード作成後に検索をやり直すためのカウンタ
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    if (!boardId) return
    fetchBoard(boardId)
      .then(setBoard)
      .catch((e: Error) => setError(e.message))
  }, [boardId])

  // 検索条件が変わるたびにカードを検索する。キーワード入力は少し待ってから送る
  useEffect(() => {
    let cancelled = false
    const timer = setTimeout(() => {
      searchCards({ keyword: keyword.trim(), priority: priority || undefined })
        .then((result) => {
          if (!cancelled) setCards(result)
        })
        .catch((e: Error) => {
          if (!cancelled) setError(e.message)
        })
    }, SEARCH_DELAY_MS)
    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [keyword, priority, reloadKey])

  // 検索 API はボードを指定できないため、このボードのリストに属するカードだけを振り分ける
  const cardsByList = useMemo(() => {
    const map = new Map<string, Card[]>()
    for (const card of cards) {
      map.set(card.listId, [...(map.get(card.listId) ?? []), card])
    }
    return map
  }, [cards])

  if (error) return <p className="message error">取得できませんでした: {error}</p>
  if (!board) return <p className="message">読み込み中…</p>

  return (
    <section>
      <p>
        <Link to="/">← ボード一覧</Link>
      </p>
      <h1>{board.title}</h1>

      <form className="search" role="search" onSubmit={(e) => e.preventDefault()}>
        <input
          type="search"
          placeholder="タイトル・説明で検索"
          aria-label="キーワード"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <select
          aria-label="優先度"
          value={priority}
          onChange={(e) => setPriority(e.target.value as Priority | '')}
        >
          <option value="">優先度: すべて</option>
          <option value="HIGH">高</option>
          <option value="MID">中</option>
          <option value="LOW">低</option>
        </select>
      </form>

      <div className="columns">
        {board.lists.map((list) => {
          const listCards = cardsByList.get(list.id) ?? []
          return (
            <section key={list.id} className="column" aria-label={list.title}>
              <h2>
                {list.title} <span className="count">{listCards.length}</span>
              </h2>
              {listCards.map((card) => (
                <CardItem key={card.id} card={card} />
              ))}
              <AddCardForm listId={list.id} onCreated={() => setReloadKey((k) => k + 1)} />
            </section>
          )
        })}
      </div>
    </section>
  )
}
