# GitHub ブランチ保護設定（main）

`gh api` で適用する内容。リポジトリ: `yosten1595/TaskManagement`

| 設定 | 値 |
|---|---|
| Pull Request 必須 | 有効（承認 0 件。個人開発のため自己マージ可、直接 push のみ禁止） |
| force push | 禁止 |
| ブランチ削除 | 禁止 |
| 管理者にも適用 (enforce_admins) | 有効 |
| 会話の解決を必須 | 有効 |
| マージ後のブランチ自動削除 | 有効 |
| マージ方式 | Squash merge のみ |

適用コマンド:

```sh
gh api -X PUT repos/yosten1595/TaskManagement/branches/main/protection --input - <<'JSON'
{
  "required_status_checks": null,
  "enforce_admins": true,
  "required_pull_request_reviews": { "required_approving_review_count": 0 },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false,
  "required_conversation_resolution": true
}
JSON

gh repo edit yosten1595/TaskManagement --delete-branch-on-merge \
  --enable-squash-merge --enable-merge-commit=false --enable-rebase-merge=false
```

## CI を必須チェックにする（推奨）

`.github/workflows/ci.yml` が PR ごとに backend（`./gradlew check`）と frontend（lint・build）を実行する。通らない PR をマージできないようにするには、GitHub の設定で `required_status_checks` に `Backend (check)` と `Frontend (lint, build)` を追加する（初回の CI 実行後に選べる）。この設定は人が行う。

注意: プライベートリポジトリで GitHub Free の場合、ブランチ保護は利用できない（Pro 以上、または public 化が必要）。
