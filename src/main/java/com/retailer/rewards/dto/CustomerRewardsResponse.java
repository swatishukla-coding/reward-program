package com.retailer.rewards.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class CustomerRewardsResponse {

    private String customerId;
    private String customerName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodEnd;

    private int totalPointsEarned;
    private List<MonthlyRewardDto> monthlyBreakdown;

    public CustomerRewardsResponse(String customerId, String customerName, LocalDate periodStart,
                                    LocalDate periodEnd, int totalPointsEarned,
                                    List<MonthlyRewardDto> monthlyBreakdown) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalPointsEarned = totalPointsEarned;
        this.monthlyBreakdown = monthlyBreakdown;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public int getTotalPointsEarned() {
        return totalPointsEarned;
    }

    public List<MonthlyRewardDto> getMonthlyBreakdown() {
        return monthlyBreakdown;
    }
}
