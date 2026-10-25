import { useState } from 'react'
import { createPortal } from 'react-dom'
import { deleteTemplate, fetchTemplates } from '../api'
import type { Label, Template } from '../types'
import TemplateEditForm from './TemplateEditForm'

interface Props {
  labels: Label[]
}

// 「テンプレート管理」。モーダルに一覧を出し、編集・削除する（作成はカードの「テンプレートとして保存」から）
export default function TemplateManager({ labels }: Props) {
  const [open, setOpen] = useState(false)
  const [templates, setTemplates] = useState<Template[] | null>(null)
  const [editing, setEditing] = useState<Template | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = async () => {
    try {
      setTemplates(await fetchTemplates())
    } catch (err) {
      setError(`取得できませんでした: ${(err as Error).message}`)
    }
  }

  const show = () => {
    setOpen(true)
    setTemplates(null)
    setEditing(null)
    setError(null)
    load()
  }

  const close = () => {
    setOpen(false)
    setEditing(null)
  }

  const remove = async (template: Template) => {
    if (busy || !window.confirm(`テンプレート「${template.title}」を削除しますか？\nこのテンプレートから作成済みのカードは残ります。`)) return
    setBusy(true)
    setError(null)
    try {
      await deleteTemplate(template.id)
      await load()
    } catch (err) {
      setError(`削除できませんでした: ${(err as Error).message}`)
    } finally {
      setBusy(false)
    }
  }

  const button = (
    <button type="button" className="template-manager-open" onClick={show}>
      テンプレート管理
    </button>
  )
  if (!open) return button

  const modal = createPortal(
    <div className="modal-backdrop">
      <div className="modal" role="dialog" aria-modal="true" aria-label="テンプレート管理">
        {editing ? (
          <TemplateEditForm
            template={editing}
            labels={labels}
            onSaved={() => {
              setEditing(null)
              setTemplates(null)
              load()
            }}
            onCancel={() => setEditing(null)}
          />
        ) : (
          <>
            <div className="template-manager-head">
              <strong>テンプレート管理</strong>
              <button type="button" onClick={close}>
                閉じる
              </button>
            </div>
            {templates === null && !error && <p className="hint">読み込み中…</p>}
            {templates?.length === 0 && (
              <p className="hint">テンプレートがありません。カードの追加・編集画面から「テンプレートとして保存」できます。</p>
            )}
            <ul className="template-manager-list">
              {templates?.map((template) => (
                <li key={template.id}>
                  <span className="template-manager-title">{template.title}</span>
                  <button type="button" disabled={busy} aria-label={`「${template.title}」を編集`} onClick={() => setEditing(template)}>
                    編集
                  </button>
                  <button type="button" className="danger" disabled={busy} aria-label={`「${template.title}」を削除`} onClick={() => remove(template)}>
                    削除
                  </button>
                </li>
              ))}
            </ul>
            {error && <p className="message error">{error}</p>}
          </>
        )}
      </div>
    </div>,
    document.body,
  )

  return (
    <>
      {button}
      {modal}
    </>
  )
}
