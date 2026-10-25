import { useEffect, useRef, type KeyboardEvent, type ReactNode } from 'react'
import { createPortal } from 'react-dom'

interface Props {
  label: string
  // Esc キーで閉じるときに呼ぶ
  onClose: () => void
  children: ReactNode
}

const FOCUSABLE =
  'a[href], button:not(:disabled), input:not(:disabled), select:not(:disabled), textarea:not(:disabled), [tabindex]:not([tabindex="-1"])'

// 画面に重ねるモーダル。キーボード操作のため、開いたらモーダル内にフォーカスを移し、
// Tab がモーダルの外へ抜けないようにし、Esc で閉じ、閉じたら開く前の位置にフォーカスを戻す
export default function Modal({ label, onClose, children }: Props) {
  const dialogRef = useRef<HTMLDivElement>(null)
  // 開く前にフォーカスがあった要素。入力欄の autoFocus より前（描画時）に控える
  const previousRef = useRef<HTMLElement | null>(document.activeElement as HTMLElement | null)

  useEffect(() => {
    const previous = previousRef.current
    const dialog = dialogRef.current
    // 入力欄が autoFocus で既にフォーカスを持っているときは、そのままにする
    if (dialog && !dialog.contains(document.activeElement)) {
      const first = dialog.querySelector<HTMLElement>(FOCUSABLE)
      ;(first ?? dialog).focus()
    }
    return () => previous?.focus?.()
  }, [])

  const handleKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
    if (e.key === 'Escape') {
      e.stopPropagation()
      onClose()
      return
    }
    if (e.key !== 'Tab' || !dialogRef.current) return
    const items = [...dialogRef.current.querySelectorAll<HTMLElement>(FOCUSABLE)].filter((el) => el.offsetParent !== null)
    if (items.length === 0) {
      e.preventDefault()
      return
    }
    const first = items[0]
    const last = items[items.length - 1]
    const current = document.activeElement
    if (e.shiftKey && (current === first || current === dialogRef.current)) {
      e.preventDefault()
      last.focus()
    } else if (!e.shiftKey && current === last) {
      e.preventDefault()
      first.focus()
    }
  }

  return createPortal(
    <div className="modal-backdrop">
      <div
        ref={dialogRef}
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-label={label}
        tabIndex={-1}
        onKeyDown={handleKeyDown}
      >
        {children}
      </div>
    </div>,
    document.body,
  )
}
