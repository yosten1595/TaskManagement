import { useState } from 'react'
import { addCheckItem, deleteCheckItem, updateCheckItem } from '../api'
import type { CheckItem } from '../types'

interface Props {
  cardId: string
  items: CheckItem[]
  // 項目を変更したとき（呼び出し側で閉じた後にボードを再読み込みするため）
  onChanged: (items: CheckItem[]) => void
}

// カード編集フォームの中に置くため、<form> は使わず Enter キーで追加する。操作は API に即時反映される
export default function ChecklistEditor({ cardId, items, onChanged }: Props) {
  const [text, setText] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const run = async (action: () => Promise<CheckItem[]>, failure: string) => {
    if (busy) return
    setBusy(true)
    setError(null)
    try {
      onChanged(await action())
    } catch (err) {
      setError(`${failure}: ${(err as Error).message}`)
    } finally {
      setBusy(false)
    }
  }

  const add = async () => {
    const trimmed = text.trim()
    if (!trimmed) return
    await run(async () => {
      const card = await addCheckItem(cardId, trimmed)
      setText('')
      return card.checklist
    }, '追加できませんでした')
  }

  const toggle = (item: CheckItem) =>
    run(async () => (await updateCheckItem(cardId, item.id, item.text, !item.done)).checklist, '更新できませんでした')

  const remove = (item: CheckItem) =>
    run(async () => {
      await deleteCheckItem(cardId, item.id)
      return items.filter((i) => i.id !== item.id)
    }, '削除できませんでした')

  const doneCount = items.filter((i) => i.done).length

  return (
    <fieldset className="checklist">
      <legend>
        チェックリスト{items.length > 0 && `（${doneCount}/${items.length} 完了）`}
      </legend>
      {items.length > 0 && (
        <ul>
          {items.map((item) => (
            <li key={item.id}>
              <label className={item.done ? 'checklist-done' : undefined}>
                <input type="checkbox" checked={item.done} disabled={busy} onChange={() => toggle(item)} />
                {item.text}
              </label>
              <button
                type="button"
                className="checklist-remove"
                aria-label={`「${item.text}」を削除`}
                disabled={busy}
                onClick={() => remove(item)}
              >
                ×
              </button>
            </li>
          ))}
        </ul>
      )}
      <div className="checklist-add">
        <input
          aria-label="チェックリスト項目"
          placeholder="項目を追加"
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault()
              if (!e.nativeEvent.isComposing) add()
            }
          }}
        />
        <button type="button" disabled={!text.trim() || busy} onClick={add}>
          追加
        </button>
      </div>
      {error && <p className="message error">{error}</p>}
    </fieldset>
  )
}
