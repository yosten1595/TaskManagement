import { beforeEach, describe, expect, it, vi } from 'vitest'
import { addCheckItem, deleteCheckItem, updateCheckItem } from './api'
import { newItemId, NEW_ITEM_PREFIX, syncChecklist } from './checklistSync'
import type { Card, CheckItem } from './types'

vi.mock('./api', () => ({
  addCheckItem: vi.fn(),
  deleteCheckItem: vi.fn(),
  updateCheckItem: vi.fn(),
}))

const item = (id: string, text: string, done = false, position = 0): CheckItem => ({ id, text, done, position })

describe('syncChecklist', () => {
  beforeEach(() => vi.clearAllMocks())

  it('変更がなければ何も呼ばない', async () => {
    const saved = [item('a', 'one'), item('b', 'two', true, 1)]
    await syncChecklist('card', saved, saved.map((i) => ({ ...i })))
    expect(addCheckItem).not.toHaveBeenCalled()
    expect(updateCheckItem).not.toHaveBeenCalled()
    expect(deleteCheckItem).not.toHaveBeenCalled()
  })

  it('消した項目は削除、書き換えた項目は更新、新しい項目は追加する', async () => {
    const saved = [item('a', 'one'), item('b', 'two'), item('c', 'three')]
    const desired = [item('a', 'one'), item('b', 'two', true), item(`${NEW_ITEM_PREFIX}x`, 'four', false, 2)]
    await syncChecklist('card', saved, desired)
    expect(deleteCheckItem).toHaveBeenCalledWith('card', 'c')
    expect(updateCheckItem).toHaveBeenCalledWith('card', 'b', 'two', true)
    expect(addCheckItem).toHaveBeenCalledWith('card', 'four')
    expect(updateCheckItem).toHaveBeenCalledTimes(1)
  })

  it('削除 → 更新 → 追加の順に呼び、追加はフォームの並び順どおりにする', async () => {
    const calls: string[] = []
    vi.mocked(deleteCheckItem).mockImplementation(async () => void calls.push('delete'))
    vi.mocked(updateCheckItem).mockImplementation(async () => {
      calls.push('update')
      return {} as Card
    })
    vi.mocked(addCheckItem).mockImplementation(async (_card, text) => {
      calls.push(`add:${text}`)
      return {} as Card
    })
    const saved = [item('a', 'one'), item('b', 'two')]
    const desired = [item('a', 'ONE'), item(`${NEW_ITEM_PREFIX}1`, 'first'), item(`${NEW_ITEM_PREFIX}2`, 'second')]
    await syncChecklist('card', saved, desired)
    expect(calls).toEqual(['delete', 'update', 'add:first', 'add:second'])
  })
})

describe('newItemId', () => {
  it('保存前の項目と分かる接頭辞が付き、毎回違う値になる', () => {
    const ids = new Set(Array.from({ length: 50 }, () => newItemId()))
    expect(ids.size).toBe(50)
    for (const id of ids) expect(id.startsWith(NEW_ITEM_PREFIX)).toBe(true)
  })
})
