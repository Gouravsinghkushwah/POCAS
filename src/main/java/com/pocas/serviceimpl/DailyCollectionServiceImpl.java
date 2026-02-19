package com.pocas.serviceimpl;

import com.pocas.entity.*;
import com.pocas.entity.CollectionAccount;
import com.pocas.exception.ApiException;
import com.pocas.repo.AccountRepository;
import com.pocas.repo.DailyCollectionRepository;
import com.pocas.request.DailyCollectionRequest;
import com.pocas.response.*;
import com.pocas.service.DailyCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyCollectionServiceImpl implements DailyCollectionService {

    private final DailyCollectionRepository dailyCollectionRepository;
    private final AccountRepository accountRepository;

    /**
     * Add a daily collection entry
     */
    @Override
    public DailyCollectionResponse addDailyCollection(DailyCollectionRequest request) {
        try {
            // Fetch collectionAccount
            CollectionAccount collectionAccount = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new ApiException("CollectionAccount with ID " + request.getAccountId() + " does not exist"));

            // Check customer status
            Customer customer = collectionAccount.getCustomer();
            if (customer.getStatus() == CustomerStatus.CLOSED) {
                throw new ApiException("Cannot collect for deactivated customer. Customer status: " + customer.getStatus());
            }

            // Check collectionAccount status
            if (collectionAccount.getStatus() != AccountStatus.ACTIVE) {
                throw new ApiException("Cannot collect for collectionAccount with status " + collectionAccount.getStatus());
            }

            // Validate amount
            if (request.getCollectedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException("Collected amount must be greater than 0");
            }

            // Determine month/year from collectionDate
            int month = request.getCollectionDate().getMonthValue();
            int year = request.getCollectionDate().getYear();

            // Create DailyCollection
            DailyCollection dailyCollection = DailyCollection.builder()
                    .collectionAccount(collectionAccount)
                    .collectionDate(request.getCollectionDate())
                    .collectedAmount(request.getCollectedAmount())
                    .month(month)
                    .year(year)
                    .build();

            DailyCollection saved = dailyCollectionRepository.save(dailyCollection);

            // Update collectionAccount remaining months based on total collected
            updateAccountAfterCollection(collectionAccount);

            return mapToResponse(saved);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to add daily collection. Please try again later.");
        }
    }

    /**
     * Update collectionAccount remaining months and status after daily collection
     */
    private void updateAccountAfterCollection(CollectionAccount collectionAccount) {
        try {
            List<DailyCollection> collections = dailyCollectionRepository.findByCollectionAccount(collectionAccount);

            BigDecimal totalPaid = collections.stream()
                    .map(DailyCollection::getCollectedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Remaining months = totalMonths - totalPaid / monthlyKist
            int monthsPaid = totalPaid.divide(collectionAccount.getMonthlyKist(), BigDecimal.ROUND_DOWN).intValue();
            int remainingMonths = collectionAccount.getTotalMonths() - monthsPaid;
            collectionAccount.setRemainingMonths(Math.max(remainingMonths, 0));

            // If fully paid, mark as COMPLETED
            if (totalPaid.compareTo(collectionAccount.getTotalExpectedDeposit()) >= 0) {
                collectionAccount.setStatus(AccountStatus.COMPLETED);
                collectionAccount.setRemainingMonths(0);
            }

            accountRepository.save(collectionAccount);
        } catch (Exception e) {
            throw new ApiException("Failed to update collectionAccount after collection");
        }
    }

    /**
     * Get all collections for collectionAccount
     */
    @Override
    public List<DailyCollectionResponse> getAllCollections(Long accountId) {
        try {
            CollectionAccount collectionAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException("CollectionAccount with ID " + accountId + " does not exist"));

            // Check customer status
            Customer customer = collectionAccount.getCustomer();
            if (customer.getStatus() == CustomerStatus.CLOSED) {
                throw new ApiException("Cannot view collections for deactivated customer. Customer status: " + customer.getStatus());
            }

            return dailyCollectionRepository.findByCollectionAccount(collectionAccount).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to fetch daily collections");
        }
    }

    /**
     * Get monthly summary
     */
    @Override
    public MonthlyCollectionSummaryResponse getMonthlySummary(Long accountId, Integer month, Integer year) {
        try {
            CollectionAccount collectionAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException("CollectionAccount with ID " + accountId + " does not exist"));

            List<DailyCollection> monthlyCollections = dailyCollectionRepository.findByCollectionAccountAndMonthAndYear(collectionAccount, month, year);

            BigDecimal totalCollected = monthlyCollections.stream()
                    .map(DailyCollection::getCollectedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return MonthlyCollectionSummaryResponse.builder()
                    .accountId(collectionAccount.getId())
                    .month(month)
                    .year(year)
                    .totalCollected(totalCollected)
                    .build();

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to fetch monthly summary");
        }
    }

    /**
     * Get remaining and collected amounts for a month
     */
    @Override
    public MonthlyAccountSummaryResponse getRemainingAndCollected(Long accountId, Integer month, Integer year) {
        try {
            CollectionAccount collectionAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException("CollectionAccount with ID " + accountId + " does not exist"));

            Customer customer = collectionAccount.getCustomer();

            // All daily collections till the end of given month/year
            List<DailyCollection> allCollections = dailyCollectionRepository.findByCollectionAccount(collectionAccount);

//            BigDecimal totalCollectedTillMonth = allCollections.stream()
//                    .filter(dc -> (dc.getYear() < year) || (dc.getYear() == year && dc.getMonth() <= month))
//                    .map(DailyCollection::getCollectedAmount)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCollectedTillMonth = dailyCollectionRepository.sumMonthlyCollection(accountId, month, year);

            BigDecimal remainingMonthsAmount = collectionAccount.getMonthlyKist().subtract(totalCollectedTillMonth);

            // Remaining amount = TotalExpectedDeposit - collectedTillMonth
            BigDecimal remainingAmount = collectionAccount.getTotalExpectedDeposit().subtract(totalCollectedTillMonth);

            return MonthlyAccountSummaryResponse.builder()
                    .accountId(collectionAccount.getId())
                    .customerId(customer.getId())
                    .customerName(customer.getName())
                    .month(month)
                    .year(year)
                    .monthlyKist(collectionAccount.getMonthlyKist())
                    .collectedThisMonth(totalCollectedTillMonth)
                    .remainingMonthsAmount(remainingMonthsAmount)
                    .remainingAmount(remainingAmount.max(BigDecimal.ZERO))
                    .remainingMonths(collectionAccount.getRemainingMonths())
                    .build();

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to fetch remaining and collected data");
        }
    }

    /**
     * Get all collections with full details
     */
    @Override
    public List<DailyCollectionFullResponse> getAllCollectionsFull() {
        try {
            return dailyCollectionRepository.findAll().stream()
                    .map(collection -> DailyCollectionFullResponse.builder()
                            .collectionId(collection.getId())
                            .accountId(collection.getCollectionAccount().getId())
                            .customerId(collection.getCollectionAccount().getCustomer().getId())
                            .customerName(collection.getCollectionAccount().getCustomer().getName())
                            .collectionDate(collection.getCollectionDate())
                            .collectedAmount(collection.getCollectedAmount())
                            .month(collection.getMonth())
                            .year(collection.getYear())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException("Failed to fetch all collection data");
        }
    }

    /**
     * Get collections by customer ID
     */
    @Override
    public List<DailyCollectionFullResponse> getCollectionsByCustomerId(Long customerId) {
        try {
            // Fetch all collectionAccounts of this customer
            List<CollectionAccount> collectionAccounts = accountRepository.findByCustomerId(customerId);

            if (collectionAccounts.isEmpty()) {
                throw new ApiException("No collectionAccounts found for customer ID " + customerId);
            }

            // Check customer status
            Customer customer = collectionAccounts.getFirst().getCustomer();
            if (customer.getStatus() == CustomerStatus.CLOSED) {
                throw new ApiException("Cannot view collections for deactivated customer. Customer status: " + customer.getStatus());
            }

            // Fetch all collections for all collectionAccounts of the customer
            return collectionAccounts.stream()
                    .flatMap(account -> dailyCollectionRepository.findByCollectionAccount(account).stream())
                    .map(collection -> DailyCollectionFullResponse.builder()
                            .collectionId(collection.getId())
                            .accountId(collection.getCollectionAccount().getId())
                            .customerId(collection.getCollectionAccount().getCustomer().getId())
                            .customerName(collection.getCollectionAccount().getCustomer().getName())
                            .collectionDate(collection.getCollectionDate())
                            .collectedAmount(collection.getCollectedAmount())
                            .month(collection.getMonth())
                            .year(collection.getYear())
                            .build())
                    .collect(Collectors.toList());

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to fetch collections for customer ID " + customerId);
        }
    }

    /**
     * Get payment status by collectionAccount ID
     */
    @Override
    public List<DailyPaymentStatusResponse> getPaymentStatusByAccountId(Long accountId) {
        try {
            // Fetch collectionAccount
            CollectionAccount collectionAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException("CollectionAccount with ID " + accountId + " not found"));

            // Check customer status
            Customer customer = collectionAccount.getCustomer();
            if (customer.getStatus() == CustomerStatus.CLOSED) {
                throw new ApiException("Cannot view payment status for deactivated customer. Customer status: " + customer.getStatus());
            }

            LocalDate startDate = collectionAccount.getStartDate();
            if (startDate == null) {
                throw new ApiException("Start date not set for collectionAccount ID " + accountId);
            }

            LocalDate today = LocalDate.now();

            //  Fetch all daily collections for this collectionAccount
            List<DailyCollection> collections = dailyCollectionRepository.findByCollectionAccount(collectionAccount);

            // Map collection date -> total collected amount per day
            Map<LocalDate, BigDecimal> dateToAmountMap = collections.stream()
                    .collect(Collectors.toMap(
                            DailyCollection::getCollectionDate,
                            DailyCollection::getCollectedAmount,
                            BigDecimal::add // sum amounts if multiple collections on same day
                    ));

            // Generate list of DailyPaymentStatusResponse for all dates from start to today
            List<DailyPaymentStatusResponse> statusList = startDate.datesUntil(today.plusDays(1))
                    .map(date -> {
                        BigDecimal amount = dateToAmountMap.getOrDefault(date, BigDecimal.ZERO);
                        return DailyPaymentStatusResponse.builder()
                                .date(date)
                                .paidAmount(amount)
                                .isPaid(amount.compareTo(BigDecimal.ZERO) > 0)
                                .build();
                    })
                    .collect(Collectors.toList());

            return statusList;

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to fetch daily payment status for collectionAccount ID " + accountId + ": " + e.getMessage());
        }
    }

    /**
     * Map Entity → Response DTO
     */
    private DailyCollectionResponse mapToResponse(DailyCollection collection) {
        return DailyCollectionResponse.builder()
                .id(collection.getId())
                .accountId(collection.getCollectionAccount().getId())
                .collectionDate(collection.getCollectionDate())
                .collectedAmount(collection.getCollectedAmount())
                .month(collection.getMonth())
                .year(collection.getYear())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .build();
    }
}
