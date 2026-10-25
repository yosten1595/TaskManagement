interface Props {
  done: number
  total: number
}

// チェックリストの進捗バー。項目がないときは表示しない。全て完了で緑にする
export default function ProgressBar({ done, total }: Props) {
  if (total === 0) return null
  return (
    <div
      className={`progress${done === total ? ' progress-complete' : ''}`}
      role="progressbar"
      aria-label="チェックリストの進捗"
      aria-valuemin={0}
      aria-valuemax={total}
      aria-valuenow={done}
    >
      <div className="progress-fill" style={{ width: `${(done / total) * 100}%` }} />
    </div>
  )
}
