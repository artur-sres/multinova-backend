param([switch]$StatementTests)
$ErrorActionPreference = 'Stop'
$testRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$testData = Join-Path $testRoot 'target/history-test-pg'
$testBin = 'C:/Program Files/PostgreSQL/18/bin'
$testPort = '55439'
$testDatabase = 'history_test_' + (Get-Date -Format 'yyyyMMddHHmmss')
$testProcess = Start-Process -FilePath "$testBin/postgres.exe" -ArgumentList @('-D', $testData, '-h', '127.0.0.1', '-p', $testPort) -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $testRoot 'target/history-pg.out') -RedirectStandardError (Join-Path $testRoot 'target/history-pg.err')
try {
    for ($attempt = 0; $attempt -lt 40; $attempt++) {
        & "$testBin/pg_isready.exe" -h 127.0.0.1 -p $testPort -U postgres -q
        if ($LASTEXITCODE -eq 0) { break }
        if ($testProcess.HasExited) { throw 'Temporary PostgreSQL exited before startup' }
        Start-Sleep -Milliseconds 250
    }
    if ($testProcess.HasExited) { throw 'Temporary PostgreSQL did not start' }
    $actualData = & "$testBin/psql.exe" -X -h 127.0.0.1 -p $testPort -U postgres -d postgres -Atc 'SHOW data_directory'
    if ($LASTEXITCODE -ne 0 -or [System.IO.Path]::GetFullPath($actualData.Trim()) -ne [System.IO.Path]::GetFullPath($testData)) { throw 'Refusing to access a different PostgreSQL instance' }
    & "$testBin/psql.exe" -X -h 127.0.0.1 -p $testPort -U postgres -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE $testDatabase"
    if ($LASTEXITCODE -ne 0) { throw 'Could not create temporary test database' }
    $testMigrations = Get-ChildItem (Join-Path $testRoot 'src/main/resources/db/migration') -Filter '*.sql' | Sort-Object { [int]($_.Name -replace '^V(\d+)__.*$', '$1') }
    foreach ($migration in $testMigrations) {
        if ($migration.Name.StartsWith('V23__')) {
            & "$testBin/psql.exe" -X -h 127.0.0.1 -p $testPort -U postgres -d $testDatabase -v ON_ERROR_STOP=1 -f (Join-Path $PSScriptRoot 'history-before.sql')
            if ($LASTEXITCODE -ne 0) { throw 'Could not seed history fixtures' }
        }
        & "$testBin/psql.exe" -X -h 127.0.0.1 -p $testPort -U postgres -d $testDatabase -v ON_ERROR_STOP=1 -f $migration.FullName
        if ($LASTEXITCODE -ne 0) { throw "Migration failed: $($migration.Name)" }
    }
    & "$testBin/psql.exe" -X -h 127.0.0.1 -p $testPort -U postgres -d $testDatabase -v ON_ERROR_STOP=1 -f (Join-Path $PSScriptRoot 'history-check.sql')
    if ($LASTEXITCODE -ne 0) { throw 'SQL checks failed' }
    if ($StatementTests) {
        $previousTestUrl = $env:MULTINOVA_STATEMENT_TEST_URL
        try {
            $env:MULTINOVA_STATEMENT_TEST_URL = "jdbc:postgresql://127.0.0.1:$testPort/$testDatabase"
            & mvn -f (Join-Path $testRoot 'pom.xml') '-Dtest=CustomerStatementIntegrationTests' test
            if ($LASTEXITCODE -ne 0) { throw 'Statement integration checks failed' }
        } finally { $env:MULTINOVA_STATEMENT_TEST_URL = $previousTestUrl }
    }
} finally {
    if (!$testProcess.HasExited) { & "$testBin/pg_ctl.exe" -D $testData stop -m fast -w }
}
