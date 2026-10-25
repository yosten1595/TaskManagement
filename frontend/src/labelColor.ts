// ラベルの背景色（#rrggbb）に対して、コントラスト比が高い方の文字色（白または黒）を返す。
// 明るい背景に白文字を載せて読みにくくならないようにする（WCAG AA）
function channel(value: number): number {
  const v = value / 255
  return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4)
}

export function labelTextColor(background: string): string {
  const hex = background.replace('#', '')
  const r = parseInt(hex.slice(0, 2), 16)
  const g = parseInt(hex.slice(2, 4), 16)
  const b = parseInt(hex.slice(4, 6), 16)
  if ([r, g, b].some(Number.isNaN)) return '#fff'
  const luminance = 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b)
  const white = 1.05 / (luminance + 0.05)
  const black = (luminance + 0.05) / 0.05
  return white >= black ? '#fff' : '#000'
}
