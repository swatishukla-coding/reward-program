package com.retailer.rewards.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory stand-in for a real transactions data source (a database or a
 * downstream "transactions" microservice). Seeded once at startup from a
 * bundled JSON fixture so the demo dataset is easy to review and adjust.
 *
 * Swapping this for a JPA repository or a Feign/RestTemplate client later
 * would not require any changes to the service layer, since callers only
 * depend on the methods below.
 */
@Repository
public class TransactionStore {

    private static final Logger log = LoggerFactory.getLogger(TransactionStore.class);
    private static final String SEED_FILE = "data/seed-data.json";

    private final ObjectMapper objectMapper;

    private Map<String, Customer> customersById = Collections.emptyMap();
    private Map<String, List<Transaction>> transactionsByCustomerId = Collections.emptyMap();

    public TransactionStore() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    void loadSeedData() {
        try (InputStream inputStream = new ClassPathResource(SEED_FILE).getInputStream()) {
            SeedData seedData = objectMapper.readValue(inputStream, SeedData.class);

            Map<String, Customer> customerMap = new HashMap<>();
            for (Customer customer : seedData.getCustomers()) {
                customerMap.put(customer.getCustomerId(), customer);
            }
            this.customersById = Collections.unmodifiableMap(customerMap);

            this.transactionsByCustomerId = Collections.unmodifiableMap(
                    seedData.getTransactions().stream()
                            .collect(Collectors.groupingBy(Transaction::getCustomerId)));

            log.info("Loaded {} customers and {} transactions from {}",
                    customersById.size(), seedData.getTransactions().size(), SEED_FILE);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load seed data from " + SEED_FILE, e);
        }
    }

    public Optional<Customer> findCustomerById(String customerId) {
        return Optional.ofNullable(customersById.get(customerId));
    }

    public List<Customer> findAllCustomers() {
        return new ArrayList<>(customersById.values());
    }

    public List<Transaction> findTransactionsByCustomerId(String customerId) {
        return transactionsByCustomerId.getOrDefault(customerId, Collections.emptyList());
    }
}
