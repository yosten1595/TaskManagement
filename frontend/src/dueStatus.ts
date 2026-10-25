export type DueStatus = 'overdue' | 'soon' | 'ok' | 'done'

// dueDate は YYYY-MM-DD。ブラウザのローカル日付の 0 時同士で日数差を取る
export function dueStatus(dueDate: string, warnDays: number): Exclude<DueStatus, 'done'> {
  const [y, m, d] = dueDate.split('-').map(Number)
  const today = new Date()
  const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  const days = Math.round((new Date(y, m - 1, d).getTime() - todayStart.getTime()) / 86_400_000)
  if (days < 0) return 'overdue'
  return days <= warnDays ? 'soon' : 'ok'
}

// 期限での絞り込み。'none' は期限なし。完了リストのカードは「完了」と表示されるので、期限の条件には合わない
export type DueFilter = '' | 'overdue' | 'soon' | 'ok' | 'none'

export function matchesDueFilter(
  filter: DueFilter,
  card: { dueDate: string | null; warnDays: number },
  completed: boolean,
): boolean {
  if (filter === '') return true
  if (completed) return false
  if (filter === 'none') return card.dueDate === null
  return card.dueDate !== null && dueStatus(card.dueDate, card.warnDays) === filter
}
