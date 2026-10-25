import { useState } from 'react'
import { fetchCard, updateCard } from '../api'
import { syncChecklist } from '../checklistSync'
import type { Card, Label, Priority } from '../types'
import { parseWarnDays } from '../dueStatus'
import CardFields from './CardFields'
import ChecklistEditor from './ChecklistEditor'

interface Props {
  card: Card
  labels: Label[]
  onSaved: () => void
  onCancel: () => void
}

export default function EditCardForm({ card, labels, onSaved, onCancel }: Props) {
  const [title, setTitle] = useState(card.title)
  const [description, setDescription] = useState(card.description ?? '')
  const [dueDate, setDueDate] = useState(card.dueDate ?? '')
  const [priority, setPriority] = useState<Priority | ''>(card.priority ?? '')
  const [labelIds, setLabelIds] = useState<string[]>(card.labels.map((l) => l.id))
  const [warnDays, setWarnDays] = useState(String(card.warnDays))
  // チェックリストは「保存」を押したときだけ API に反映する。savedChecklist は API 上の現在の状態
  const [savedChecklist, setSavedChecklist] = useState(card.checklist)
  const [checklist, setChecklist] = useState(card.checklist)
  // 保存の途中で失敗してカードが一部だけ更新された場合、キャンセルでもボードを再読み込みする
  const [partiallySaved, setPartiallySaved] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const cancel = partiallySaved ? onSaved : onCancel

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = title.trim()
    if (!trimmed || submitting) return
    setSubmitting(true)
    setError(null)
    try {
      await updateCard(card.id, {
        title: trimmed,
        description: description.trim() || null,
        dueDate: dueDate || null,
        priority: priority || null,
        labelIds,
        warnDays: parseWarnDays(warnDays),
      })
    } catch (err) {
      setError(`保存できませんでした: ${(err as Error).message}`)
      setSubmitting(false)
      return
    }
    try {
      await syncChecklist(card.id, savedChecklist, checklist)
      onSaved()
    } catch (err) {
      // 途中まで反映された可能性があるため、API 上の最新の状態に表示を合わせる
      setPartiallySaved(true)
      setError(`カードは保存しましたが、チェックリストを保存できませんでした: ${(err as Error).message}`)
      try {
        const latest = await fetchCard(card.id)
        setSavedChecklist(latest.checklist)
        setChecklist(latest.checklist)
      } catch {
        // 最新の取得にも失敗した場合は表示をそのままにする
      }
      setSubmitting(false)
    }
  }

  return (
    <form className="add-card card" onSubmit={submit}>
      <input
        autoFocus
        aria-label="カードのタイトル"
        placeholder="タイトルを入力"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        onKeyDown={(e) => e.key === 'Escape' && cancel()}
      />
      <textarea
        aria-label="説明"
        placeholder="説明"
        rows={3}
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        onKeyDown={(e) => e.key === 'Escape' && cancel()}
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
      <ChecklistEditor items={checklist} onChange={setChecklist} />
      {error && <p className="message error">{error}</p>}
      <div className="add-card-actions">
        <button type="submit" disabled={!title.trim() || submitting}>
          保存
        </button>
        <button type="button" disabled={submitting} onClick={cancel}>
          キャンセル
        </button>
      </div>
    </form>
  )
}
