package com.retailer.rewards.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure calculation logic for the rewards formula:
 *   - 2 points for every dollar spent over $100
 *   - 1 point for every dollar spent between $50 and $100
 *   - 0 points for the first $50
 *
 * e.g. a $120 purchase = (20 * 2) + (50 * 1) = 90 points.
 *
 * Kept as its own service (no dependency on transactions, customers, or the
 * web layer) so the formula can be unit tested in isolation.
 */
@Service
public class RewardsCalculationService {

    private static final BigDecimal LOWER_THRESHOLD = BigDecimal.valueOf(50);
    private static final BigDecimal UPPER_THRESHOLD = BigDecimal.valueOf(100);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    public int calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal points = BigDecimal.ZERO;

        if (amount.compareTo(UPPER_THRESHOLD) > 0) {
            BigDecimal amountOverHundred = amount.subtract(UPPER_THRESHOLD);
            points = points.add(amountOverHundred.multiply(TWO));
            points = points.add(UPPER_THRESHOLD.subtract(LOWER_THRESHOLD)); // full $50-$100 tier = 50 pts
        } else if (amount.compareTo(LOWER_THRESHOLD) > 0) {
            points = points.add(amount.subtract(LOWER_THRESHOLD));
        }

        // Points are whole numbers; a purchase like $100.50 earns 1 point, not 1.5.
        return points.setScale(0, RoundingMode.DOWN).intValue();
    }
}
