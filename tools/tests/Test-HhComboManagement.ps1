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
    'NO_CODDESC_TIEBREAKER' = $catalog.Contains('empatados.size() > 1') -and
        $catalog.Contains('COMBO_SELECTION_AMBIGUOUS')
    'COMBO_DESCTIPO_C_SUPPORTED' = $catalog.Contains("D.DESCTIPO IN ('R','M','C')")
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
    'ORDER_GENEALOGY_NOT_IN_BACKEND_TABLE' = $schema.Contains('T_PEDIDO_PROMO_STATE') -and
        -not $schema.Contains('add(db,"D_PEDIDOD","PRECIO_BASE') -and
        -not $sync.Contains('dbld.insert("T_PEDIDO_PROMO_STATE"')
    'STALE_SCHEMA_SYNC_GUARD' = $builder.Contains('esColumnaPromocionLocal(tn,n)') -and
        $builder.Contains('tabla.equalsIgnoreCase("D_FACTURAD")') -and
        $builder.Contains('tabla.equalsIgnoreCase("D_PEDIDOD")')
}

$failed = @($guards.GetEnumerator() | Where-Object { -not $_.Value } | ForEach-Object Key)
if ($failed.Count -gt 0) { throw ('HH_COMBO_GUARDS_FAILED ' + ($failed -join ',')) }

Write-Output "HH_COMBO_MANAGEMENT_OK cases=7 guards=$($guards.Count)"
