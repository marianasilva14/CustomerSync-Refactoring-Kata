# Technical Test

Here’s my refactored solution to the CustomerSync Refactoring Kata.
The main goal was to separate business logic from data, while keeping the existing behavior unchanged.
I also implemented the requested feature to sync the new `bonusPointsBalance` field for private customers.

## Here’s a summary of the main changes I made:

- `CustomerSync` now only coordinates the steps; it no longer mixes business rules with database calls.
- Matching logic has been moved to `PersonMatchStrategy` / `CompanyMatchStrategy`, which use a `CustomerRepository` to fetch data.
- `CustomerUpdater` handles all field updates
- `NormalizedCustomer` wraps `ExternalCustomer`, so the business layer has a clean interface without touching external DTOs.
- `CustomerRepo` (with `CustomerRepoImplementation`) is the only entry point to persistence; it wraps `CustomerDataAccess`, which still talks to `CustomerDataLayer`.
- Added `bonusPointsBalance` support for private customer with approval tests and Mockito tests to cover this functionality.
  

Note: I based my refactoring on the `with_tests` branch from the original repository.  
According to its README, this branch provides a good set of unit tests that use a **Fake database** and an **Approval Testing** approach to verify that the stored customer is synchronized correctly with the external customer.  
This setup allowed me to focus on refactoring the code while keeping test coverage intact.  
I also added a few **Mockito tests** where necessary to cover the new `bonusPointsBalance` feature and ensure the new architecture works as expected.

### Layered Architecture
- Coordinator: `CustomerSync` orchestrates the flow only.
- Business services: `NormalizedCustomer`, `PersonMatchStrategy` / `CompanyMatchStrategy`, `CustomerUpdater`.
- Repository: `CustomerRepo` interface + `CustomerRepoImpl` wrapping `CustomerDataAccess`.
- Data layer: `CustomerDataAccess` delegates to `CustomerDataLayer` (or `FakeDatabase` in tests).

This separation makes the sync logic easier to test, maintain, and extend.

### Clean Code & Practices
- Removed duplicated matching/update logic and extracted strategies and updater classes.
- Introduced interfaces for the repository and strategies to reduce coupling.

### Bonus Points Feature
- Added `bonusPointsBalance` to both `ExternalCustomer` and `Customer`.
- Balance is copied only for private customers (both create and update paths) and ignored for companies.
- Covered the new behavior with approval tests and a Mockito test to ensure updates happen when values differ.