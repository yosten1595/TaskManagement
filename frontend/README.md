# TaskBoard フロントエンド

React 19 + TypeScript + Vite のシングルページアプリです。全体の説明は[ルートの README](../README.md) を参照してください。

## コマンド

| コマンド | 内容 |
| --- | --- |
| `npm run dev` | 開発サーバー（http://localhost:5173 。`/api` は `localhost:8080` のバックエンドへ転送） |
| `npm run lint` | Oxlint。警告もエラー扱い |
| `npm run typecheck` | `tsc -b`（型チェック） |
| `npm test` | Vitest（`src/*.test.ts`） |
| `npm run build` | 型チェック + 本番ビルド |
| `npm run preview` | 本番ビルドの確認（5173 で起動。先に開発サーバーを止める） |

ポートは固定です（`strictPort`）。使用中のときの対処はルートの README を参照してください。

## 構成（`src/`）

| 場所 | 内容 |
| --- | --- |
| `pages/` | 画面（`HomePage`: ボード一覧、`BoardPage`: カンバンボード） |
| `components/` | 画面の部品（カード、モーダル、フォーム、テンプレート、バックアップなど） |
| `api.ts` / `types.ts` | バックエンド API の呼び出しと、DTO に対応する型 |
| `undo.tsx` / `undoContext.ts` | 「元に戻す」の通知（Provider）と、それを使うフック |
| `checklistSync.ts` | チェックリストの編集内容を、保存時に API へ反映する差分処理 |
| `dueStatus.ts` / `labelColor.ts` | 期限の状態の判定、ラベルの文字色の選択 |

## Lint の設定

`.oxlintrc.json`。`correctness` はエラー、`suspicious` は警告にし、React Hooks と jsx-a11y（アクセシビリティ）のルールを有効にしています。`autoFocus`（モーダルやフォームを開いた直後の入力のため）と、`role` 属性の代わりに専用タグを使う指摘は、意図があるので無効にしています。個別の例外は、その行の `oxlint-disable-next-line` コメントに理由を書いています。
