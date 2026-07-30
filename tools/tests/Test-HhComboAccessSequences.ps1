$ErrorActionPreference = 'Stop'

$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$filterPath = Join-Path $repo 'app\src\main\java\com\dts\roadp\clsDescFiltro.java'
$catalogPath = Join-Path $repo 'app\src\main\java\models\Catalogo.java'
$filter = [IO.File]::ReadAllText($filterPath)
$catalog = [IO.File]::ReadAllText($catalogPath)

function Test-ComboScope {
    param(
        [int]$Ctipo,
        [string]$ConditionCustomer,
        [string]$ConditionTipologia = '',
        [string]$ConditionSucursal = '',
        [string]$CustomerCode = '0001002150',
        [string]$CustomerTipologia = 'Z0022405',
        [string]$CustomerSubtipologia = 'Z001110101',
        [string]$CustomerPriorizacion = 'AA',
        [string]$CustomerSucursal = '3900',
        [string]$CurrentRoute = 'P601-5',
        [bool]$AssignedToRoute = $true
    )

    switch ($Ctipo) {
        1  { return $ConditionCustomer -eq $CustomerCode }
        11 { return $ConditionCustomer -eq $CurrentRoute -and $AssignedToRoute }
        12 { return $ConditionCustomer -eq $CustomerTipologia }
        13 {
            return $ConditionCustomer -eq $CustomerPriorizacion -and
                $ConditionTipologia -eq $CustomerTipologia -and
                $ConditionSucursal -eq $CustomerSucursal
        }
        14 { return $ConditionCustomer -eq $CustomerSubtipologia }
        default { return $false }
    }
}

$cases = @(
    @{ Name='A908_cliente'; Result=(Test-ComboScope -Ctipo 1 -ConditionCustomer '0001002150'); Expected=$true },
    @{ Name='A908_otro_cliente'; Result=(Test-ComboScope -Ctipo 1 -ConditionCustomer '0001000001'); Expected=$false },
    @{ Name='A909_completo'; Result=(Test-ComboScope -Ctipo 13 -ConditionCustomer 'AA' -ConditionTipologia 'Z0022405' -ConditionSucursal '3900'); Expected=$true },
    @{ Name='A909_priorizacion_distinta'; Result=(Test-ComboScope -Ctipo 13 -ConditionCustomer 'BC' -ConditionTipologia 'Z0022405' -ConditionSucursal '3900'); Expected=$false },
    @{ Name='A909_tipologia_distinta'; Result=(Test-ComboScope -Ctipo 13 -ConditionCustomer 'AA' -ConditionTipologia 'Z0011101' -ConditionSucursal '3900'); Expected=$false },
    @{ Name='A909_sucursal_distinta'; Result=(Test-ComboScope -Ctipo 13 -ConditionCustomer 'AA' -ConditionTipologia 'Z0022405' -ConditionSucursal '4000'); Expected=$false },
    @{ Name='A910_tipologia'; Result=(Test-ComboScope -Ctipo 12 -ConditionCustomer 'Z0022405'); Expected=$true },
    @{ Name='A911_subtipologia'; Result=(Test-ComboScope -Ctipo 14 -ConditionCustomer 'Z001110101'); Expected=$true },
    @{ Name='A912_ruta_asignada'; Result=(Test-ComboScope -Ctipo 11 -ConditionCustomer 'P601-5'); Expected=$true },
    @{ Name='A912_otra_ruta'; Result=(Test-ComboScope -Ctipo 11 -ConditionCustomer 'P601-6'); Expected=$false },
    @{ Name='A912_sin_asignacion'; Result=(Test-ComboScope -Ctipo 11 -ConditionCustomer 'P601-5' -AssignedToRoute $false); Expected=$false }
)

foreach ($case in $cases) {
    if ($case.Result -ne $case.Expected) {
        throw "HH_COMBO_ACCESS_FAILED case=$($case.Name)"
    }
}

$guards = @{
    'A908' = $filter.Contains('((CTIPO=1)')
    'A909' = $filter.Contains('((CTIPO=13)') -and $filter.Contains('CPriorizacion') -and
        $filter.Contains("IFNULL(D.TIPOLOGIA,'')") -and $filter.Contains("IFNULL(D.SUCURSAL,'')")
    'A910' = $filter.Contains('((CTIPO=12)') -and $filter.Contains('CTipologia')
    'A911' = $filter.Contains('((CTIPO=14)') -and $filter.Contains('CSubTipologia')
    'A912_ROUTE_ONLY' = $filter.Contains('((CTIPO=11)') -and $filter.Contains('P_CLIRUTA CR') -and
        $filter.Contains('CR.RUTA=D.CLIENTE') -and -not $filter.Contains('CR.DIA')
    'DETAIL_EXCLUSION' = $catalog.Contains('P_DESCUENTO_COMBO_DET DD') -and
        $catalog.Contains('E.PRODUCTO=DD.PRODUCTO') -and $catalog.Contains('DD.CODDESC=D.CODDESC')
}

$failedGuards = @($guards.GetEnumerator() | Where-Object { -not $_.Value } | ForEach-Object Key)
if ($failedGuards.Count -gt 0) {
    throw "HH_COMBO_ACCESS_GUARD_FAILED: $($failedGuards -join ', ')"
}

Write-Output "HH_COMBO_ACCESS_OK cases=$($cases.Count) guards=$($guards.Count)"
