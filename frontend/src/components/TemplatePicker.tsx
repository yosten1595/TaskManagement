import { useEffect, useState } from 'react'
import { createCardFromTemplate, fetchTemplates } from '../api'
import type { Template } from '../types'

interface Props {
  listId: string
  onCreated: () => void
}

// 「テンプレートから追加」。カード追加フォームの中に置く。選んだテンプレートの内容でカードを作る
export default function TemplatePicker({ listId, onCreated }: Props) {
  const [templates, setTemplates] = useState<Template[] | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    fetchTemplates(controller.signal)
      .then(setTemplates)
      .catch((err: Error) => {
        if (!controller.signal.aborted) setError(`テンプレートを取得できませんでした: ${err.message}`)
      })
    return () => controller.abort()
  }, [])

  const choose = async (templateId: string) => {
    if (!templateId || busy) return
    setBusy(true)
    setError(null)
    try {
      await createCardFromTemplate(templateId, listId)
      onCreated()
    } catch (err) {
      setError(`作成できませんでした: ${(err as Error).message}`)
      setBusy(false)
    }
  }

  return (
    <div className="template-picker">
      <select
        aria-label="テンプレートから追加"
        value=""
        disabled={busy || templates === null || templates.length === 0}
        onChange={(e) => choose(e.target.value)}
      >
        <option value="">
          {templates === null
            ? 'テンプレートを読み込み中…'
            : templates.length === 0
              ? 'テンプレートがありません'
              : 'テンプレートから追加…'}
        </option>
        {templates?.map((template) => (
          <option key={template.id} value={template.id}>
            {template.title}
          </option>
        ))}
      </select>
      {error && <p className="message error">{error}</p>}
    </div>
  )
}
