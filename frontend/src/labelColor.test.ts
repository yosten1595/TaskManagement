import { describe, expect, it } from 'vitest'
import { labelTextColor } from './labelColor'

describe('labelTextColor', () => {
  it('暗い背景には白、明るい背景には黒を返す', () => {
    expect(labelTextColor('#000000')).toBe('#fff')
    expect(labelTextColor('#4a6a8a')).toBe('#fff')
    expect(labelTextColor('#ffffff')).toBe('#000')
    expect(labelTextColor('#ffff00')).toBe('#000')
  })

  it('色として読めない値は白にする', () => {
    expect(labelTextColor('not-a-color')).toBe('#fff')
  })
})
