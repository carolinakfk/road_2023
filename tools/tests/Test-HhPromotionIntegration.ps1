$ErrorActionPreference = 'Stop'
$repo = Resolve-Path (Join-Path $PSScriptRoot '..\..')

function Read-Source([string]$relativePath) {
    return Get-Content -Raw (Join-Path $repo $relativePath)
}

$comws = Read-Source 'app\src\main\java\com\dts\roadp\ComWS.java'
$filter = Read-Source 'app\src\main\java\com\dts\roadp\clsDescFiltro.java'
$selector = Read-Source 'app\src\main\java\com\dts\roadp\clsDescuento.java'
$price = Read-Source 'app\src\main\java\com\dts\roadp\Precio.java'
$transactionPrice = Read-Source 'app\src\main\java\com\dts\roadp\PrecioTran.java'
$sale = Read-Source 'app\src\main\java\com\dts\roadp\Venta.java'
$invoice = Read-Source 'app\src\main\java\com\dts\roadp\FacturaRes.java'
$combo = Read-Source 'app\src\main\java\models\Catalogo.java'
$schema = Read-Source 'app\src\main\java\com\dts\roadp\promotions\PromotionSchema.java'
$print = Read-Source 'app\src\main\java\com\dts\roadp\clsDocFactura.java'
$calculator = Read-Source 'app\src\main\java\com\dts\roadp\promotions\SapPromotionCalculator.java'
$all = $comws + $filter + $selector + $price + $transactionPrice + $sale + $invoice + $combo + $schema + $print + $calculator

$checks = [ordered]@{
    'F01_FILTER_ALIAS' = $filter.Contains('FROM P_DESCUENTO D WHERE ((CTIPO=0)')
    'F02_SYNC_EXCLUSIONS' = $comws.Contains('nombretabla="P_CLIENTE_PROD_EXCLUIDOS"')
    'F03_FINE_TRACE' = $all.Contains('PROMO_CALCULATION') -and $all.Contains('PROMO_COMBO_APPLIED')
    'F04_CONDITION_METADATA' = $selector.Contains('TmpDescuento.codDesc') -and $selector.Contains('TmpDescuento.descTipo')
    'F05_BIGDECIMAL_ENGINE' = $price.Contains('SapPromotionCalculator.calculate') -and $transactionPrice.Contains('SapPromotionCalculator.calculate')
    'F06_TOTAL_WEIGHT' = $price.Contains('ppeso > 0 ? ppeso : cant') -and $transactionPrice.Contains('baseFacturacion=ppeso>0?ppeso:cant')
    'F07_M_BEFORE_R' = $selector.Contains("CASE WHEN DESCTIPO='M' THEN 0 ELSE 1 END") -and $selector.Contains('PRIORIDAD_DESCUENTO ASC')
    'F08_AUTHORITATIVE_TOTAL' = $sale.Contains('prodtot=mu.round(prc.tot,2)')
    'F09_SESSION_BASE' = $combo.Contains('preciosBaseSesion')
    'F10_NO_REEXTEND' = $invoice.Contains('SELECT SUM(DESMON),') -and $invoice.Contains('SUM(RECARGOMONTO)')
    'F11_COMBO_PTIPO6' = $combo.Contains("D.PTIPO = 6 AND D.DESCTIPO IN ('R','M')")
    'F12_REQUIRED_AND_UM' = $combo.Contains('itemCombo.obligatorio') -and $combo.Contains('umCompatible')
    'F13_SINGLE_APPLY' = $invoice.Contains('detDescuento, beDescuento, null') -and $invoice.Contains('detRecargo, null, beRecargo')
    'F14_BONUS_INCLUDED' = $combo.Contains('CargarBonificacionesParaCombo') -and $combo.Contains('cantidadBonificadaCompatible')
    'F15_BASE_PERSISTED' = $schema.Contains('PRECIO_BASE REAL') -and $invoice.Contains('CODDESC_APLICADO')
    'F16_NCND_REMAINS_BOF' = -not $all.Contains('hh-ncnd-precio')
    'F17_SAME_BASE_CONCURRENCY' = $calculator.Contains('calculateAdjustment(extendedBase, safeBasis, safeDiscount)') -and $calculator.Contains('calculateAdjustment(extendedBase, safeBasis, safeSurcharge)')
    'F18_EFFECTIVE_PRICE_PRINT' = $print.Contains('formatEffectivePrice(item.prec,8)') -and $print.Contains('scale=6;scale>=2')
    'TC0015_RANGE_BASIS_BY_UM' = $selector.Contains('#EJC20260724 fix(TC0015-hh-escala-um)') -and
        $selector.Contains('String baseEvaluacionSql = "CASE WHEN UMVENTA=') -and
        $selector.Contains('" WHEN UMVENTA=') -and
        $selector.Contains('baseEvaluacionSql+">=RANGOINI') -and
        $selector.Contains('baseEvaluacionSql+"<=RANGOFIN')
}

$failed = @($checks.GetEnumerator() | Where-Object { -not $_.Value } | ForEach-Object Key)
if ($failed.Count -gt 0) { throw ('HH_PROMOTION_INTEGRATION_FAILED ' + ($failed -join ',')) }

Write-Output "HH_PROMOTION_INTEGRATION_OK checks=$($checks.Count) bof_only=F16"
