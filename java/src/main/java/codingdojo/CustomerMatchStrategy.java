package codingdojo;

public interface CustomerMatchStrategy {
    CustomerMatches load(NormalizedCustomer normalizedCustomer, CustomerRepo customerRepo);
}
