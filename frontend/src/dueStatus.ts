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

// 警告日数（期限の何日前から「期限間近」にするか）。バックエンドの既定値・上限と合わせる
export const DEFAULT_WARN_DAYS = 3
export const MAX_WARN_DAYS = 365

// 入力欄の文字列を API に送る日数にする。空・範囲外は既定値
export function parseWarnDays(value: string): number {
  const n = Number(value)
  return value.trim() !== '' && Number.isInteger(n) && n >= 0 && n <= MAX_WARN_DAYS ? n : DEFAULT_WARN_DAYS
}
