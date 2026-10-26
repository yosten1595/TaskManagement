# TaskBoard

付箋やメモ帳で管理していたタスクをデジタル化し、期限・優先度・進捗をひと目で把握できる**カンバン型のタスク管理 Web アプリケーション**です。個人の作業管理から 2〜10 名規模のチームまでを想定しています。

- フロントエンド: React のシングルページアプリ（SPA）
- バックエンド: Spring Boot の REST API
- データ保存: PostgreSQL（ローカル開発は Docker Compose）

要件の詳細は [requirements.html](requirements.html) と、そこからリンクされている個別ドキュメントにまとめています。

## 解決したい課題

| 現状（付箋・メモ帳） | TaskBoard を使うと |
| --- | --- |
| 期限が近づいても気づかず遅延する | 期限が近づくと自動で色が変わる |
| どれが急ぎか分からない | 優先度・ラベルで重要度が一目で分かる |
| サブタスクを管理できない | カード内のチェックリストで進捗を管理できる |
| プロジェクトが増えると混在する | ボードを分けて整理できる |
| チームで進捗を共有しにくい | カンバンボードで即座に共有できる |

## 主な機能（要件）

| 領域 | 内容 |
| --- | --- |
| ボード / リスト / カード | 作成・削除、リストとカードの階層管理 |
| 並び替え | ドラッグ&ドロップ（列間移動・列内並び替え）、優先度順・期限順ソート |
| カード詳細 | 期限、優先度（高・中・低）、ラベル、チェックリスト、警告タイミング（既定 3 日前） |
| テンプレート | 定期タスクをワンタップで再作成 |
| 検索・フィルタ | キーワード、ラベル・優先度・期限での絞り込み（条件に合うカードのみ表示） |
| 仕上げ | ダークモード、レスポンシブ、アクセシビリティ |

機能の優先度（Must / Should / Nice）と開発フェーズ（Phase 1〜4）は [requirements.html](requirements.html) の「開発フェーズ」を参照してください。

## 現在の実装状況

要件定義書のすべての機能（Must・Should・Nice）が実装済みで、Phase 1〜4 は完了しています。

