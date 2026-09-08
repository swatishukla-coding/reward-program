package com.retailer.rewards.controller;

import com.retailer.rewards.dto.CustomerRewardsResponse;
import com.retailer.rewards.service.RewardsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rewards")
@Validated
public class RewardsController {

    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    /**
     * Reward points for one customer, broken down by month.
     *
     * @param customerId the customer to look up
     * @param months     size of the trailing window, in months (default 3 per the take-home spec,
     *                   but exposed as a parameter so the API isn't hard-coded to a fixed period)
     * @param asOfDate   optional reference date the window is measured back from; defaults to today,
     *                   useful for reproducible testing against fixed data
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerRewardsResponse> getCustomerRewards(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "3") @Min(1) @Max(24) int months,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        LocalDate effectiveDate = asOfDate != null ? asOfDate : LocalDate.now();
        return ResponseEntity.ok(rewardsService.getRewardsForCustomer(customerId, months, effectiveDate));
    }

    /**
     * Reward points for every known customer, same window semantics as above.
     */
    @GetMapping("/customers")
    public ResponseEntity<List<CustomerRewardsResponse>> getAllCustomerRewards(
            @RequestParam(defaultValue = "3") @Min(1) @Max(24) int months,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        LocalDate effectiveDate = asOfDate != null ? asOfDate : LocalDate.now();
        return ResponseEntity.ok(rewardsService.getRewardsForAllCustomers(months, effectiveDate));
    }
}
