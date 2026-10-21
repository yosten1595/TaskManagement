# PreToolUse hook: dev servers must run on their default ports (backend 8080 / frontend 5173).
# Blocks commands that pick another port. See CLAUDE.md "Dev server ports".
# Exit code 2 blocks the tool call and feeds stderr back to Claude.
$ErrorActionPreference = 'Stop'
[Console]::InputEncoding = [Text.Encoding]::UTF8
[Console]::OutputEncoding = [Text.Encoding]::UTF8

function Block($msg) {
    [Console]::Error.WriteLine("[CLAUDE.md rule violation] $msg")
    exit 2
}

try {
    $data = [Console]::In.ReadToEnd() | ConvertFrom-Json
    $command = $data.tool_input.command
} catch { exit 0 }
if (-not $command) { exit 0 }

$hint = 'If the port is in use, stop the process holding it with "powershell -NoProfile -File scripts/free-port.ps1 -Port <port>" and start on the default port (backend 8080 / frontend 5173).'

foreach ($stmt in ($command -split '&&|\|\||;|\r?\n')) {
    # Frontend: --port on vite / npm run dev, or disabling strictPort
    if ($stmt -match '\b(vite|npm(\.cmd)?\s+(run\s+)?dev|npx\s+vite)\b') {
        if ($stmt -match '(^|\s)(--port|-p)(\s|=)') {
            Block "Do not pass --port to the frontend dev server. $hint"
        }
        if ($stmt -match '(?i)--strictPort[=\s]+false') {
            Block "Do not disable strictPort to fall back to another port. $hint"
        }
    }
    # Backend: overriding server.port / SERVER_PORT
    if ($stmt -match '(?i)\b(gradlew|gradle|java)\b' -and $stmt -match '(?i)(server\.port|server_port)') {
        Block "Do not override the backend server.port. $hint"
    }
    if ($stmt -match '(?i)\$env:SERVER_PORT\s*=' -or $stmt -match '(?i)\bSERVER_PORT=') {
        Block "Do not change the backend port with SERVER_PORT. $hint"
    }
}
exit 0
