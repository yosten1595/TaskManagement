import { useState } from 'react'
import { NEW_ITEM_PREFIX } from '../checklistSync'
import type { CheckItem } from '../types'
import ProgressBar from './ProgressBar'

interface Props {
  items: CheckItem[]
  // 変更はこのフォームの中だけで持ち、API への反映は呼び出し側が「保存」時に行う
  onChange: (items: CheckItem[]) => void
}

// カード編集フォームの中に置くため、<form> は使わず Enter キーで追加する
export default function ChecklistEditor({ items, onChange }: Props) {
  const [text, setText] = useState('')

  const add = () => {
    const trimmed = text.trim()
    if (!trimmed) return
    onChange([
      ...items,
      { id: `${NEW_ITEM_PREFIX}${crypto.randomUUID()}`, text: trimmed, done: false, position: items.length },
    ])
    setText('')
  }

  const toggle = (item: CheckItem) =>
    onChange(items.map((i) => (i.id === item.id ? { ...i, done: !i.done } : i)))

  const remove = (item: CheckItem) => onChange(items.filter((i) => i.id !== item.id))

  const doneCount = items.filter((i) => i.done).length

  return (
    <fieldset className="checklist">
      <legend>
        チェックリスト{items.length > 0 && `（${doneCount}/${items.length} 完了）`}
      </legend>
      <ProgressBar done={doneCount} total={items.length} />
      {items.length > 0 && (
        <ul>
          {items.map((item) => (
            <li key={item.id}>
              <label className={item.done ? 'checklist-done' : undefined}>
                <input type="checkbox" checked={item.done} onChange={() => toggle(item)} />
                {item.text}
              </label>
              <button
                type="button"
                className="checklist-remove"
                aria-label={`「${item.text}」を削除`}
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
        <button type="button" disabled={!text.trim()} onClick={add}>
          追加
        </button>
      </div>
    </fieldset>
  )
}
