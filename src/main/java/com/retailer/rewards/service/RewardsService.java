package com.retailer.rewards.service;

import com.retailer.rewards.dto.CustomerRewardsResponse;
import com.retailer.rewards.dto.MonthlyRewardDto;
import com.retailer.rewards.dto.TransactionDetailDto;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.TransactionFetchException;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class RewardsService {

    private static final Logger log = LoggerFactory.getLogger(RewardsService.class);
    private static final long FETCH_TIMEOUT_SECONDS = 5;

    private final TransactionStore transactionStore;
    private final TransactionDataService transactionDataService;
    private final RewardsCalculationService calculationService;

    public RewardsService(TransactionStore transactionStore,
                           TransactionDataService transactionDataService,
                           RewardsCalculationService calculationService) {
        this.transactionStore = transactionStore;
        this.transactionDataService = transactionDataService;
        this.calculationService = calculationService;
    }

    /**
     * Calculates reward points earned by a customer, broken down by month,
     * for the {@code months} immediately preceding {@code asOfDate}.
     */
    public CustomerRewardsResponse getRewardsForCustomer(String customerId, int months, LocalDate asOfDate) {
        Customer customer = transactionStore.findCustomerById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        LocalDate periodStart = asOfDate.minusMonths(months).plusDays(1);
        List<Transaction> transactions = fetchTransactions(customerId);

        List<Transaction> inWindow = transactions.stream()
                .filter(t -> !t.getTransactionDate().isBefore(periodStart) && !t.getTransactionDate().isAfter(asOfDate))
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .collect(Collectors.toList());

        Map<YearMonth, List<Transaction>> byMonth = inWindow.stream()
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getTransactionDate()),
                        TreeMap::new,
                        Collectors.toList()));

        List<MonthlyRewardDto> monthlyBreakdown = new ArrayList<>();
        int totalPoints = 0;

        for (Map.Entry<YearMonth, List<Transaction>> entry : byMonth.entrySet()) {
            List<TransactionDetailDto> details = new ArrayList<>();
            int monthPoints = 0;

            for (Transaction t : entry.getValue()) {
                int points = calculationService.calculatePoints(t.getAmount());
                monthPoints += points;
                details.add(new TransactionDetailDto(t.getTransactionId(), t.getTransactionDate(), t.getAmount(), points));
            }

            monthlyBreakdown.add(new MonthlyRewardDto(entry.getKey().toString(), monthPoints, details));
            totalPoints += monthPoints;
        }

        return new CustomerRewardsResponse(
                customer.getCustomerId(), customer.getName(), periodStart, asOfDate, totalPoints, monthlyBreakdown);
    }

    public List<CustomerRewardsResponse> getRewardsForAllCustomers(int months, LocalDate asOfDate) {
        return transactionStore.findAllCustomers().stream()
                .map(c -> getRewardsForCustomer(c.getCustomerId(), months, asOfDate))
                .collect(Collectors.toList());
    }

    private List<Transaction> fetchTransactions(String customerId) {
        CompletableFuture<List<Transaction>> future = transactionDataService.fetchTransactionsForCustomer(customerId);
        try {
            return future.get(FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionFetchException(customerId, e);
        } catch (ExecutionException | TimeoutException e) {
            log.error("Failed to fetch transactions for customer {}", customerId, e);
            throw new TransactionFetchException(customerId, e);
        }
    }
}
