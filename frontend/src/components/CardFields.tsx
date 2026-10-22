import type { Label, Priority } from '../types'

interface Props {
  labels: Label[]
  dueDate: string
  priority: Priority | ''
  labelIds: string[]
  onDueDateChange: (value: string) => void
  onPriorityChange: (value: Priority | '') => void
  onLabelIdsChange: (value: string[]) => void
}

// カードの作成・編集フォームで共通の、期限・優先度・ラベルの入力欄
export default function CardFields({
  labels,
  dueDate,
  priority,
  labelIds,
  onDueDateChange,
  onPriorityChange,
  onLabelIdsChange,
}: Props) {
  const toggleLabel = (id: string) =>
    onLabelIdsChange(labelIds.includes(id) ? labelIds.filter((x) => x !== id) : [...labelIds, id])

  return (
    <>
      <div className="add-card-fields">
        <label>
          期限
          <input type="date" value={dueDate} onChange={(e) => onDueDateChange(e.target.value)} />
        </label>
        <label>
          優先度
          <select value={priority} onChange={(e) => onPriorityChange(e.target.value as Priority | '')}>
            <option value="">未設定</option>
            <option value="HIGH">高</option>
            <option value="MID">中</option>
            <option value="LOW">低</option>
          </select>
        </label>
      </div>
      {labels.length > 0 && (
        <fieldset className="add-card-labels">
          <legend>ラベル</legend>
          {labels.map((label) => (
            <label key={label.id} className="label-choice">
              <input
                type="checkbox"
                checked={labelIds.includes(label.id)}
                onChange={() => toggleLabel(label.id)}
              />
              <span className="label" style={{ background: label.color }}>
                {label.name}
              </span>
            </label>
          ))}
        </fieldset>
      )}
    </>
  )
}
