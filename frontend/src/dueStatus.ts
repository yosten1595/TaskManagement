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
