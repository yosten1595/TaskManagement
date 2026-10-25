import { useState } from 'react'
import { updateTemplate } from '../api'
import { parseWarnDays } from '../dueStatus'
import type { CheckItem, Label, Priority, Template } from '../types'
import CardFields from './CardFields'
import ChecklistEditor from './ChecklistEditor'

interface Props {
  template: Template
  labels: Label[]
  onSaved: () => void
  onCancel: () => void
}

// テンプレートの編集フォーム。カードと違って期限・完了状態は持たない
export default function TemplateEditForm({ template, labels, onSaved, onCancel }: Props) {
  const [title, setTitle] = useState(template.title)
  const [description, setDescription] = useState(template.description ?? '')
  const [priority, setPriority] = useState<Priority | ''>(template.priority ?? '')
  const [warnDays, setWarnDays] = useState(String(template.warnDays))
  const [labelIds, setLabelIds] = useState<string[]>(template.labels.map((l) => l.id))
  // ChecklistEditor は CheckItem を扱うので、done は常に false で持つ（保存するのはテキストだけ）
  const [checklist, setChecklist] = useState<CheckItem[]>(
    template.checklist.map((item) => ({ ...item, done: false })),
  )
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = title.trim()
    if (!trimmed || submitting) return
    setSubmitting(true)
    setError(null)
    try {
      await updateTemplate(template.id, {
        title: trimmed,
        description: description.trim() || null,
        priority: priority || null,
        warnDays: parseWarnDays(warnDays),
        labelIds,
        checklist: checklist.map((item) => item.text),
      })
      onSaved()
    } catch (err) {
      setError(`保存できませんでした: ${(err as Error).message}`)
      setSubmitting(false)
    }
  }

  return (
    <form className="add-card" onSubmit={submit}>
      <input
        autoFocus
        aria-label="テンプレートのタイトル"
        placeholder="タイトルを入力"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        onKeyDown={(e) => e.key === 'Escape' && onCancel()}
      />
      <textarea
        aria-label="説明"
        placeholder="説明"
        rows={3}
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        onKeyDown={(e) => e.key === 'Escape' && onCancel()}
      />
      <CardFields
        labels={labels}
        priority={priority}
        labelIds={labelIds}
        warnDays={warnDays}
        onPriorityChange={setPriority}
        onLabelIdsChange={setLabelIds}
        onWarnDaysChange={setWarnDays}
      />
      <ChecklistEditor items={checklist} onChange={setChecklist} />
      {error && <p className="message error">{error}</p>}
      <div className="add-card-actions">
        <button type="submit" disabled={!title.trim() || submitting}>
          保存
        </button>
        <button type="button" disabled={submitting} onClick={onCancel}>
          キャンセル
        </button>
      </div>
    </form>
  )
}
