import { useRef, useState } from 'react'
import { exportBackup, importBackup } from '../api'

interface Props {
  // インポートでボードが増えたあとに、一覧を読み込み直す
  onImported: () => void
}

// 全データの JSON ファイルへの書き出し（エクスポート）と、ファイルからの追加（インポート）
export default function BackupControls({ onImported }: Props) {
  const fileInput = useRef<HTMLInputElement>(null)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState<{ text: string; error: boolean } | null>(null)

  const download = async () => {
    setBusy(true)
    setMessage(null)
    try {
      const json = await exportBackup()
      const url = URL.createObjectURL(new Blob([json], { type: 'application/json' }))
      const link = document.createElement('a')
      const today = new Date().toLocaleDateString('sv-SE').replaceAll('-', '')
      link.href = url
      link.download = `taskboard-backup-${today}.json`
      link.click()
      URL.revokeObjectURL(url)
      setMessage({ text: 'エクスポートしました', error: false })
    } catch (err) {
      setMessage({ text: `エクスポートできませんでした: ${(err as Error).message}`, error: true })
    } finally {
      setBusy(false)
    }
  }

  const upload = async (file: File) => {
    setBusy(true)
    setMessage(null)
    try {
      const result = await importBackup(await file.text())
      setMessage({
        text: `インポートしました（ボード ${result.boards} 件、カード ${result.cards} 件、テンプレート ${result.templates} 件を追加）`,
        error: false,
      })
      onImported()
    } catch (err) {
      setMessage({ text: `インポートできませんでした: ${(err as Error).message}`, error: true })
    } finally {
      setBusy(false)
      // 同じファイルをもう一度選べるようにする
      if (fileInput.current) fileInput.current.value = ''
    }
  }

  return (
    <div className="backup">
      <button type="button" disabled={busy} onClick={download}>
        エクスポート
      </button>
      <button type="button" disabled={busy} onClick={() => fileInput.current?.click()}>
        インポート
      </button>
      <input
        ref={fileInput}
        type="file"
        accept="application/json,.json"
        hidden
        aria-label="インポートするファイル"
        onChange={(e) => {
          const file = e.target.files?.[0]
          if (file) upload(file)
        }}
      />
      {message && (
        <span className={message.error ? 'message error' : 'message'} role="status">
          {message.text}
        </span>
      )}
    </div>
  )
}
