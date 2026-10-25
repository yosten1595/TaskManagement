import { useEffect, useState } from 'react'

type Theme = 'auto' | 'light' | 'dark'

const STORAGE_KEY = 'taskboard-theme'
const LABEL: Record<Theme, string> = { auto: '自動', light: 'ライト', dark: 'ダーク' }
const NEXT: Record<Theme, Theme> = { auto: 'light', light: 'dark', dark: 'auto' }

function load(): Theme {
  try {
    const value = localStorage.getItem(STORAGE_KEY)
    return value === 'light' || value === 'dark' ? value : 'auto'
  } catch {
    // 保存が使えない環境（プライベートウィンドウなど）では、自動のまま
    return 'auto'
  }
}

// 表示テーマの切り替え（自動 → ライト → ダーク）。自動は OS の設定に従う。選択はこのブラウザに保存する
export default function ThemeToggle() {
  const [theme, setTheme] = useState<Theme>(load)

  useEffect(() => {
    const root = document.documentElement
    if (theme === 'auto') root.removeAttribute('data-theme')
    else root.setAttribute('data-theme', theme)
    try {
      if (theme === 'auto') localStorage.removeItem(STORAGE_KEY)
      else localStorage.setItem(STORAGE_KEY, theme)
    } catch {
      // 保存できなくても、この画面の間は切り替えられる
    }
  }, [theme])

  return (
    <button
      type="button"
      className="theme-toggle"
      aria-label={`表示テーマを切り替える（現在: ${LABEL[theme]}）`}
      onClick={() => setTheme(NEXT[theme])}
    >
      テーマ: {LABEL[theme]}
    </button>
  )
}
