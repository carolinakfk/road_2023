$ErrorActionPreference = 'Stop'
$repo = Resolve-Path (Join-Path $PSScriptRoot '..\..')

function Assert-Equal([string]$name, $actual, $expected) {
    if ($actual -ne $expected) {
        throw "HH_COMBO_CASE_FAILED $name expected=$expected actual=$actual"
    }
}

function Resolve-Combo([object[]]$candidates) {
    $complete = @($candidates | Where-Object Complete |
        Sort-Object PriorityDiscount, Priority)
    if ($complete.Count -eq 0) { return 'INDIVIDUAL' }
    $best = $complete[0]
    $ties = @($complete | Where-Object {
        $_.PriorityDiscount -eq $best.PriorityDiscount -and
        $_.Priority -eq $best.Priority
    })
    if ($ties.Count -gt 1) { return 'INDIVIDUAL_AMBIGUOUS' }
    return "COMBO_$($best.Code)"
}

Assert-Equal 'IncompleteUsesIndividual' (Resolve-Combo @(
    [pscustomobject]@{Code=304;PriorityDiscount=1;Priority=1;Complete=$false}
)) 'INDIVIDUAL'

Assert-Equal 'UniqueCompleteReplacesIndividual' (Resolve-Combo @(
    [pscustomobject]@{Code=304;PriorityDiscount=1;Priority=1;Complete=$true}
)) 'COMBO_304'

Assert-Equal 'SameBestPriorityRejectsAllCombos' (Resolve-Combo @(
    [pscustomobject]@{Code=304;PriorityDiscount=1;Priority=1;Complete=$true},
    [pscustomobject]@{Code=999;PriorityDiscount=1;Priority=1;Complete=$true}
)) 'INDIVIDUAL_AMBIGUOUS'

$chargedQuantity = 1
$bonusQuantity = 1
$required = 2
Assert-Equal 'BonusDoesNotCompleteCombo' ($chargedQuantity -ge $required) $false
Assert-Equal 'RepeatedChargedLinesCompleteCombo' ((1 + 1) -ge $required) $true
Assert-Equal 'DeletingRequirementRestoresIndividual' (Resolve-Combo @(
    [pscustomobject]@{Code=304;PriorityDiscount=1;Priority=1;Complete=$false}
)) 'INDIVIDUAL'
Assert-Equal 'DiscountAndSurchargeSameBase' (100 - 10 + 5) 95

function Resolve-CompatibleCombos([object[]]$candidates) {
    $complete = @($candidates | Where-Object Complete)
    $conflicts = @{}
    for ($i=0; $i -lt $complete.Count; $i++) {
        for ($j=$i+1; $j -lt $complete.Count; $j++) {
            $shared = @($complete[$i].Products | Where-Object { $complete[$j].Products -contains $_ })
            if ($shared.Count -gt 0) {
                $conflicts[$complete[$i].Code] = $true
                $conflicts[$complete[$j].Code] = $true
            }
        }
    }
    return @($complete | Where-Object { -not $conflicts.ContainsKey($_.Code) } |
        ForEach-Object Code)
}

$disjoint = @(Resolve-CompatibleCombos @(
    [pscustomobject]@{Code=1;Complete=$true;Products=@('A','B')},
    [pscustomobject]@{Code=2;Complete=$true;Products=@('C','D')}
))
Assert-Equal 'DisjointCombosCount' $disjoint.Count 2
Assert-Equal 'DisjointComboOneApplied' ($disjoint -contains 1) $true
Assert-Equal 'DisjointComboTwoApplied' ($disjoint -contains 2) $true

$overlap = @(Resolve-CompatibleCombos @(
    [pscustomobject]@{Code=1;Complete=$true;Products=@('A','B')},
    [pscustomobject]@{Code=2;Complete=$true;Products=@('B','C')},
    [pscustomobject]@{Code=3;Complete=$true;Products=@('D','E')}
))
Assert-Equal 'OverlappingCombosRejected' ($overlap -contains 1 -or $overlap -contains 2) $false
Assert-Equal 'IndependentComboSurvivesOverlap' ($overlap -contains 3) $true

function Normalize-PromotionDate([long]$date) {
    $value = [string]$date
    if ($value.Length -eq 14) { return $date }
    if ($value.Length -ne 12) { throw 'INVALID_PROMOTION_DATE' }
    return [long]("20" + $value)
}

Assert-Equal 'LegacyPFechaGetsFullYear' (Normalize-PromotionDate 260724000000) 20260724000000
Assert-Equal 'FullPromotionDateIsPreserved' (Normalize-PromotionDate 20260724000000) 20260724000000

$catalog = Get-Content -Raw (Join-Path $repo 'app\src\main\java\models\Catalogo.java')
$sale = Get-Content -Raw (Join-Path $repo 'app\src\main\java\com\dts\roadp\Venta.java')
$invoice = Get-Content -Raw (Join-Path $repo 'app\src\main\java\com\dts\roadp\FacturaRes.java')
$order = Get-Content -Raw (Join-Path $repo 'app\src\main\java\com\dts\roadp\PedidoRes.java')
$return = Get-Content -Raw (Join-Path $repo 'app\src\main\java\com\dts\roadp\DevolCli.java')
$database = Get-Content -Raw (Join-Path $repo 'app\src\main\java\com\dts\roadp\BaseDatosScript.java')
$schema = Get-Content -Raw (Join-Path $repo 'app\src\main\java\com\dts\roadp\promotions\PromotionSchema.java')
$sync = Get-Content -Raw (Join-Path $repo 'app\src\main\java\com\dts\roadp\ComWS.java')
$builder = Get-Content -Raw (Join-Path $repo 'app\src\main\java\com\dts\roadp\clsDataBuilder.java')

