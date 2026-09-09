package com.retailer.rewards.dto;

import java.util.List;

public class MonthlyRewardDto {

    /** e.g. "2026-07" */
    private String month;
    private int pointsEarned;
    private List<TransactionDetailDto> transactions;

    public MonthlyRewardDto(String month, int pointsEarned, List<TransactionDetailDto> transactions) {
        this.month = month;
        this.pointsEarned = pointsEarned;
        this.transactions = transactions;
    }

    public String getMonth() {
        return month;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public List<TransactionDetailDto> getTransactions() {
        return transactions;
    }
}
