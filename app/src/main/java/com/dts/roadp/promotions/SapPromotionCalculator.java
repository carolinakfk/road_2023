package com.dts.roadp.promotions;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * #EJC20260721 feat(hh-sap-calculator): calcula ajustes sobre el total extendido SAP.
 * Clase pura: no depende de Android, SQLite, UI ni SOAP.
 */
public final class SapPromotionCalculator {

    public static final int MONEY_SCALE = 2;
    public static final int UNIT_PRICE_SCALE = 6;
    public static final RoundingMode SAP_ROUNDING = RoundingMode.HALF_UP;

    private SapPromotionCalculator() {
    }

    public enum AdjustmentKind {
        NONE,
        FIXED,
        PERCENTAGE_MULTIPLE,
        PERCENTAGE_RANGE
    }

    public static final class Adjustment {
        public final AdjustmentKind kind;
        public final BigDecimal value;

        public Adjustment(AdjustmentKind kind, BigDecimal value) {
            this.kind = kind == null ? AdjustmentKind.NONE : kind;
            this.value = value == null ? BigDecimal.ZERO : value;
        }

        public static Adjustment none() {
            return new Adjustment(AdjustmentKind.NONE, BigDecimal.ZERO);
        }
    }

    public static final class Result {
        public final BigDecimal baseUnitPrice;
        public final BigDecimal billingBasis;
        public final BigDecimal extendedBaseTotal;
        public final BigDecimal discountTotal;
        public final BigDecimal surchargeTotal;
        public final BigDecimal authoritativeFinalTotal;
        public final BigDecimal derivedUnitPrice;

        private Result(BigDecimal baseUnitPrice,
                       BigDecimal billingBasis,
                       BigDecimal extendedBaseTotal,
                       BigDecimal discountTotal,
                       BigDecimal surchargeTotal,
                       BigDecimal authoritativeFinalTotal,
                       BigDecimal derivedUnitPrice) {
            this.baseUnitPrice = baseUnitPrice;
            this.billingBasis = billingBasis;
            this.extendedBaseTotal = extendedBaseTotal;
            this.discountTotal = discountTotal;
            this.surchargeTotal = surchargeTotal;
            this.authoritativeFinalTotal = authoritativeFinalTotal;
            this.derivedUnitPrice = derivedUnitPrice;
        }
    }

    public static Result calculate(BigDecimal baseUnitPrice,
                                   BigDecimal billingBasis,
                                   Adjustment discount,
                                   Adjustment surcharge) {
        BigDecimal safePrice = nonNegative(baseUnitPrice, "baseUnitPrice");
        BigDecimal safeBasis = positive(billingBasis, "billingBasis");
        Adjustment safeDiscount = discount == null ? Adjustment.none() : discount;
        Adjustment safeSurcharge = surcharge == null ? Adjustment.none() : surcharge;

        BigDecimal extendedBase = money(safePrice.multiply(safeBasis));
        BigDecimal discountTotal = calculateAdjustment(extendedBase, safeBasis, safeDiscount);
        BigDecimal surchargeTotal = calculateAdjustment(extendedBase, safeBasis, safeSurcharge);
        BigDecimal finalTotal = money(extendedBase.subtract(discountTotal).add(surchargeTotal));
        BigDecimal derivedPrice = finalTotal.divide(safeBasis, UNIT_PRICE_SCALE, SAP_ROUNDING);

        return new Result(safePrice, safeBasis, extendedBase, discountTotal,
                surchargeTotal, finalTotal, derivedPrice);
    }

    public static BigDecimal calculateAdjustment(BigDecimal extendedBase,
                                                 BigDecimal billingBasis,
                                                 Adjustment adjustment) {
        if (adjustment == null || adjustment.kind == AdjustmentKind.NONE) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, SAP_ROUNDING);
        }

        BigDecimal value = nonNegative(adjustment.value, "adjustment.value");
        switch (adjustment.kind) {
            case FIXED:
                return money(value.multiply(billingBasis));
            case PERCENTAGE_MULTIPLE:
                // SAP/BOF: ZK95/ZR95 M no redondea el ajuste intermedio.
                return extendedBase.multiply(value).divide(new BigDecimal("100"));
            case PERCENTAGE_RANGE:
                // SAP/BOF: ZK97/ZR97 R redondea el ajuste extendido a centavos.
                return money(extendedBase.multiply(value).divide(new BigDecimal("100")));
            default:
                return BigDecimal.ZERO.setScale(MONEY_SCALE, SAP_ROUNDING);
        }
    }

    public static BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, SAP_ROUNDING);
    }

    public static BigDecimal decimal(double value) {
        return new BigDecimal(Double.toString(value));
    }

    private static BigDecimal nonNegative(BigDecimal value, String name) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(name + " must be >= 0");
        }
        return value;
    }

    private static BigDecimal positive(BigDecimal value, String name) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
        return value;
    }
}
