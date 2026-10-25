import { DEFAULT_WARN_DAYS, MAX_WARN_DAYS } from '../dueStatus'
import { labelTextColor } from '../labelColor'
import type { Label, Priority } from '../types'

interface Props {
  labels: Label[]
  // 期限は任意。onDueDateChange を渡さない場合（テンプレートの編集）は入力欄を出さない
  dueDate?: string
  priority: Priority | ''
  labelIds: string[]
  // 空文字は未入力（保存時に既定の日数になる）
  warnDays: string
  onDueDateChange?: (value: string) => void
  onPriorityChange: (value: Priority | '') => void
  onLabelIdsChange: (value: string[]) => void
  onWarnDaysChange: (value: string) => void
}

// カードの作成・編集フォームで共通の、期限・優先度・ラベルの入力欄
export default function CardFields({
  labels,
  dueDate,
  priority,
  labelIds,
  warnDays,
  onDueDateChange,
  onPriorityChange,
  onLabelIdsChange,
  onWarnDaysChange,
}: Props) {
  const toggleLabel = (id: string) =>
    onLabelIdsChange(labelIds.includes(id) ? labelIds.filter((x) => x !== id) : [...labelIds, id])

  return (
    <>
      <div className="add-card-fields">
        {onDueDateChange && (
          <label>
            期限
            <input type="date" value={dueDate ?? ''} onChange={(e) => onDueDateChange(e.target.value)} />
          </label>
        )}
        <label>
          優先度
          <select value={priority} onChange={(e) => onPriorityChange(e.target.value as Priority | '')}>
            <option value="">未設定</option>
            <option value="HIGH">高</option>
            <option value="MID">中</option>
            <option value="LOW">低</option>
          </select>
        </label>
        <label>
          警告日数
          <input
            type="number"
            min={0}
            max={MAX_WARN_DAYS}
            list="warn-days-options"
            placeholder={`${DEFAULT_WARN_DAYS}`}
            title="期限の何日前から「期限間近」と表示するか"
            value={warnDays}
            onChange={(e) => onWarnDaysChange(e.target.value)}
          />
          <datalist id="warn-days-options">
            {[1, 3, 5, 7, 14].map((d) => (
              <option key={d} value={d} />
            ))}
          </datalist>
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
              <span className="label" style={{ background: label.color, color: labelTextColor(label.color) }}>
                {label.name}
              </span>
            </label>
          ))}
        </fieldset>
      )}
    </>
  )
}
