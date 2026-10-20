# TaskManagement – Claude Code 作業ルール

以下は厳守するルール。例外は認めない。迷ったら作業を止めてユーザーに確認する。

## 1. Issue ファースト
- コード・ドキュメントの変更を伴う作業は、**必ず先に GitHub Issue を作る**（既存 Issue があればそれを使う）。
- Issue なしでブランチ作成・コミットをしない。
- Issue は `.github/ISSUE_TEMPLATE/` のテンプレートに沿って、目的・完了条件を書く。
- 作成は `gh issue create`。作成前に重複する Issue がないか `gh issue list` で確認する。

## 2. ブランチ
- `main` では作業しない。`main` への直接コミット・直接 push は禁止。
- 命名規則: `<type>/<issue番号>-<kebab-case の短い説明>`
  - type: `feature` / `fix` / `docs` / `refactor` / `chore` / `test`
  - 例: `feature/12-add-task-sort`、`fix/30-null-due-date`
  - 正規表現: `^(feature|fix|docs|refactor|chore|test)/[0-9]+-[a-z0-9]+(-[a-z0-9]+)*$`
- 1 Issue = 1 ブランチ = 1 PR。ブランチは最新の `main` から切る。

## 3. コミット
- メッセージは英語の命令形で簡潔に。末尾に関連 Issue を `(#12)` の形で付ける。
- 例: `Add sort option to task list (#12)`
- 1 コミット 1 論理変更。無関係な変更を混ぜない。

## 4. Push と Pull Request
- push は作業ブランチのみ。`--force` / `--force-with-lease` は使わない。
- 変更は必ず PR 経由で `main` に入れる。PR 本文には `Closes #<番号>` を入れ、`.github/pull_request_template.md` に沿って書く。
- PR の作成まではやってよいが、**マージはユーザーが行う**（Claude はマージしない）。
- ブランチ保護（PR 必須・直接 push 禁止・force push 禁止）を回避・解除する操作をしない。

## 5. 強制の仕組み
- `.claude/hooks/guard-git.ps1`（`.claude/settings.json` の PreToolUse フック）が上記のうち機械的に判定できるものをブロックする:
  - main 上での `git commit` / `git push`、main・master を対象とする push
  - force push
  - 命名規則に違反するブランチの作成
  - Issue 番号を含まない `gh pr create`
- フックが拒否した場合は回避せず、ルールに沿ってやり直す（Issue を作る等）。
- GitHub 側の設定は `.github/BRANCH_PROTECTION.md` を参照。
