import { useState } from 'react'
import { createCardFromTemplate, fetchTemplates } from '../api'
import type { Template } from '../types'

interface Props {
  listId: string
  onCreated: () => void
}

// 「テンプレートから追加」。開くたびに一覧を取得し、選んだテンプレートの内容でカードを作る
export default function TemplatePicker({ listId, onCreated }: Props) {
  const [open, setOpen] = useState(false)
  const [templates, setTemplates] = useState<Template[] | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const close = () => {
    setOpen(false)
    setError(null)
  }

  const show = async () => {
    setOpen(true)
    setTemplates(null)
    setError(null)
    try {
      setTemplates(await fetchTemplates())
    } catch (err) {
      setError(`取得できませんでした: ${(err as Error).message}`)
    }
  }

  const choose = async (template: Template) => {
    if (busy) return
    setBusy(true)
    setError(null)
    try {
      await createCardFromTemplate(template.id, listId)
      close()
      onCreated()
    } catch (err) {
      setError(`作成できませんでした: ${(err as Error).message}`)
    } finally {
      setBusy(false)
    }
  }

  if (!open) {
    return (
      <button type="button" className="add-card-open" onClick={show}>
        ＋ テンプレートから追加
      </button>
    )
  }

  return (
    <div className="template-picker">
      {templates === null && !error && <p className="hint">読み込み中…</p>}
      {templates?.length === 0 && (
        <p className="hint">テンプレートがありません。カードの編集画面から「テンプレートとして保存」できます。</p>
      )}
      <ul>
        {templates?.map((template) => (
          <li key={template.id}>
            <button type="button" disabled={busy} onClick={() => choose(template)}>
              {template.title}
            </button>
          </li>
        ))}
      </ul>
      {error && <p className="message error">{error}</p>}
      <button type="button" className="template-picker-close" disabled={busy} onClick={close}>
        閉じる
      </button>
    </div>
  )
}
