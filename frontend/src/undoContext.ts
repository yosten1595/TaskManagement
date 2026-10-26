import { createContext, useContext } from 'react'

export interface UndoApi {
  // 取り消せる操作を登録する。undo は、操作を元に戻す処理（失敗したら例外を投げる）
  push: (message: string, undo: () => Promise<void>) => void
}

export const UndoContext = createContext<UndoApi>({ push: () => {} })

export const useUndo = () => useContext(UndoContext)