| 項目 | 状況 |
| --- | --- |
| PostgreSQL（Docker）と JPA エンティティ | 実装済み。スキーマは Flyway のマイグレーションで管理（`ddl-auto=validate`） |
| 動作確認用テストデータの自動投入 | 実装済み |
| ボード一覧、ボード画面、カード検索（キーワード・優先度・リスト） | 実装済み |
| カードの作成・編集・削除（期限・優先度・ラベル付き） | 実装済み |
| ドラッグ&ドロップ（リスト間移動・リスト内並び替え）、期限順・優先度順ソート | 実装済み |
| 期限の色分け表示（完了リストでは「完了」表示） | 実装済み |
| チェックリストの追加・チェック・削除（カード編集フォームで編集し、保存時に反映） | 実装済み |
| チェック進捗バー（カード一覧・編集モーダル） | 実装済み |
| 警告日数（期限の何日前から「期限間近」にするか。カードごとに設定、既定 3 日） | 実装済み |
| 検索・絞り込み | 実装済み（画面はキーワード・優先度・ラベル（「ラベルなし」も可）・期限。期限は日付を入力して、その期限のカードに絞り込む） |
| ボードの作成／名前変更／削除 | 実装済み（API・画面） |
| ホームのボードタイルの件数と期限バッジ（期限切れ・期限間近・すべて順調） | 実装済み |
| リスト（未着手 / 進行中 / 完了の3列に固定） | ボード作成時に自動で作成。追加・名前変更・削除・並び替えは行わない |
| ラベルの作成／編集／削除 | 行わない（画面・API ともになし）。ラベルは一覧取得、カードへの付与、絞り込みのみ |
| カード詳細モーダル（カードをクリックして編集） | 実装済み |
| テンプレート | 実装済み。保存（カード編集モーダルとカード追加モーダルの「テンプレートとして保存」）、利用（カード追加モーダルの「テンプレートから追加」）、編集・削除（ボード画面の「テンプレート管理」） |
| レスポンシブ（幅 375px〜） | 実装済み。ホーム・ボード・各モーダルで横にはみ出さないことを確認（Edge で幅 375px を撮影して確認）。カードのドラッグ移動は、タッチでは 250ms 長押ししてから動かす |
| デザイン（カフェ風） | 実装済み。ライトは生成りの紙とエスプレッソ・キャラメル、ダークは夜のカフェ（深いコーヒー色）。見出しは Noto Serif JP（Google Fonts から読み込み）。ラベルの初期色も落ち着いた色に変更 |
| ブラウザ対応 | コードで確認済み。使っている機能（`color-mix()`、`:focus-visible`、`inset` など）は Chrome・Firefox・Edge・Safari の最新 2 バージョンで使える。実機での動作確認は Edge のみ。`crypto.randomUUID()` は http のネットワーク越しで使えないため、使わない形にした |
| 性能（カード 150 枚） | API は 100ms 未満、カードを開く操作は約 60ms。ドラッグ中の一瞬の引っかかりは、製品版のビルド（`npm run preview`）で最大 83〜150ms（中央値 約 100ms）。開発用サーバーは描画が重く、約 300ms になる（GPU を使わない Edge での測定） |
| キーボード操作 | 実装済み。Tab で全ての操作対象に移動でき、カードは Enter で開き Space でドラッグ移動。モーダルは開くとフォーカスが中に移り、Tab が外へ抜けず、Esc で閉じて元の位置にフォーカスが戻る |
| 配色のコントラスト（WCAG AA） | 実装済み。ライト・ダークで、ホーム・ボード・各モーダルの文字色と背景色を計測し、基準（通常文字 4.5:1）未満がないことを確認（無効状態のボタンは対象外）。ラベルの文字色は背景色に合わせて白か黒を自動で選ぶ |
| ダークモード（手動切り替え） | 実装済み。ヘッダーの「テーマ」ボタンで 自動（OS の設定に従う）→ ライト → ダーク の順に切り替え、選択はブラウザに保存 |
| データのエクスポート・インポート（JSON） | 実装済み。ホーム画面の「エクスポート」で全ボード・テンプレートをファイルに保存し、「インポート」でファイルの内容を新しいボード・テンプレートとして追加する（既存のデータは消さない。ラベルは名前で対応づけ） |
| アニメーション | 実装済み。モーダルは下からふわっと現れ、「元に戻す」の通知はスライドし、新しいカードはフェードイン、カードやボタンは色や影がなめらかに変わる。OS の「動きを減らす」設定ではすべて止まる |
| 操作の取り消し（Undo） | 実装済み。カードの削除（まとめて削除も）、カードのドラッグ移動、ボードの削除のあと、画面の下に「元に戻す」の通知が 10 秒間出る（Ctrl+Z / Cmd+Z でも可）。直前の操作 1 件だけが対象。ページを移ると取り消せなくなる |

## 技術スタック

| 区分 | 採用技術 | バージョン |
| --- | --- | --- |
| フロントエンド | React / React DOM | 19.3.1 |
| | TypeScript | 6.0.3 |
| | Vite | 8.3.5 |
| | React Router | 7.18.4 |
| | Oxlint（Lint） | 1.89.0 |
| | Node.js（動作確認環境） | 24（LTS） |
| バックエンド | Java（Temurin） | 25（LTS） |
| | Spring Boot | 4.1.1 |
| | Gradle（Wrapper） | 9.8.1 |
| | Spring Data JPA（Hibernate） / Flyway / Validation / Actuator | Spring Boot の依存管理に従う |
| データベース | PostgreSQL（Docker イメージ `postgres:18`） | 18 |
| 管理 | Git / GitHub | — |

バージョンの正は、フロントエンドが `frontend/package-lock.json`、バックエンドが `backend/build.gradle` と `backend/gradle/wrapper/gradle-wrapper.properties` です。変更したら [requirements.html](requirements.html) の「技術スタック」も更新してください。

## ディレクトリ構成

