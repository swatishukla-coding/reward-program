package com.retailer.rewards.service;

import com.retailer.rewards.dto.CustomerRewardsResponse;
import com.retailer.rewards.dto.MonthlyRewardDto;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

    @Mock
    private TransactionStore transactionStore;

    @Mock
    private TransactionDataService transactionDataService;

    private RewardsService rewardsService;

    private static final LocalDate AS_OF = LocalDate.of(2026, 9, 8);

    @BeforeEach
    void setUp() {
        rewardsService = new RewardsService(transactionStore, transactionDataService, new RewardsCalculationService());
    }

    @Test
    void throwsWhenCustomerDoesNotExist() {
        when(transactionStore.findCustomerById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> rewardsService.getRewardsForCustomer("UNKNOWN", 3, AS_OF));
    }

    @Test
    void groupsPointsByMonthAndExcludesTransactionsOutsideWindow() {
        when(transactionStore.findCustomerById("C001"))
                .thenReturn(Optional.of(new Customer("C001", "Alice Johnson")));

        List<Transaction> transactions = Arrays.asList(
                // outside the 3-month window ending 2026-09-08 (window starts 2026-06-09)
                new Transaction("T0", "C001", LocalDate.of(2026, 5, 15), new BigDecimal("999.00")),
                // June: 120 -> 90 points
                new Transaction("T1", "C001", LocalDate.of(2026, 6, 20), new BigDecimal("120.00")),
                // July: 75 -> 25 points, 45 -> 0 points
                new Transaction("T2", "C001", LocalDate.of(2026, 7, 5), new BigDecimal("75.00")),
                new Transaction("T3", "C001", LocalDate.of(2026, 7, 10), new BigDecimal("45.00")),
                // August: 200 -> 250 points
                new Transaction("T4", "C001", LocalDate.of(2026, 8, 1), new BigDecimal("200.00"))
        );

        when(transactionDataService.fetchTransactionsForCustomer("C001"))
                .thenReturn(CompletableFuture.completedFuture(transactions));

        CustomerRewardsResponse response = rewardsService.getRewardsForCustomer("C001", 3, AS_OF);

        assertEquals("C001", response.getCustomerId());
        assertEquals(3, response.getMonthlyBreakdown().size()); // June, July, August
        assertEquals(90 + 25 + 250, response.getTotalPointsEarned());

        MonthlyRewardDto june = response.getMonthlyBreakdown().get(0);
        assertEquals("2026-06", june.getMonth());
        assertEquals(90, june.getPointsEarned());

        MonthlyRewardDto july = response.getMonthlyBreakdown().get(1);
        assertEquals("2026-07", july.getMonth());
        assertEquals(25, july.getPointsEarned());
    }

    @Test
    void customerWithNoTransactionsInWindowReturnsZeroPoints() {
        when(transactionStore.findCustomerById("C002"))
                .thenReturn(Optional.of(new Customer("C002", "Brian Smith")));
        when(transactionDataService.fetchTransactionsForCustomer("C002"))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        CustomerRewardsResponse response = rewardsService.getRewardsForCustomer("C002", 3, AS_OF);

        assertEquals(0, response.getTotalPointsEarned());
        assertEquals(0, response.getMonthlyBreakdown().size());
    }
}
