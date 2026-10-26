import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { dueStatus, parseWarnDays } from './dueStatus'

describe('dueStatus', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    // ローカル時間の 2026-10-26 15:30（時刻が日付の判定に影響しないことも確かめる）
    vi.setSystemTime(new Date(2026, 9, 26, 15, 30))
  })
  afterEach(() => vi.useRealTimers())

  it('期限が今日より前なら期限切れ', () => {
    expect(dueStatus('2026-10-25', 3)).toBe('overdue')
  })

  it('期限が今日なら期限間近（警告日数が 0 でも）', () => {
    expect(dueStatus('2026-10-26', 0)).toBe('soon')
  })

  it('警告日数ちょうど先までは期限間近、1 日先は余裕あり', () => {
    expect(dueStatus('2026-10-29', 3)).toBe('soon')
    expect(dueStatus('2026-10-30', 3)).toBe('ok')
  })

  it('月をまたぐ日付も正しく数える', () => {
    expect(dueStatus('2026-11-02', 7)).toBe('soon')
    expect(dueStatus('2026-11-03', 7)).toBe('ok')
  })
})

describe('parseWarnDays', () => {
  it('0〜365 の整数はそのまま使う', () => {
    expect(parseWarnDays('0')).toBe(0)
    expect(parseWarnDays('5')).toBe(5)
    expect(parseWarnDays('365')).toBe(365)
  })

  it('空・範囲外・整数でない値は既定の 3 日にする', () => {
    for (const value of ['', '  ', '-1', '366', '1.5', 'abc']) {
      expect(parseWarnDays(value)).toBe(3)
    }
  })
})
