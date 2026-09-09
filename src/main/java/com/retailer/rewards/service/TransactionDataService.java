package com.retailer.rewards.service;

import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Simulates fetching a customer's transactions asynchronously from an
 * external source (e.g. a transactions microservice or a database call
 * over the network), so the request thread isn't blocked while the data
 * is retrieved. In this demo the data itself comes from an in-memory
 * {@link TransactionStore}; only the latency/async behavior is simulated.
 */
@Service
public class TransactionDataService {

    private static final Logger log = LoggerFactory.getLogger(TransactionDataService.class);
    private static final long SIMULATED_LATENCY_MS = 150;

    private final TransactionStore transactionStore;

    public TransactionDataService(TransactionStore transactionStore) {
        this.transactionStore = transactionStore;
    }

    @Async("rewardsTaskExecutor")
    public CompletableFuture<List<Transaction>> fetchTransactionsForCustomer(String customerId) {
        log.info("Fetching transactions for customer {} on thread {}", customerId, Thread.currentThread().getName());

        try {
            Thread.sleep(SIMULATED_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            CompletableFuture<List<Transaction>> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }

        List<Transaction> transactions = transactionStore.findTransactionsByCustomerId(customerId);
        log.info("Retrieved {} transaction(s) for customer {}", transactions.size(), customerId);
        return CompletableFuture.completedFuture(transactions);
    }
}
