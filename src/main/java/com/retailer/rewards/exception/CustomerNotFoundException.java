package com.retailer.rewards.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String customerId) {
        super("No customer found with id '" + customerId + "'");
    }
}
