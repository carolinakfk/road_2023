$ErrorActionPreference = 'Stop'
$repo = Resolve-Path (Join-Path $PSScriptRoot '..\..')
$out = Join-Path $env:TEMP 'road-hh-sap-promotion-tests'
if (Test-Path $out) { Remove-Item -LiteralPath $out -Recurse -Force }
New-Item -ItemType Directory -Path $out | Out-Null

$calculator = Join-Path $repo 'app\src\main\java\com\dts\roadp\promotions\SapPromotionCalculator.java'
$test = Join-Path $repo 'tools\tests\SapPromotionCalculatorContractTest.java'
$javac = (Get-Command javac -ErrorAction SilentlyContinue).Source
$java = (Get-Command java -ErrorAction SilentlyContinue).Source
if (-not $javac) { $javac = 'C:\Program Files\Android\Android Studio\jbr\bin\javac.exe' }
if (-not $java) { $java = 'C:\Program Files\Android\Android Studio\jbr\bin\java.exe' }
& $javac -encoding UTF-8 -d $out $calculator $test
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }
& $java -cp $out SapPromotionCalculatorContractTest
if ($LASTEXITCODE -ne 0) { throw 'contract tests failed' }
