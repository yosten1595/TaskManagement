import { useState } from 'react'
import { createLabel, deleteLabel, fetchLabels, updateLabel } from '../api'
import type { Label } from '../types'

interface Props {
  labels: Label[]
  onChanged: (labels: Label[]) => void
}

const DEFAULT_COLOR = '#2563eb'

// ラベルの一覧・追加・名前と色の変更・削除。ラベルは全ボード共通
export default function LabelManager({ labels, onChanged }: Props) {
  const [open, setOpen] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [name, setName] = useState('')
  const [color, setColor] = useState(DEFAULT_COLOR)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const reset = () => {
    setEditingId(null)
    setName('')
    setColor(DEFAULT_COLOR)
  }

  const startEdit = (label: Label) => {
    setEditingId(label.id)
    setName(label.name)
    setColor(label.color)
    setError(null)
  }

  const run = async (action: () => Promise<unknown>, failure: string) => {
    setBusy(true)
    setError(null)
    try {
      await action()
      onChanged(await fetchLabels())
      reset()
    } catch (err) {
      setError(`${failure}: ${(err as Error).message}`)
    } finally {
      setBusy(false)
    }
  }

  const submit = (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = name.trim()
    if (!trimmed || busy) return
    if (editingId) {
      run(() => updateLabel(editingId, trimmed, color), '保存できませんでした')
    } else {
      run(() => createLabel(trimmed, color), '作成できませんでした')
    }
  }

  const remove = (label: Label) => {
    if (busy || !window.confirm(`ラベル「${label.name}」を削除しますか？\nカードからも外れます（カードは残ります）。`)) return
    run(() => deleteLabel(label.id), '削除できませんでした')
  }

  if (!open) {
    return (
      <button type="button" className="label-manager-open" onClick={() => setOpen(true)}>
        ラベル管理
      </button>
    )
  }

  return (
    <div className="label-manager">
      <div className="label-manager-head">
        <strong>ラベル管理</strong>
        <button type="button" onClick={() => { setOpen(false); reset(); setError(null) }}>
          閉じる
        </button>
      </div>
      {labels.length === 0 && <p className="hint">ラベルはまだありません。</p>}
      <ul className="label-manager-list">
        {labels.map((label) => (
          <li key={label.id}>
            <span className="label" style={{ background: label.color }}>
              {label.name}
            </span>
            <button type="button" disabled={busy} aria-label={`ラベル「${label.name}」を編集`} onClick={() => startEdit(label)}>
              編集
            </button>
            <button type="button" className="danger" disabled={busy} aria-label={`ラベル「${label.name}」を削除`} onClick={() => remove(label)}>
              削除
            </button>
          </li>
        ))}
      </ul>
      <form className="label-manager-form" onSubmit={submit}>
        <input
          aria-label="ラベル名"
          placeholder={editingId ? 'ラベル名' : '新しいラベル名'}
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <input type="color" aria-label="ラベルの色" value={color} onChange={(e) => setColor(e.target.value)} />
        <button type="submit" className="primary" disabled={!name.trim() || busy}>
          {editingId ? '保存' : '追加'}
        </button>
        {editingId && (
          <button type="button" disabled={busy} onClick={reset}>
            キャンセル
          </button>
        )}
      </form>
      {error && <p className="message error">{error}</p>}
    </div>
  )
}
