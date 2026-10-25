import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { useLocation } from 'react-router-dom'

// 直前の操作 1 件だけを取り消せる。通知は一定時間で消える
const VISIBLE_MS = 10_000

interface UndoAction {
  message: string
  undo: () => Promise<void>
}

interface UndoApi {
  // 取り消せる操作を登録する。undo は、操作を元に戻す処理（失敗したら例外を投げる）
  push: (message: string, undo: () => Promise<void>) => void
}

const UndoContext = createContext<UndoApi>({ push: () => {} })

export const useUndo = () => useContext(UndoContext)

// 入力欄にいるときの Ctrl+Z は、入力欄自身の元に戻すを優先する
function isEditing(target: EventTarget | null): boolean {
  return target instanceof HTMLElement && target.closest('input, textarea, select, [contenteditable="true"]') !== null
}

export function UndoProvider({ children }: { children: ReactNode }) {
  const [action, setAction] = useState<UndoAction | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const timer = useRef<number | undefined>(undefined)
  const location = useLocation()

  const clear = useCallback(() => {
    window.clearTimeout(timer.current)
    setAction(null)
    setError(null)
  }, [])

  const push = useCallback((message: string, undo: () => Promise<void>) => {
    window.clearTimeout(timer.current)
    setError(null)
    setAction({ message, undo })
    timer.current = window.setTimeout(() => setAction(null), VISIBLE_MS)
  }, [])

  const run = useCallback(async () => {
    if (!action || busy) return
    setBusy(true)
    setError(null)
    window.clearTimeout(timer.current)
    try {
      await action.undo()
      setAction(null)
    } catch (err) {
      setError(`元に戻せませんでした: ${(err as Error).message}`)
      timer.current = window.setTimeout(() => setAction(null), VISIBLE_MS)
    } finally {
      setBusy(false)
    }
  }, [action, busy])

  // 別のページへ移ったら、前のページの操作は取り消せなくする
  useEffect(() => clear(), [location.pathname, clear])

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key.toLowerCase() !== 'z' || e.shiftKey || !(e.ctrlKey || e.metaKey)) return
      if (isEditing(e.target) || document.querySelector('.modal')) return
      if (!action) return
      e.preventDefault()
      run()
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [action, run])

  const api = useMemo(() => ({ push }), [push])

  return (
    <UndoContext.Provider value={api}>
      {children}
      {action && (
        <div className="undo-bar" role="status">
          <span>{error ?? action.message}</span>
          <button type="button" disabled={busy} onClick={run}>
            元に戻す
          </button>
          <button type="button" className="undo-close" aria-label="通知を閉じる" onClick={clear}>
            ×
          </button>
        </div>
      )}
    </UndoContext.Provider>
  )
}
