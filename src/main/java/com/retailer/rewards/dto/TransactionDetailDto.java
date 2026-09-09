package com.retailer.rewards.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDetailDto {

    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    private BigDecimal amount;
    private int pointsEarned;

    public TransactionDetailDto(String transactionId, LocalDate transactionDate, BigDecimal amount, int pointsEarned) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.pointsEarned = pointsEarned;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }
}