$factStart = $database.IndexOf('CREATE TABLE [D_FACTURAD]')
$factEnd = $database.IndexOf('CREATE TABLE [D_FACTURAP]', $factStart)
$factSchema = $database.Substring($factStart, $factEnd - $factStart)
$saleStart = $database.IndexOf('CREATE TABLE [T_VENTA]')
$saleEnd = $database.IndexOf('CREATE INDEX T_VENTA_idx1', $saleStart)
$saleSchema = $database.Substring($saleStart, $saleEnd - $saleStart)

$guards = [ordered]@{
    'NO_CODDESC_TIEBREAKER' = $catalog.Contains('COMBO_SELECTION_OVERLAP') -and
        $catalog.Contains('accion=individual_solo_en_combos_traslapados') -and
        -not $catalog.Contains('result.seleccion = mejor')
    'COMBO_DESCTIPO_C_SUPPORTED' = $catalog.Contains("D.DESCTIPO IN ('R','M','C')")
    'COMBO_DATE_NORMALIZED_CENTRALLY' = $catalog.Contains('normalizarFechaVigencia(fechaDocumento)') -and
        $catalog.Contains('PROMO_DATE_NORMALIZED') -and $catalog.Contains('PROMO_DATE_INVALID')
    'BONUS_EXCLUDED' = $catalog.Contains('bonificadosIncluidos=0') -and
        -not $catalog.Contains('cantidadAcumulada += cantidadBonificadaCompatible')
    'LIVE_EVENTS' = $sale.Contains('LINE_ADDED') -and $sale.Contains('LINE_EDITED') -and
        $sale.Contains('LINE_DELETED')
    'FACTURA_SUMMARY' = $invoice.Contains('ResolverCombosEnTVenta')
    'PEDIDO_SUMMARY' = $order.Contains('ResolverCombosEnTVenta')
    'RETURN_BEFORE_SAVE' = $return.Contains('ResolverCombosEnDevolucion') -and
        $return.Contains('BEFORE_SAVE')
    'LOCAL_FALLBACK_ONLY_IN_TVENTA' = $saleSchema.Contains('INDIVIDUAL_SNAPSHOT') -and
        $saleSchema.Contains('INDIVIDUAL_CODRECARGO') -and
        -not $factSchema.Contains('INDIVIDUAL_')
    'MODIFIED_ORDER_REBUILDS_INDIVIDUAL_FALLBACK' =
        $catalog.Contains('INDIVIDUAL_FALLBACK_REBUILT') -and
        $catalog.Contains('PEDIDO_CARGADO_CON_COMBO') -and
        $catalog.Contains('esAjusteCombo')
    'MODIFIED_ORDER_PRESERVES_PERSISTED_PROMOTION_ON_LOAD' =
        $sale.Contains('PROMO_MODIFIED_ORDER_PRESERVED') -and
        -not $sale.Contains('reevaluarCombosDocumento("MODIFIED_ORDER_LOADED")')
	'MODIFIED_ORDER_RESTORES_COMMERCIAL_UM' =
		$sale.Contains('PROMO_MODIFIED_ORDER_LINE_REBUILT') -and
		$sale.Contains('?app.umStock(productoPedido):app.umStockPV(productoPedido)') -and
		$sale.Contains('ins.add("UMSTOCK",umStockPedido)') -and
		-not $sale.Contains('ins.add("UMSTOCK",dt.getString(2))')
    'MULTIPLE_DISJOINT_COMBOS' = $catalog.Contains('List<ComboEvaluation> selecciones') -and
        $catalog.Contains('compartenProductos') -and
        $catalog.Contains('COMBO_SELECTION_OVERLAP')
    'ORDER_GENEALOGY_NOT_IN_BACKEND_TABLE' = $schema.Contains('T_PEDIDO_PROMO_STATE') -and
        -not $schema.Contains('add(db,"D_PEDIDOD","PRECIO_BASE') -and
        -not $sync.Contains('dbld.insert("T_PEDIDO_PROMO_STATE"')
    'STALE_SCHEMA_SYNC_GUARD' = $builder.Contains('esColumnaPromocionLocal(tn,n)') -and
        $builder.Contains('tabla.equalsIgnoreCase("D_FACTURAD")') -and
        $builder.Contains('tabla.equalsIgnoreCase("D_PEDIDOD")')
}

$failed = @($guards.GetEnumerator() | Where-Object { -not $_.Value } | ForEach-Object Key)
if ($failed.Count -gt 0) { throw ('HH_COMBO_GUARDS_FAILED ' + ($failed -join ',')) }

Write-Output "HH_COMBO_MANAGEMENT_OK cases=13 guards=$($guards.Count)"
