import { useState } from 'react'
import { createCard, createTemplate } from '../api'
import type { Label, Priority } from '../types'
import { parseWarnDays } from '../dueStatus'
import CardFields from './CardFields'
import TemplatePicker from './TemplatePicker'

interface Props {
  listId: string
  labels: Label[]
  onCreated: () => void
}

export default function AddCardForm({ listId, labels, onCreated }: Props) {
  const [open, setOpen] = useState(false)
  const [title, setTitle] = useState('')
  const [dueDate, setDueDate] = useState('')
  const [priority, setPriority] = useState<Priority | ''>('')
  const [labelIds, setLabelIds] = useState<string[]>([])
  const [warnDays, setWarnDays] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [templateMessage, setTemplateMessage] = useState<string | null>(null)

  const reset = () => {
    setTitle('')
    setDueDate('')
    setPriority('')
    setLabelIds([])
    setWarnDays('')
    setError(null)
    setTemplateMessage(null)
  }

  const close = () => {
    setOpen(false)
    reset()
  }

  // カードは作らず、入力中の内容（タイトル・優先度・警告日数・ラベル）だけをテンプレートとして保存する
  const saveAsTemplate = async () => {
    const trimmed = title.trim()
    if (!trimmed || submitting) return
    setSubmitting(true)
    setError(null)
    setTemplateMessage(null)
    try {
      await createTemplate({
        title: trimmed,
        description: null,
        priority: priority || null,
        warnDays: parseWarnDays(warnDays),
        labelIds,
        checklist: [],
      })
      setTemplateMessage('テンプレートとして保存しました')
    } catch (err) {
      setError(`テンプレートを保存できませんでした: ${(err as Error).message}`)
    } finally {
      setSubmitting(false)
    }
  }

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = title.trim()
    if (!trimmed || submitting) return
    setSubmitting(true)
    setError(null)
    try {
      await createCard(listId, {
        title: trimmed,
        dueDate: dueDate || undefined,
        priority: priority || undefined,
        labelIds: labelIds.length > 0 ? labelIds : undefined,
        warnDays: parseWarnDays(warnDays),
      })
      reset()
      onCreated()
    } catch (err) {
      setError(`作成できませんでした: ${(err as Error).message}`)
    } finally {
      setSubmitting(false)
    }
  }

  if (!open) {
    return (
      <button type="button" className="add-card-open" onClick={() => setOpen(true)}>
        ＋ カードを追加
      </button>
    )
  }

  return (
    <form className="add-card" onSubmit={submit}>
      <TemplatePicker
        listId={listId}
        onCreated={() => {
          close()
          onCreated()
        }}
      />
      <input
        autoFocus
        aria-label="カードのタイトル"
        placeholder="タイトルを入力"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        onKeyDown={(e) => e.key === 'Escape' && close()}
      />
      <CardFields
        labels={labels}
        dueDate={dueDate}
        priority={priority}
        labelIds={labelIds}
        warnDays={warnDays}
        onDueDateChange={setDueDate}
        onPriorityChange={setPriority}
        onLabelIdsChange={setLabelIds}
        onWarnDaysChange={setWarnDays}
      />
      {error && <p className="message error">{error}</p>}
      {templateMessage && <p className="message">{templateMessage}</p>}
      <div className="add-card-actions">
        <button type="submit" disabled={!title.trim() || submitting}>
          追加
        </button>
        <button type="button" onClick={close}>
          キャンセル
        </button>
        <button type="button" disabled={!title.trim() || submitting} onClick={saveAsTemplate}>
          テンプレートとして保存
        </button>
      </div>
    </form>
  )
}
