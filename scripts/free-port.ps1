# Stop the process listening on the given port(s) so a dev server can start on its default port.
# Default ports: backend 8080 / frontend 5173. See CLAUDE.md "Dev server ports".
# Messages are ASCII on purpose: Windows PowerShell 5.1 misreads UTF-8 files without a BOM.
#
# Usage: powershell -NoProfile -File scripts/free-port.ps1 -Port 8080,5173
param(
    [Parameter(Mandatory = $true)]
    [string[]]$Port
)

# With "powershell -File", "-Port 8080,5173" arrives as one string, so split it here.
$Port = @($Port -split '[,\s]+' | Where-Object { $_ } | ForEach-Object { [int]$_ })

# Never stop these (e.g. Docker publishes the DB port through them).
$protected = @('com.docker.backend', 'Docker Desktop', 'wslrelay', 'System', 'Idle')

$failed = $false
foreach ($p in $Port) {
    $owners = @(Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique)

    if ($owners.Count -eq 0) {
        Write-Host "port ${p}: free"
        continue
    }

    foreach ($id in $owners) {
        $proc = Get-Process -Id $id -ErrorAction SilentlyContinue
        $name = if ($proc) { $proc.ProcessName } else { '(exited)' }
        if ($protected -contains $name -or $id -le 4) {
            Write-Host "port ${p}: $name (PID $id) is protected, not stopping it"
            $failed = $true
            continue
        }
        Write-Host "port ${p}: stopping $name (PID $id)"
        Stop-Process -Id $id -Force -ErrorAction SilentlyContinue
    }

    Start-Sleep -Seconds 1
    if (Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue) {
        Write-Host "port ${p}: still in use"
        $failed = $true
    } else {
        Write-Host "port ${p}: released"
    }
}

if ($failed) { exit 1 }
