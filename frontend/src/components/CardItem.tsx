import type { Card, Priority } from '../types'

const PRIORITY_LABEL: Record<Priority, string> = { HIGH: '高', MID: '中', LOW: '低' }

export default function CardItem({ card }: { card: Card }) {
  const doneCount = card.checklist.filter((item) => item.done).length

  return (
    <article className="card">
      {card.labels.length > 0 && (
        <div className="card-labels">
          {card.labels.map((label) => (
            <span key={label.id} className="label" style={{ background: label.color }}>
              {label.name}
            </span>
          ))}
        </div>
      )}
      <h3>{card.title}</h3>
      <div className="card-meta">
        {card.priority && (
          <span className={`priority priority-${card.priority.toLowerCase()}`}>
            優先度 {PRIORITY_LABEL[card.priority]}
          </span>
        )}
        {card.dueDate && <span>期限 {card.dueDate}</span>}
        {card.checklist.length > 0 && (
          <span>
            ✓ {doneCount}/{card.checklist.length}
          </span>
        )}
      </div>
    </article>
  )
}
