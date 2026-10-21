---
name: dev-servers
description: バックエンド（Spring Boot）とフロントエンド（Vite）の開発サーバーを起動・再起動するときに使う。ポート競合の解消と、既定ポートでの起動手順を定める。「サーバーを起動して」「画面を確認したい」「ポートが使われている」などで使用する。
---

# 開発サーバーの起動

## ルール
- 起動するポートは**必ず既定ポート**にする。別ポートへ逃がさない。
  - バックエンド: **8080**（`backend/src/main/resources/application.properties` の `server.port`）
  - フロントエンド: **5173**（`frontend/vite.config.ts` の `server.port`。`strictPort: true`）
- 理由: フロントエンドは `/api` を `localhost:8080` へプロキシする。別ポートで起動すると接続先が合わず動作しない。
- ポートが使用中なら、**占有しているプロセスを停止してから**起動する。

## 手順
1. DB を起動する: `docker compose up -d --wait`（ホスト側ポートは 5433）
2. ポートを解放する:
   `powershell -NoProfile -File scripts/free-port.ps1 -Port 8080,5173`
   - 空いていれば何もしない。使用中なら占有プロセスを PID 指定で停止する。
   - Docker 関連のプロセスは停止対象外。止められなかった場合は、ユーザーに伝えて判断を仰ぐ。
3. バックエンドを起動する: `backend` で `./gradlew bootRun`
   - `http://localhost:8080/actuator/health` が `UP` になるまで待つ。
4. フロントエンドを起動する: `frontend` で `npm run dev`
   - `http://localhost:5173` で応答することを確認する。
5. 確認が終わったら、**自分で起動したサーバーだけ**を止める（ポート番号から PID を特定して停止する）。

## してはいけないこと
- `--port` / `-p`、`-Dserver.port`、`SERVER_PORT`、`strictPort: false` で別ポートにすること（`.claude/hooks/guard-ports.ps1` がブロックする）。
- `taskkill /IM java.exe` のように、プロセス名で無関係なプロセスまで止めること。必ず PID で止める。
- 5432 を使っているローカルの PostgreSQL など、アプリのサーバー以外のプロセスを止めること。
