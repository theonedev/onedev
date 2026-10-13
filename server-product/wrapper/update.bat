@echo off
setlocal EnableExtensions DisableDelayedExpansion
set "WRAPPER_UPDATE_SCRIPT=%~f0"
set "WRAPPER_UPDATE_COUNT=0"
:arguments
if "%~1"=="" goto run
set "WRAPPER_UPDATE_ARG_%WRAPPER_UPDATE_COUNT%=%~1"
set /a WRAPPER_UPDATE_COUNT+=1 >nul
shift
goto arguments
:run
rem Pass arguments as data, including paths containing spaces or apostrophes.
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$source = Get-Content -LiteralPath $env:WRAPPER_UPDATE_SCRIPT -Raw; & ([scriptblock]::Create(($source -split '(?m)^# POWERSHELL UPDATER\r?$', 2)[1]))"
exit /b %ERRORLEVEL%

# POWERSHELL UPDATER
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2
$wrapperDirectory = Split-Path -Parent $env:WRAPPER_UPDATE_SCRIPT
$utf8 = [Text.UTF8Encoding]::new($false)
$nativeNames = @('wrapper-linux-x86-64', 'wrapper-linux-arm-64', 'wrapper-macosx-arm-64',
    'wrapper-windows-x86-64.exe', 'wrapper-windows-arm-64.exe')

function Show-Usage {
    @'
Usage: update.bat [COMMAND]
       update.bat [WRAPPER_DIRECTORY] [--sandbox]

Commands:
  update [DIRECTORY] [--sandbox] Download and import the latest release (default)
                                 or copy build.bat output from a wrapper checkout
  import ARCHIVE [OPTIONS]       Import a local release archive
    --checksum FILE              Use FILE instead of ARCHIVE.sha256
    --destination DIRECTORY      Import here instead of the script directory
    --sandbox                    Refresh binaries and scripts in ../target/sandbox
  test                           Run offline import, update, and sandbox checks
  help                           Show this help

Requires Windows PowerShell 5.1; release imports also require tar.exe.
Use update.sh check-package on Unix to verify packaging and executable permissions.
'@
}

function Get-FullPath([string] $Path) {
    $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Path)
}

function New-Directory([string] $Path) {
    [IO.Directory]::CreateDirectory($Path) | Out-Null
}

function New-TemporaryDirectory {
    $path = Join-Path ([IO.Path]::GetTempPath()) ('onedev-wrapper-' + [guid]::NewGuid().ToString('N'))
    New-Directory $path
    return $path
}

function Assert-NoLinks([string] $Path) {
    $item = Get-Item -LiteralPath $Path -Force
    if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) { throw "Links are not supported: $Path" }
    if ($item.PSIsContainer) {
        foreach ($child in Get-ChildItem -LiteralPath $Path -Force) { Assert-NoLinks $child.FullName }
    }
}

