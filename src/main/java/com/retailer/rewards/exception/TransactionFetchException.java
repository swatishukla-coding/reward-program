package com.retailer.rewards.exception;

public class TransactionFetchException extends RuntimeException {

    public TransactionFetchException(String customerId, Throwable cause) {
        super("Failed to retrieve transactions for customer '" + customerId + "'", cause);
    }
}
