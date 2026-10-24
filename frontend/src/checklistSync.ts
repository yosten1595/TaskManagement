import { addCheckItem, deleteCheckItem, updateCheckItem } from './api'
import type { CheckItem } from './types'

// まだ保存されていない項目の ID の接頭辞（保存時に API で追加する）
export const NEW_ITEM_PREFIX = 'new-'

// フォームで編集したチェックリスト（desired）を、保存済みの状態（saved）との差分で API に反映する。
// 順序は 削除 → 更新 → 追加（追加は末尾に積まれるため、フォーム上の並び順どおりに保存される）
export async function syncChecklist(cardId: string, saved: CheckItem[], desired: CheckItem[]): Promise<void> {
  for (const item of saved.filter((s) => !desired.some((d) => d.id === s.id))) {
    await deleteCheckItem(cardId, item.id)
  }
  for (const item of desired) {
    const before = saved.find((s) => s.id === item.id)
    if (before && (before.text !== item.text || before.done !== item.done)) {
      await updateCheckItem(cardId, item.id, item.text, item.done)
    }
  }
  for (const item of desired.filter((d) => d.id.startsWith(NEW_ITEM_PREFIX))) {
    await addCheckItem(cardId, item.text)
  }
}
