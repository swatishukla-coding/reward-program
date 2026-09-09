package com.retailer.rewards.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RewardsCalculationServiceTest {

    private final RewardsCalculationService service = new RewardsCalculationService();

    @Test
    void zeroOrNegativeAmountEarnsNoPoints() {
        assertEquals(0, service.calculatePoints(BigDecimal.ZERO));
        assertEquals(0, service.calculatePoints(BigDecimal.valueOf(-25)));
        assertEquals(0, service.calculatePoints(null));
    }

    @Test
    void amountAtOrBelowFiftyEarnsNoPoints() {
        assertEquals(0, service.calculatePoints(BigDecimal.valueOf(10)));
        assertEquals(0, service.calculatePoints(BigDecimal.valueOf(50)));
    }

    @Test
    void amountBetweenFiftyAndHundredEarnsOnePointPerDollarOverFifty() {
        // $75 -> (75 - 50) * 1 = 25
        assertEquals(25, service.calculatePoints(BigDecimal.valueOf(75)));
    }

    @Test
    void amountExactlyAtHundredEarnsFiftyPoints() {
        assertEquals(50, service.calculatePoints(BigDecimal.valueOf(100)));
    }

    @Test
    void classicExampleFromSpec_120DollarPurchase_earns90Points() {
        // spec: a $120 purchase = 2x$20 + 1x$50 = 90 points
        assertEquals(90, service.calculatePoints(BigDecimal.valueOf(120)));
    }

    @Test
    void largePurchaseScalesLinearlyAboveHundred() {
        // $500 -> (400 * 2) + 50 = 850
        assertEquals(850, service.calculatePoints(BigDecimal.valueOf(500)));
    }

    @Test
    void fractionalDollarsRoundDownToWholePoints() {
        // $100.99 -> over-hundred portion 0.99 * 2 = 1.98, + 50 = 51.98 -> floor 51
        assertEquals(51, service.calculatePoints(new BigDecimal("100.99")));
        // $99.99 -> 49.99 -> floor 49
        assertEquals(49, service.calculatePoints(new BigDecimal("99.99")));
    }

    @ParameterizedTest(name = "${'$'}{0} earns {1} points")
    @CsvSource({
            "0, 0",
            "50, 0",
            "50.01, 0",
            "60, 10",
            "100, 50",
            "100.01, 50",
            "120, 90",
            "150.25, 150",
            "250, 350",
            "500, 850"
    })
    void parameterizedBoundaryChecks(BigDecimal amount, int expectedPoints) {
        assertEquals(expectedPoints, service.calculatePoints(amount));
    }
}