```
.
├── backend/              Spring Boot (Gradle) の REST API
│   ├── config/checkstyle/ Checkstyle の設定
│   ├── src/main/resources/db/migration/  Flyway のマイグレーション（V1: 初期スキーマ、V2: 外部キーのインデックス）
│   ├── src/test/         API の結合テスト（PostgreSQL に接続して実行）
│   └── src/main/java/com/taskboard/api/
│       ├── controller/   REST コントローラ
│       ├── service/      取得・作成・更新・削除などのロジック
│       ├── repository/   Spring Data JPA リポジトリ
│       ├── entity/       JPA エンティティ
│       ├── exception/    共通の例外（存在しない ID は ResourceNotFoundException → 404）
│       ├── dto/          API のリクエスト・レスポンス用 DTO
│       └── seed/         動作確認用テストデータと、固定ラベルの登録
├── frontend/             React + TypeScript + Vite の SPA
│   └── src/              pages / components / api.ts / types.ts / undo.tsx など
├── docker-compose.yml    開発用 PostgreSQL
├── scripts/              開発補助スクリプト（ポート解放など）
├── requirements.html     製品要件定義書
├── .github/              Issue / PR テンプレート、CI（workflows）、ブランチ保護の方針
├── .claude/              Claude Code 用のフック・スキル
└── CLAUDE.md             開発ルール（Claude Code 向け）
```

## ローカルでの起動方法

### 前提

- Docker（Docker Compose）
- JDK 25
- Node.js 24 と npm

### ポート

開発サーバーのポートは固定です。フロントエンドは `/api` を `localhost:8080` に転送するため、別ポートでは動作しません。

| 対象 | ポート |
| --- | --- |
| バックエンド | 8080 |
| フロントエンド | 5173 |
| PostgreSQL（ホスト側） | 5433 |

ポートが使用中の場合は、占有しているプロセスを PID 指定で停止してから起動します。

```powershell
powershell -NoProfile -File scripts/free-port.ps1 -Port 8080,5173
```

### 手順

1. データベースを起動する

   ```bash
   docker compose up -d --wait
   ```

2. バックエンドを起動する（別ターミナル）

   ```bash
   cd backend
   ./gradlew bootRun
   ```

   `http://localhost:8080/actuator/health` が `UP` になれば起動完了です。`boards` テーブルが空のときだけ、テストデータが自動で投入されます。

3. フロントエンドを起動する（別ターミナル）

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

   ブラウザで http://localhost:5173 を開きます。

### スキーマの変更（Flyway）

テーブルの変更は、`backend/src/main/resources/db/migration/` に `V<番号>__<説明>.sql` を追加して行います（既存のファイルは書き換えない）。Hibernate はスキーマを変更せず、エンティティとずれていると起動時に失敗します（`ddl-auto=validate`）。以前の `ddl-auto=update` で作られた既存の DB は、初回起動時にバージョン 1 として引き継がれ、データは消えません。

### 設定（環境変数）

| 変数 | 既定値 | 内容 |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5433/taskboard` | 接続先 DB |
| `DB_USERNAME` | `taskboard` | DB ユーザー |
| `DB_PASSWORD` | `taskboard` | DB パスワード |
| `HEALTH_DETAILS` | `always` | `/actuator/health` に DB 接続などの詳細を出すか（`always` / `never`。本番では `never`） |
| `SEED_ENABLED` | `true` | テストデータ（サンプルのボード・カード）の自動投入（本番では `false`）。固定の3ラベルは、この設定に関係なく起動時に必ず登録される |

### ビルド・テスト・Lint

Pull Request ごとに GitHub Actions（`.github/workflows/ci.yml`）が同じチェックを実行します。

```bash
# バックエンド（テストは DB が起動している状態で実行する）
# check = test + Checkstyle。コンパイラ警告（-Xlint:all）は警告 0 を要求する
cd backend && ./gradlew check

# フロントエンド
cd frontend
npm run lint        # Oxlint（警告もエラー扱い）
npm run typecheck   # tsc -b
npm test            # Vitest（期限判定・ラベル色・チェックリスト差分）
npm run build

