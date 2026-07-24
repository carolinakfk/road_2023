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
$order = Read-Source 'app\src\main\java\com\dts\roadp\PedidoRes.java'
$return = Read-Source 'app\src\main\java\com\dts\roadp\DevolCli.java'
$combo = Read-Source 'app\src\main\java\models\Catalogo.java'
$schema = Read-Source 'app\src\main\java\com\dts\roadp\promotions\PromotionSchema.java'
$print = Read-Source 'app\src\main\java\com\dts\roadp\clsDocFactura.java'
$calculator = Read-Source 'app\src\main\java\com\dts\roadp\promotions\SapPromotionCalculator.java'
$all = $comws + $filter + $selector + $price + $transactionPrice + $sale + $invoice + $order + $return + $combo + $schema + $print + $calculator

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
    'F13_SINGLE_APPLY' = $invoice.Contains('ResolverCombosEnTVenta') -and
        -not $invoice.Contains('AplicarAjusteComboEnTVenta(detDescuento') -and
        -not $invoice.Contains('AplicarAjusteComboEnTVenta(detRecargo')
    'F14_BONUS_EXCLUDED' = $combo.Contains('PROMO_COMBO_BONUS_EXCLUDED') -and
        $combo.Contains('bonificadosIncluidos=0') -and
        -not $combo.Contains('cantidadAcumulada += cantidadBonificadaCompatible')
    'F15_BASE_LOCAL_ONLY' = $schema.Contains('T_VENTA","PRECIO_BASE REAL') -and
        $invoice.Contains('PROMO_LINE_PERSISTED') -and
        -not $schema.Contains('D_FACTURAD","PRECIO_BASE REAL') -and
        -not $invoice.Contains('ins.add("CODDESC_APLICADO"')
    'F16_NCND_REMAINS_BOF' = -not $all.Contains('hh-ncnd-precio')
    'F17_SAME_BASE_CONCURRENCY' = $calculator.Contains('calculateAdjustment(extendedBase, safeBasis, safeDiscount)') -and $calculator.Contains('calculateAdjustment(extendedBase, safeBasis, safeSurcharge)')
    'F18_EFFECTIVE_PRICE_PRINT' = $print.Contains('formatEffectivePrice(item.prec,8)') -and $print.Contains('scale=6;scale>=2')
    'TC0015_RANGE_BASIS_BY_UM' = $selector.Contains('#EJC20260724 fix(TC0015-hh-escala-um)') -and
        $selector.Contains('String baseEvaluacionSql = "CASE WHEN UMVENTA=') -and
        $selector.Contains('" WHEN UMVENTA=') -and
        $selector.Contains('baseEvaluacionSql+">=RANGOINI') -and
        $selector.Contains('baseEvaluacionSql+"<=RANGOFIN')
    'COMBO_TIE_FALLBACK' = $combo.Contains('COMBO_SELECTION_AMBIGUOUS') -and
        $combo.Contains('accion=individual') -and $combo.Contains('empatados.size() > 1')
    'COMBO_BOTH_SIDES_ONCE' = $invoice.Contains('ResolverCombosEnTVenta') -and
        $combo.Contains('aplicarResolucionConjunta')
    'COMBO_LIVE_REEVALUATION' = $sale.Contains('LINE_ADDED') -and
        $sale.Contains('LINE_EDITED') -and $sale.Contains('LINE_DELETED') -and
        $sale.Contains('WEIGHT_OR_BARCODE_EDITED')
    'COMBO_ORDER_SUMMARY' = $order.Contains('ResolverCombosEnTVenta')
    'COMBO_CUSTOMER_RETURN' = $return.Contains('ResolverCombosEnDevolucion') -and
        $return.Contains('BEFORE_SAVE')
}

$failed = @($checks.GetEnumerator() | Where-Object { -not $_.Value } | ForEach-Object Key)
if ($failed.Count -gt 0) { throw ('HH_PROMOTION_INTEGRATION_FAILED ' + ($failed -join ',')) }

Write-Output "HH_PROMOTION_INTEGRATION_OK checks=$($checks.Count) bof_only=F16"
