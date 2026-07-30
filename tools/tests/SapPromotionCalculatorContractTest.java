import com.dts.roadp.promotions.SapPromotionCalculator;

import java.math.BigDecimal;

/** Ejecutable con javac/java, sin Android ni JUnit. */
public final class SapPromotionCalculatorContractTest {
    private static int cases;

    public static void main(String[] args) {
        if (args.length == 1 && "customer-return".equals(args[0])) {
            customerReturnPreservesRangeDiscountTotal();
            System.out.println("HH_RETURN_PROMOTION_TEST_OK cases=" + cases);
            return;
        }
        percentageMultipleDoesNotRoundIntermediate();
        percentageRangeRoundsAdjustment();
        fixedAdjustmentRoundsAfterExtending();
        roadWeightIsAlreadyTotal();
        customerReturnPreservesRangeDiscountTotal();
        retryIsIdempotent();
        System.out.println("HH_SAP_PROMOTION_TESTS_OK cases=" + cases);
    }

    private static void percentageMultipleDoesNotRoundIntermediate() {
        SapPromotionCalculator.Result r = calculate("5.84", "27.621",
                SapPromotionCalculator.AdjustmentKind.PERCENTAGE_MULTIPLE, "1",
                SapPromotionCalculator.AdjustmentKind.NONE, "0");
        eq("161.31", r.extendedBaseTotal);
        eq("1.6131", r.discountTotal);
        eq("159.70", r.authoritativeFinalTotal);
        eq("5.781833", r.derivedUnitPrice);
        cases++;
    }

    private static void percentageRangeRoundsAdjustment() {
        SapPromotionCalculator.Result r = calculate("1.30", "5",
                SapPromotionCalculator.AdjustmentKind.PERCENTAGE_RANGE, "1",
                SapPromotionCalculator.AdjustmentKind.NONE, "0");
        eq("6.50", r.extendedBaseTotal);
        eq("0.07", r.discountTotal);
        eq("6.43", r.authoritativeFinalTotal);
        eq("1.286000", r.derivedUnitPrice);
        cases++;
    }

    private static void fixedAdjustmentRoundsAfterExtending() {
        SapPromotionCalculator.Result r = calculate("5.84", "9.32",
                SapPromotionCalculator.AdjustmentKind.FIXED, "0.07",
                SapPromotionCalculator.AdjustmentKind.NONE, "0");
        eq("0.65", r.discountTotal);
        cases++;
    }

    private static void roadWeightIsAlreadyTotal() {
        SapPromotionCalculator.Result r = calculate("6.00", "9.450",
                SapPromotionCalculator.AdjustmentKind.NONE, "0",
                SapPromotionCalculator.AdjustmentKind.NONE, "0");
        eq("56.70", r.authoritativeFinalTotal);
        cases++;
    }

    private static void customerReturnPreservesRangeDiscountTotal() {
        SapPromotionCalculator.Result r = calculate("2.47", "35.478",
                SapPromotionCalculator.AdjustmentKind.PERCENTAGE_RANGE, "1",
                SapPromotionCalculator.AdjustmentKind.NONE, "0");
        eq("87.63", r.extendedBaseTotal);
        eq("0.88", r.discountTotal);
        eq("86.75", r.authoritativeFinalTotal);
        eq("2.445177", r.derivedUnitPrice);
        cases++;
    }

    private static void retryIsIdempotent() {
        SapPromotionCalculator.Result a = calculate("5.84", "9.207",
                SapPromotionCalculator.AdjustmentKind.PERCENTAGE_MULTIPLE, "1",
                SapPromotionCalculator.AdjustmentKind.NONE, "0");
        SapPromotionCalculator.Result b = calculate("5.84", "9.207",
                SapPromotionCalculator.AdjustmentKind.PERCENTAGE_MULTIPLE, "1",
                SapPromotionCalculator.AdjustmentKind.NONE, "0");
        eq(a.authoritativeFinalTotal.toPlainString(), b.authoritativeFinalTotal);
        cases++;
    }

    private static SapPromotionCalculator.Result calculate(String price, String basis,
                                                            SapPromotionCalculator.AdjustmentKind dk, String dv,
                                                            SapPromotionCalculator.AdjustmentKind sk, String sv) {
        return SapPromotionCalculator.calculate(new BigDecimal(price), new BigDecimal(basis),
                new SapPromotionCalculator.Adjustment(dk, new BigDecimal(dv)),
                new SapPromotionCalculator.Adjustment(sk, new BigDecimal(sv)));
    }

    private static void eq(String expected, BigDecimal actual) {
        if (new BigDecimal(expected).compareTo(actual) != 0) {
            throw new AssertionError("expected=" + expected + " actual=" + actual.toPlainString());
        }
    }
}