# 本番ビルドの確認（開発サーバーと同じ 5173 で動く。先に開発サーバーを止める）
npm run preview
```

## API（現在提供しているもの）

| メソッド | パス | 内容 |
| --- | --- | --- |
| GET | `/api/boards` | ボード一覧（各ボードのカード数 `cardCount` と、期限切れ `overdueCount`・期限間近 `soonCount` の件数付き） |
| GET | `/api/boards/{id}` | ボード詳細（リスト・カードを含む） |
| POST | `/api/boards` | ボード作成（`title` 必須）。初期リスト「未着手 / 進行中 / 完了」も作る |
| PUT | `/api/boards/{id}` | ボード名の変更 |
| DELETE | `/api/boards/{id}` | ボード削除（リスト・カード・チェック項目も削除） |
| GET | `/api/labels` | ラベル一覧 |
| GET | `/api/templates` | テンプレート一覧（新しい順） |
| POST | `/api/templates` | テンプレート作成（`title` 必須。`description`、`priority`、`warnDays`（既定 3）、`labelIds`、`checklist`（項目テキストの配列）は任意） |
| PUT | `/api/templates/{id}` | テンプレート更新（全項目を置き換え） |
| DELETE | `/api/templates/{id}` | テンプレート削除（作成済みのカードは残る） |
| POST | `/api/templates/{id}/cards` | テンプレートからカード作成（`listId` 必須。リスト末尾に追加。期限なし、チェック項目は未完了） |
| GET | `/api/export` | 全ボード・リスト・カード・チェック項目・テンプレートを JSON で返す（`version`、`exportedAt`、`boards`、`templates`。ID は含まず、並び順は配列の順。ラベルは名前） |
| POST | `/api/import` | エクスポートした JSON から、新しいボード・テンプレートを追加（`201`。件数を返す）。既存のデータは消さず、同名のラベルがあれば付ける。`version` の不一致や入力の誤りは `400` で、何も追加しない |
| GET | `/api/cards` | カード検索。クエリ: `keyword`、`priority`、`listId`、`labelId`、`boardId`（すべて任意。`boardId` はそのボードのカードだけに絞る。ボード画面は必ず指定する） |
| GET | `/api/cards/{id}` | カード詳細 |
| POST | `/api/cards` | カード作成（`listId`、`title` 必須。`warnDays` は任意で 0〜365、省略時は 3。`description`、`checklist`（`text`・`done`）、`position`（挿入位置。後ろのカードをずらす。省略時は末尾）も任意で、削除したカードを元に戻すときに使う） |
| PUT | `/api/cards/{id}` | カード更新（全項目を置き換え。`warnDays` を省略すると 3 に戻る。所属リストと並び順は変えない） |
| PUT | `/api/cards/{id}/move` | カード移動（`listId`、`position`）。移動元・移動先の並び順を振り直す |
| DELETE | `/api/cards/{id}` | カード削除（ラベルの紐付け・チェック項目も削除。並び順を詰める） |
| PUT | `/api/lists/{id}/sort` | リスト内のカードを並べ替えて保存（`by`: `DUE_DATE` / `PRIORITY`） |
| POST | `/api/cards/{cardId}/checklist` | チェック項目を末尾に追加（更新後のカードを返す） |
| PUT | `/api/cards/{cardId}/checklist/{itemId}` | チェック項目の更新（`text`、`done`。更新後のカードを返す） |
| DELETE | `/api/cards/{cardId}/checklist/{itemId}` | チェック項目の削除（並び順を詰める） |
| GET | `/actuator/health` | ヘルスチェック（DB 接続状態を含む） |

### 入力の制限

タイトルやチェック項目のテキストは 255 文字まで（超えると `400`）。インポートは、ボード 100 件・リスト 10 件（1 ボード）・カード 1000 件（1 リスト）・チェック項目 200 件（1 カード／テンプレート）・テンプレート 500 件までで、超えると `400` で何も追加しない。検索の `keyword` に含まれる `%` と `_` は通常の文字として検索する。

### API 形式

| 項目 | 内容 |
| --- | --- |
| スタイル | REST。リソースは `/api/` 配下、複数形の名詞（`boards`、`cards`） |
| データ形式 | JSON（`application/json`）。フィールド名は camelCase |
| ID | UUID 文字列 |
| 日時 | `createdAt` は UTC の ISO 8601（例: `2026-10-21T12:00:00Z`） |
| 日付 | `dueDate` は `YYYY-MM-DD`。期限なしは `null` |
| 優先度 | `priority` は `HIGH` / `MID` / `LOW`。未設定は `null` |
| 並び順 | 配列は `position` の昇順で返す |
| 一覧 | ページングなし。該当なしは空配列 `[]` |
| エラー | Spring 標準のエラー JSON（`status` / `error` / `path` など）。存在しない ID は `404`、`priority` や UUID の形式不正は `400` |

レスポンスの例（`GET /api/boards/{id}`、一部省略）:

```json
{
  "id": "0b0f6a52-...",
  "title": "サンプルプロジェクト",
  "createdAt": "2026-10-21T12:00:00Z",
  "lists": [
    {
      "id": "7c1e...",
      "title": "未着手",
      "position": 0,
      "cards": [
        {
          "id": "a3d9...",
          "listId": "7c1e...",
          "title": "ログイン画面を作る",
          "description": null,
          "dueDate": "2026-10-25",
          "priority": "HIGH",
          "warnDays": 3,
          "position": 0,
          "createdAt": "2026-10-21T12:00:00Z",
          "labels": [{ "id": "5b2f...", "name": "フロントエンド", "color": "#5b7b48" }],
          "checklist": [{ "id": "e801...", "text": "画面設計", "done": true, "position": 0 }]
        }
      ]
    }
  ]
}
```

`GET /api/boards` は `id` / `title` / `createdAt` に加えて `cardCount` / `overdueCount` / `soonCount` を持つ配列（期限切れ・期限間近は画面のカード表示と同じ基準で、「完了」リストのカードは数えない。サーバーの現在日付で判定）、`GET /api/cards` は上記の `cards` 要素と同じ形の配列を返します。`keyword` はタイトル・説明の部分一致（大文字小文字を区別しない）です。作成・更新・削除 API もこの形式に揃えます。作成は `201`、削除は `204`、`title` やチェック項目の `text` が空なら `400`、存在しない ID は `404` を返します。

## データモデル

Board → List → Card の階層に、Label（カードと多対多）、CheckItem（カードのチェックリスト）、Template（定期タスクの雛形）を加えた構成です。並び順は配列ではなく `position` 列で持ちます。ER 図とフィールド定義は [requirements.html](requirements.html) の「データモデル（ER 図）」を参照してください。

## 要件定義ドキュメント

[requirements.html](requirements.html) をブラウザで開くと全体を確認できます。個別ドキュメントは次のとおりです。

| ドキュメント | 内容 |
| --- | --- |
| [ユースケース一覧](https://claude.ai/artifact/VQDoDfXs8c4XQJPHq6y9Kq) | UC-01〜UC-11 の全 11 ケース |
| [画面イメージ（ワイヤーフレーム）](https://claude.ai/artifact/98qWsYAwusGCRU6n7UTruX) | ホーム・カンバンボード・カード詳細モーダルの 3 画面 |
| [機能要件（詳細）](https://claude.ai/artifact/Bg5GX3uRMtQBGsW9ZtcGhN) | Must / Should / Nice の機能一覧と非機能要件 |
| [データ機能（CRUD 仕様）](https://claude.ai/artifact/Nx6gUGfXsfoFQc4oUz5Ccj) | DF-01〜DF-19 の CRUD 操作とフィールド定義 |

## 開発ルール

作業は GitHub 上のルールに従います。詳細は [CLAUDE.md](CLAUDE.md) を参照してください。

- **Issue ファースト**: 変更を伴う作業は、先に Issue を作る（`.github/ISSUE_TEMPLATE/` のテンプレートを使用）
- **ブランチ**: `main` では作業しない。`<type>/<issue番号>-<kebab-case>`（type は `feature` / `fix` / `docs` / `refactor` / `chore` / `test`）。1 Issue = 1 ブランチ = 1 PR
- **コミット**: 英語の命令形で簡潔に、末尾に `(#12)` の形で Issue 番号を付ける
- **PR**: 本文に `Closes #<番号>` を入れ、[テンプレート](.github/pull_request_template.md) に沿って書く。マージは人が行う
- **push**: 作業ブランチのみ。force push は禁止
- ブランチ保護の設定は [.github/BRANCH_PROTECTION.md](.github/BRANCH_PROTECTION.md) を参照