function Remove-Tree([string] $Path, [string] $Parent) {
    $fullPath = [IO.Path]::GetFullPath($Path)
    $fullParent = [IO.Path]::GetFullPath($Parent).TrimEnd('\') + '\'
    if (-not $fullPath.StartsWith($fullParent, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing removal outside $Parent : $Path"
    }
    if (Test-Path -LiteralPath $fullPath) {
        Assert-NoLinks $fullPath
        Remove-Item -LiteralPath $fullPath -Recurse -Force
    }
}

function Write-Text([string] $Path, [string] $Text) {
    [IO.File]::WriteAllText($Path, $Text, $utf8)
}

function Get-Digest([string] $Path) {
    (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Assert-ReleasePath([string] $Name) {
    if ($Name -cnotmatch '^[A-Za-z0-9._+/-]+$' -or $Name.StartsWith('/')) {
        throw "Invalid release path: $Name"
    }
    foreach ($part in $Name.Split('/')) {
        # Windows normalizes trailing dots and device names, even with extensions.
        if (-not $part -or $part.EndsWith('.') -or $part -match '^(CON|PRN|AUX|NUL|COM[0-9]|LPT[0-9])($|\.)') {
            throw "Invalid release path: $Name"
        }
    }
}

function Read-Checksum([string] $Line) {
    if ($Line -cnotmatch '^([0-9a-f]{64})  (.+)$') { throw "Invalid checksum entry: $Line" }
    $digest, $name = $Matches[1], $Matches[2]
    Assert-ReleasePath $name
    return @{ Digest = $digest; Name = $name }
}

function Get-FileList([string] $Directory) {
    $prefix = $Directory.TrimEnd('\', '/') + '\'
    [string[]] $names = @(Get-ChildItem -LiteralPath $Directory -File -Recurse -Force | ForEach-Object {
        $_.FullName.Substring($prefix.Length).Replace('\', '/')
    })
    [Array]::Sort($names, [StringComparer]::Ordinal)
    return $names
}

function Write-Manifest([string] $Directory) {
    $lines = foreach ($name in Get-FileList $Directory) {
        if ($name -eq 'SHA256SUMS') { continue }
        Assert-ReleasePath $name
        (Get-Digest (Join-Path $Directory $name)) + '  ' + $name
    }
    Write-Text (Join-Path $Directory 'SHA256SUMS') (($lines -join "`n") + "`n")
}

function Test-Manifest([string] $Directory) {
    $seen = @{}
    foreach ($line in [IO.File]::ReadAllLines((Join-Path $Directory 'SHA256SUMS'))) {
        $entry = Read-Checksum $line
        if ($entry.Name -eq 'SHA256SUMS' -or $seen.ContainsKey($entry.Name)) { throw 'Duplicate or self-referencing manifest entry' }
        $seen[$entry.Name] = $true
        if ((Get-Digest (Join-Path $Directory $entry.Name)) -cne $entry.Digest) {
            throw "Release file checksum does not match: $($entry.Name)"
        }
    }
    $files = @(Get-FileList $Directory | Where-Object { $_ -ne 'SHA256SUMS' })
    if ($files.Count -ne $seen.Count) { throw 'Release manifest must cover every file exactly once' }
    foreach ($name in $files) {
        if (-not $seen.ContainsKey($name)) { throw "Unlisted release file: $name" }
    }
}

function Invoke-Tar([string[]] $Arguments) {
    & tar.exe @Arguments
    if ($LASTEXITCODE -ne 0) { throw "tar.exe failed with exit code $LASTEXITCODE" }
}

function Get-Sandbox([string] $Destination) {
    $sandbox = Get-FullPath (Join-Path $Destination '../target/sandbox')
    if (-not (Test-Path -LiteralPath $sandbox -PathType Container)) {
        throw "Development sandbox not found: $sandbox. Run ./dev.sh build first."
    }
    return $sandbox
}

function Copy-Contents([string] $Source, [string] $Destination) {
    Assert-NoLinks $Source
    New-Directory $Destination
    foreach ($item in Get-ChildItem -LiteralPath $Source -Force) {
        Copy-Item -LiteralPath $item.FullName -Destination $Destination -Recurse -Force
    }
}

function Update-Sandbox([string] $Distribution, [string] $Sandbox) {
    $temporary = Join-Path $Sandbox ('.wrapper-update-' + [guid]::NewGuid().ToString('N'))
    New-Directory $temporary
    try {
        $files = @(Get-FileList $Distribution | Where-Object {
            $_ -match '^(agent/)?(boot/wrapper[^/]*|bin/[^/]+\.(sh|bat))$'
        })
        foreach ($name in $files) {
            $target = Join-Path $Sandbox $name
            # Check each existing ancestor before staging or replacing sandbox files.
            for ($parent = Split-Path -Parent $target; $parent.Length -ge $Sandbox.Length; $parent = Split-Path -Parent $parent) {
                if ((Test-Path -LiteralPath $parent) -and ((Get-Item -LiteralPath $parent -Force).Attributes -band [IO.FileAttributes]::ReparsePoint)) {
                    throw "Sandbox directory is a link: $parent"
                }
            }
            New-Directory (Split-Path -Parent (Join-Path $temporary $name))
            Copy-Item -LiteralPath (Join-Path $Distribution $name) -Destination (Join-Path $temporary $name)
        }
        foreach ($name in $files) {
            $target = Join-Path $Sandbox $name
            New-Directory (Split-Path -Parent $target)
            if (Test-Path -LiteralPath $target) {
                # Replacing the entry avoids writing into an existing executable.
                [IO.File]::Replace((Join-Path $temporary $name), $target, [System.Management.Automation.Language.NullString]::Value)
            } else {
                [IO.File]::Move((Join-Path $temporary $name), $target)
            }
        }
        Write-Host "Updated wrapper binaries and scripts in $Sandbox"
    } finally { Remove-Tree $temporary $Sandbox }
}

function Prepare-Distribution([string] $Release, [string] $Destination, [bool] $Sandbox) {
    $Destination = Get-FullPath $Destination
    $configuration = Join-Path $Destination 'wrapper.conf'
    if (-not (Test-Path -LiteralPath $configuration -PathType Leaf)) { throw "Missing $configuration" }
    $sandboxPath = if ($Sandbox) { Get-Sandbox $Destination }
    Assert-NoLinks $Release
    Assert-NoLinks $Destination
    foreach ($name in ($nativeNames + @('wrapper.jar'))) {
        if (-not (Test-Path -LiteralPath (Join-Path $Release "boot/$name") -PathType Leaf)) { throw "Missing release artifact: $name" }
    }
    if (-not (Test-Path -LiteralPath (Join-Path $Release 'VERSION') -PathType Leaf)) { throw 'Missing release VERSION' }
    $temporary = New-TemporaryDirectory
    try {
        $prepared = Join-Path $temporary 'prepared'
        Copy-Contents $Destination $prepared
        $distribution = Join-Path $prepared 'distribution'
        Remove-Tree $distribution $prepared
        foreach ($name in @('bin', 'conf', 'agent/bin', 'agent/conf')) { New-Directory (Join-Path $distribution $name) }
        Copy-Contents (Join-Path $Release 'boot') (Join-Path $distribution 'boot')
        Copy-Contents (Join-Path $Release 'boot') (Join-Path $distribution 'agent/boot')
        foreach ($extension in @('sh', 'bat')) {
            $launcher = Join-Path $Release "bin/service.$extension"
            $text = [IO.File]::ReadAllText($launcher)
            if ([regex]::Matches($text, ' --script ').Count -ne 1) { throw "Unrecognized launcher format: $launcher" }
            Copy-Item -LiteralPath $launcher -Destination (Join-Path $distribution "bin/server.$extension")
            Copy-Item -LiteralPath $launcher -Destination (Join-Path $distribution "agent/bin/agent.$extension")
            foreach ($command in @('restore-db', 'backup-db', 'upgrade', 'apply-db-constraints', 'reset-admin-password', 'enable-internal-login')) {
                Write-Text (Join-Path $distribution "bin/$command.$extension") $text.Replace(' --script ', ' --command ')
            }
        }
        foreach ($kind in @('server', 'agent')) {
            $prefix, $mainClass, $displayName, $classpath = '', 'io.onedev.commons.bootstrap.Bootstrap', 'OneDev Server', ''
            if ($kind -eq 'agent') {
                $prefix, $mainClass, $displayName, $classpath = 'agent/', 'io.onedev.agent.Agent', 'OneDev Agent', 'wrapper.java.classpath.2=../lib/agentVersion/*.jar'
            }
            $text = [IO.File]::ReadAllText($configuration).Replace('@bootstrap.class@', $mainClass).
                Replace('@app.name@', "onedev.$kind").Replace('@app.long.name@', $displayName).
                Replace('@app.description@', $displayName).Replace('@classpath2@', $classpath)
            if ($text -match '@[A-Za-z0-9_.]+@') { throw 'Unresolved placeholder in OneDev wrapper configuration' }
            Write-Text (Join-Path $distribution "${prefix}conf/wrapper.conf") $text
        }
        Copy-Item -LiteralPath (Join-Path $Release 'VERSION') -Destination (Join-Path $prepared 'VERSION') -Force
        Write-Manifest $prepared
        # Finish validation before replacing the previous prepared distribution.
        Remove-Tree (Join-Path $Destination 'distribution') $Destination
        Copy-Contents $distribution (Join-Path $Destination 'distribution')
        foreach ($name in @('VERSION', 'SHA256SUMS')) {
            Copy-Item -LiteralPath (Join-Path $prepared $name) -Destination (Join-Path $Destination $name) -Force
        }
        Write-Host "Imported Wrapper $([IO.File]::ReadAllText((Join-Path $Destination 'VERSION')).Trim()) into $Destination"
        if ($Sandbox) { Update-Sandbox (Join-Path $Destination 'distribution') $sandboxPath }
    } finally { Remove-Tree $temporary ([IO.Path]::GetTempPath()) }
}

function Import-Release([string] $Archive, [string] $Checksum, [string] $Destination, [bool] $Sandbox) {
    if ($Sandbox) { $null = Get-Sandbox $Destination }
    $Archive = Get-FullPath $Archive
    if (-not $Checksum) { $Checksum = "$Archive.sha256" }
    $lines = [IO.File]::ReadAllLines((Get-FullPath $Checksum))
    if ($lines.Count -ne 1) { throw 'Expected one archive checksum entry' }
    $entry = Read-Checksum $lines[0]
    if ($entry.Name -cne [IO.Path]::GetFileName($Archive) -or (Get-Digest $Archive) -cne $entry.Digest) {
        throw 'Release archive checksum does not match'
    }
    $temporary = New-TemporaryDirectory
    try {
        $entries = @(Invoke-Tar @('-tzf', $Archive))
        $root = $null
        $seen = @{}
        foreach ($entry in $entries) {
            $name = $entry.TrimEnd('/')
            Assert-ReleasePath $name
            $entryRoot = $name.Split('/')[0]
            if (-not $root) { $root = $entryRoot }
            if ($root -cne $entryRoot) { throw 'Expected one release directory' }
            if ($seen.ContainsKey($name)) { throw "Duplicate archive entry: $entry" }
            $seen[$name] = $true
        }
        if (-not $root) { throw 'Empty release archive' }
        foreach ($line in @(Invoke-Tar @('-tvzf', $Archive))) {
            if ($line -cnotmatch '^[-d]') { throw 'Archive may contain only regular files and directories' }
        }
        $extracted = Join-Path $temporary 'extracted'
        New-Directory $extracted
        Invoke-Tar @('-xzf', $Archive, '-C', $extracted)
        $release = Join-Path $extracted $root
        Assert-NoLinks $release
        Test-Manifest $release
        Prepare-Distribution $release $Destination $Sandbox
    } finally { Remove-Tree $temporary ([IO.Path]::GetTempPath()) }
}

function Update-Local([string] $Source, [string] $Destination, [bool] $Sandbox) {
    $Source = Get-FullPath $Source
    $boot = Join-Path $Source 'dist/boot'
    if (-not (Test-Path -LiteralPath (Join-Path $boot 'wrapper.jar') -PathType Leaf)) {
        throw "Missing $boot/wrapper.jar; run build.bat in $Source first"
    }
    $built = @($nativeNames | Where-Object { Test-Path -LiteralPath (Join-Path $boot $_) -PathType Leaf })
    if (-not $built.Count) { throw "No locally built wrapper binaries; run build.bat in $Source first" }
    $cargo = [IO.File]::ReadAllText((Join-Path $Source 'Cargo.toml'))
    $package = [regex]::Match($cargo, '(?ms)^\[package\]\s*\r?\n(.*?)(?=^\[|\z)').Groups[1].Value
    if ($package -notmatch '(?m)^\s*version\s*=\s*"([^"]+)"') { throw "Missing package version in $Source/Cargo.toml" }
    $version = $Matches[1]
    $temporary = New-TemporaryDirectory
    try {
        $release = Join-Path $temporary 'release'
        New-Directory (Join-Path $release 'bin')
        $bundled = Join-Path $Destination 'distribution/boot'
        if (Test-Path -LiteralPath $bundled) { Copy-Contents $bundled (Join-Path $release 'boot') }
        Copy-Contents $boot (Join-Path $release 'boot')
        Copy-Item -LiteralPath (Join-Path $Source 'resources/App.sh.in') -Destination (Join-Path $release 'bin/service.sh')
        Copy-Item -LiteralPath (Join-Path $Source 'resources/AppCommand.bat.in') -Destination (Join-Path $release 'bin/service.bat')
        Write-Text (Join-Path $release 'VERSION') "$version-local`n"
        Write-Host "Importing local Wrapper build from $Source"
        Prepare-Distribution $release $Destination $Sandbox
    } finally { Remove-Tree $temporary ([IO.Path]::GetTempPath()) }
}

function Get-ReleaseName([string] $Html) {
    $names = @([regex]::Matches($Html, '(?i)<a\s+[^>]*?\bhref\s*=\s*["''](wrapper-[0-9][A-Za-z0-9.+-]*\.tar\.gz)["'']') |
        ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
    if ($names.Count -ne 1) { throw 'Expected exactly one Wrapper release on the public download site' }
    return $names[0]
}

function Update-Release([string] $Destination, [bool] $Sandbox) {
    $site = 'https://code.onedev.io/onedev/wrapper/~sites/public/'
    [Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12
    $temporary = New-TemporaryDirectory
    try {
        Write-Host "Checking Wrapper releases at $site"
        $page = Invoke-WebRequest -UseBasicParsing -Uri $site -TimeoutSec 30
        $name = Get-ReleaseName $page.Content
        foreach ($file in @($name, "$name.sha256")) {
            Write-Host "Downloading $file..."
            Invoke-WebRequest -UseBasicParsing -Uri "$site$file" -OutFile (Join-Path $temporary $file) -TimeoutSec 300
        }
        Import-Release (Join-Path $temporary $name) '' $Destination $Sandbox
    } finally { Remove-Tree $temporary ([IO.Path]::GetTempPath()) }
}

function Invoke-Updater([string[]] $Arguments) {
    if (-not $Arguments.Count) { $Arguments = @('update') }
    $command = $Arguments[0]
    $remaining = @($Arguments | Select-Object -Skip 1)
    switch -CaseSensitive ($command) {
        { $_ -in @('help', '-h', '--help') } { Show-Usage; return }
        'test' {
            if ($remaining.Count) { throw 'Usage: update.bat test' }
            Test-Updater
            return
        }
        'check-package' { throw 'Use update.sh check-package on Unix to verify packaging and executable permissions.' }
        'import' { }
        'update' { }
        default { $remaining = $Arguments; $command = 'update' }
    }
    $inputPath, $checksum, $destination, $sandbox = '', '', $wrapperDirectory, $false
    for ($i = 0; $i -lt $remaining.Count; $i++) {
        $argument = $remaining[$i]
        switch -CaseSensitive ($argument) {
            { $_ -in @('-h', '--help') } { Show-Usage; return }
            '--sandbox' { $sandbox = $true }
            { $_ -in @('--checksum', '--destination') -and $command -eq 'import' } {
                $i++
                if ($i -ge $remaining.Count) { throw "Missing value for $argument" }
                if ($argument -eq '--checksum') { $checksum = $remaining[$i] } else { $destination = $remaining[$i] }
            }
            default {
                if ($argument.StartsWith('-')) { throw "Unknown option: $argument" }
                if ($inputPath) { throw 'Only one archive or wrapper checkout may be imported' }
                $inputPath = $argument
            }
        }
    }
    $destination = Get-FullPath $destination
    if ($sandbox) { $null = Get-Sandbox $destination }
    if ($command -eq 'import') {
        if (-not $inputPath) { throw 'Usage: update.bat import ARCHIVE [--checksum FILE] [--destination DIRECTORY] [--sandbox]' }
        Import-Release $inputPath $checksum $destination $sandbox
    } elseif ($inputPath) {
        Update-Local $inputPath $destination $sandbox
    } else {
        Update-Release $destination $sandbox
    }
}

function Test-Updater {
    $temporary = New-TemporaryDirectory
    try {
        $fixture = Join-Path $temporary 'release/wrapper-1.0.0'
        $destination = Join-Path $temporary "wrapper inputs & user's files!"
        New-Directory (Join-Path $fixture 'boot')
        New-Directory (Join-Path $fixture 'bin')
        New-Directory $destination
        Copy-Item -LiteralPath (Join-Path $wrapperDirectory 'wrapper.conf') -Destination $destination
        foreach ($name in ($nativeNames + @('wrapper.jar', 'future-runtime.dat'))) {
            [IO.File]::WriteAllBytes((Join-Path $fixture "boot/$name"), [byte[]]@(0, 255, 13, 10, 42))
        }
        Write-Text (Join-Path $fixture 'bin/service.sh') "#!/bin/bash`nexec binary --script `"`$0`" `"`$@`"`n"
        Write-Text (Join-Path $fixture 'bin/service.bat') "@echo off`r`nbinary --script `"%~n0`" %*`r`n"
        Write-Text (Join-Path $fixture 'VERSION') "1.0.0`n"
        $archive = Join-Path $temporary 'wrapper-1.0.0.tar.gz'
        function Pack-Fixture {
            Write-Manifest $fixture
            Invoke-Tar @('-czf', $archive, '-C', (Split-Path -Parent $fixture), 'wrapper-1.0.0')
            Write-Text "$archive.sha256" ((Get-Digest $archive) + "  wrapper-1.0.0.tar.gz`n")
        }
        function Assert-Equal($Actual, $Expected, [string] $Message) {
            if ($Actual -cne $Expected) { throw "Test failed: $Message" }
        }
        function Expect-Rejection([scriptblock] $Action) {
            $before = Get-Digest (Join-Path $destination 'SHA256SUMS')
            $rejected = $false
            try { & $Action } catch { $rejected = $true }
            if (-not $rejected) { throw 'Test failed: invalid input accepted' }
            Assert-Equal (Get-Digest (Join-Path $destination 'SHA256SUMS')) $before 'invalid input changed manifest'
            Test-Manifest $destination
        }
        Pack-Fixture
        Invoke-Updater @('import', $archive, '--destination', $destination)
        Test-Manifest $destination
        foreach ($prefix in @('', 'agent/')) {
            Assert-Equal (Get-Digest (Join-Path $destination "distribution/${prefix}boot/future-runtime.dat")) (Get-Digest (Join-Path $fixture 'boot/future-runtime.dat')) 'binary bytes'
        }
        Assert-Equal ([IO.File]::ReadAllText((Join-Path $destination 'distribution/bin/backup-db.bat'))) "@echo off`r`nbinary --command `"%~n0`" %*`r`n" 'CRLF and command adaptation'
        $agentConfig = [IO.File]::ReadAllText((Join-Path $destination 'distribution/agent/conf/wrapper.conf'))
        if (-not $agentConfig.Contains('io.onedev.agent.Agent') -or -not $agentConfig.Contains('../lib/agentVersion/*.jar')) { throw 'Test failed: agent configuration' }
        Remove-Item -LiteralPath (Join-Path $fixture 'boot/future-runtime.dat')
        Pack-Fixture
        Import-Release $archive '' $destination $false
        if (Test-Path -LiteralPath (Join-Path $destination 'distribution/boot/future-runtime.dat')) { throw 'Test failed: retired artifact survived' }
        Write-Text "$archive.sha256" (('0' * 64) + "  wrapper-1.0.0.tar.gz`n")
        Expect-Rejection { Import-Release $archive '' $destination $false }
        Pack-Fixture
        Write-Text (Join-Path $fixture 'boot/wrapper.jar') 'tampered'
        Invoke-Tar @('-czf', $archive, '-C', (Split-Path -Parent $fixture), 'wrapper-1.0.0')
        Write-Text "$archive.sha256" ((Get-Digest $archive) + "  wrapper-1.0.0.tar.gz`n")
        Expect-Rejection { Import-Release $archive '' $destination $false }
        $launcher = [IO.File]::ReadAllText((Join-Path $fixture 'bin/service.sh'))
        Write-Text (Join-Path $fixture 'bin/service.sh') 'unrecognized'
        Pack-Fixture
        Expect-Rejection { Import-Release $archive '' $destination $false }
        Write-Text (Join-Path $fixture 'bin/service.sh') $launcher
        Pack-Fixture
        Expect-Rejection { Invoke-Updater @('import', $archive, '--destination', $destination, '--sandbox') }
        foreach ($path in @('../escape', '/absolute', 'root//file', 'root/./file', 'root/NUL.txt', 'root/file.', 'root/file:stream')) {
            Expect-Rejection { Assert-ReleasePath $path }
        }
        $sandbox = Join-Path $temporary 'target/sandbox'
        New-Directory (Join-Path $sandbox 'conf')
        Write-Text (Join-Path $sandbox 'conf/wrapper.conf') 'preserve configuration'
        Import-Release $archive '' $destination $true
        Import-Release $archive '' $destination $true
        Assert-Equal ([IO.File]::ReadAllText((Join-Path $sandbox 'conf/wrapper.conf'))) 'preserve configuration' 'sandbox configuration'
        Assert-Equal (Get-Digest (Join-Path $sandbox 'agent/boot/wrapper.jar')) (Get-Digest (Join-Path $fixture 'boot/wrapper.jar')) 'sandbox binaries'
        $checkout = Join-Path $temporary 'local checkout'
        New-Directory (Join-Path $checkout 'dist/boot')
        New-Directory (Join-Path $checkout 'resources')
        Write-Text (Join-Path $checkout 'Cargo.toml') "[package]`nversion = `"2.0.0`"`n[dependencies]`n"
        Write-Text (Join-Path $checkout 'dist/boot/wrapper.jar') 'local jar'
        Write-Text (Join-Path $checkout 'dist/boot/wrapper-windows-x86-64.exe') 'local binary'
        Copy-Item -LiteralPath (Join-Path $fixture 'bin/service.sh') -Destination (Join-Path $checkout 'resources/App.sh.in')
        Copy-Item -LiteralPath (Join-Path $fixture 'bin/service.bat') -Destination (Join-Path $checkout 'resources/AppCommand.bat.in')
        Update-Local $checkout $destination $true
        Test-Manifest $destination
        Assert-Equal ([IO.File]::ReadAllText((Join-Path $destination 'VERSION'))) "2.0.0-local`n" 'local version'
        Assert-Equal ([IO.File]::ReadAllText((Join-Path $sandbox 'boot/wrapper.jar'))) 'local jar' 'local sandbox refresh'
        Assert-Equal (Get-Digest (Join-Path $destination 'distribution/boot/wrapper-linux-arm-64')) (Get-Digest (Join-Path $fixture 'boot/wrapper-linux-arm-64')) 'unbuilt platform retention'
        Assert-Equal (Get-ReleaseName '<a href="wrapper-1.0.0.tar.gz">release</a><a HREF=''wrapper-1.0.0.tar.gz''>duplicate</a>') 'wrapper-1.0.0.tar.gz' 'release discovery'
        Expect-Rejection { Get-ReleaseName '<a href="wrapper-1.0.0.tar.gz">one</a><a href="wrapper-2.0.0.tar.gz">two</a>' }
        Expect-Rejection { Get-ReleaseName '<a href="../wrapper-1.0.0.tar.gz">invalid</a>' }
        Write-Host 'Wrapper import, local update, sandbox refresh, and release discovery tests passed'
    } finally { Remove-Tree $temporary ([IO.Path]::GetTempPath()) }
}

try {
    $arguments = @(for ($i = 0; $i -lt [int]$env:WRAPPER_UPDATE_COUNT; $i++) {
        [Environment]::GetEnvironmentVariable("WRAPPER_UPDATE_ARG_$i")
    })
    Invoke-Updater $arguments
} catch {
    [Console]::Error.WriteLine("Error: $($_.Exception.Message)")
    exit 1
}
