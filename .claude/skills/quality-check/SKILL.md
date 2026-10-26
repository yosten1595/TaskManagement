---
name: quality-check
description: コードを変更したあと、またはPRを出す前に、フロントエンド（Oxlint・tsc・ビルド）とバックエンド（Checkstyle・javac警告・テスト）の品質チェックを実行するときに使う。「品質チェックして」「lintして」「PRを出す前に確認して」「CIが落ちた」などで使用する。
---

# 品質チェック

変更した側を必ず実行する。PR 上の CI（`.github/workflows/ci.yml`）も同じ内容を実行するので、手元で通してから push する。

## フロントエンド（`frontend/`）
```bash
cd frontend
npm run lint        # Oxlint。警告もエラー扱い（--deny-warnings）。0 件であること
npm run typecheck   # tsc -b
npm run build       # 型チェック + 本番ビルド
```
- 設定は `frontend/.oxlintrc.json`。`correctness` はエラー、`suspicious` は警告。react-hooks と jsx-a11y を有効にしている。
- 例外にするときは、設定を緩めるより先にコードを直す。どうしても必要なら、その行の直前に理由付きで書く:
  `// oxlint-disable-next-line <ルール名> -- 理由`
- 意図があって全体で無効にしているもの: `jsx-a11y/no-autofocus`（モーダル・フォームを開いた直後の入力のため）、`jsx-a11y/prefer-tag-over-role`、`react/react-in-jsx-scope`。
- よくある指摘と直し方:
  - `set-state-in-effect`: effect 内で同期的に setState しない。値の変化に応じた state の調整は描画中に行うか、イベントで更新する。
  - `only-export-components`: フックや定数はコンポーネントと別ファイルに置く（例: `undo.tsx` と `undoContext.ts`）。
  - `exhaustive-deps`: 依存配列を正しくする。関数は `useCallback` にする。

## バックエンド（`backend/`）
```bash
docker compose up -d --wait   # テストは PostgreSQL（ホスト側 5433）に接続する
cd backend && ./gradlew check # test + Checkstyle。javac は -Xlint:all -Werror
```
- Checkstyle の設定は `backend/config/checkstyle/checkstyle.xml`（タブ字下げ・既存の書き方に合わせた緩めの設定。整形ではなくミスの検出が目的）。
- コンパイラ警告は `-Werror` でエラーになる。警告は抑制せず直す（例: `Serializable` には `serialVersionUID`）。
- 一部だけ速く確認したいとき: `./gradlew checkstyleMain checkstyleTest compileJava compileTestJava`。
- テストは `TestBoardFixture` で専用ボードを作り、終了後に削除する方式。開発データに依存させない。

## CI が落ちたとき
```bash
gh pr checks <PR番号>
gh run view <run-id> --log-failed
```
- `Backend (check)` / `Frontend (lint, build)` の 2 ジョブ。まず手元で同じコマンドを再現する。
- CI の DB はポート 5432（`DB_URL` で上書き）。ローカルは 5433。

## 注意
- チェックを通すためだけに、警告の抑制・ルール無効化・テスト削除をしない。
- 開発サーバーを起動する場合は `dev-servers` スキルに従い、既定ポート（8080 / 5173）を使う。
