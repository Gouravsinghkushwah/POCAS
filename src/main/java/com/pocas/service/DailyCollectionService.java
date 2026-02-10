package com.pocas.service;

import com.pocas.entity.Account;
import com.pocas.entity.AccountStatus;
import com.pocas.entity.Customer;
import com.pocas.entity.DailyCollection;
import com.pocas.exception.ApiException;
import com.pocas.repo.AccountRepository;
import com.pocas.repo.DailyCollectionRepository;
import com.pocas.request.DailyCollectionRequest;
import com.pocas.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyCollectionService {

    private final DailyCollectionRepository dailyCollectionRepository;
    private final AccountRepository accountRepository;

    /**
     * Add a daily collection entry
     */
    public DailyCollectionResponse addDailyCollection(DailyCollectionRequest request) {
        try {
            // Fetch account
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new ApiException("Account with ID " + request.getAccountId() + " does not exist"));

            // Check account status
            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new ApiException("Cannot collect for account with status " + account.getStatus());
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
                    .account(account)
                    .collectionDate(request.getCollectionDate())
                    .collectedAmount(request.getCollectedAmount())
                    .month(month)
                    .year(year)
                    .build();

            DailyCollection saved = dailyCollectionRepository.save(dailyCollection);

            // Update account remaining months based on total collected
            updateAccountAfterCollection(account);

            return mapToResponse(saved);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to add daily collection. Please try again later.");
        }
    }

    /**
     * Update account remaining months and status after daily collection
     */
    private void updateAccountAfterCollection(Account account) {
        try {
            List<DailyCollection> collections = dailyCollectionRepository.findByAccount(account);

            BigDecimal totalPaid = collections.stream()
                    .map(DailyCollection::getCollectedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Remaining months = totalMonths - totalPaid / monthlyKist
            int monthsPaid = totalPaid.divide(account.getMonthlyKist(), BigDecimal.ROUND_DOWN).intValue();
            int remainingMonths = account.getTotalMonths() - monthsPaid;
            account.setRemainingMonths(Math.max(remainingMonths, 0));

            // If fully paid, mark as COMPLETED
            if (totalPaid.compareTo(account.getTotalExpectedDeposit()) >= 0) {
                account.setStatus(AccountStatus.COMPLETED);
                account.setRemainingMonths(0);
            }

            accountRepository.save(account);
        } catch (Exception e) {
            throw new ApiException("Failed to update account after collection");
        }
    }

    /**
     * Get all collections for account
     */
    public List<DailyCollectionResponse> getAllCollections(Long accountId) {
        try {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException("Account with ID " + accountId + " does not exist"));

            return dailyCollectionRepository.findByAccount(account).stream()
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
    public MonthlyCollectionSummaryResponse getMonthlySummary(Long accountId, Integer month, Integer year) {
        try {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException("Account with ID " + accountId + " does not exist"));

            List<DailyCollection> monthlyCollections = dailyCollectionRepository.findByAccountAndMonthAndYear(account, month, year);

            BigDecimal totalCollected = monthlyCollections.stream()
                    .map(DailyCollection::getCollectedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return MonthlyCollectionSummaryResponse.builder()
                    .accountId(account.getId())
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
     * Map Entity → Response DTO
     */
    private DailyCollectionResponse mapToResponse(DailyCollection collection) {
        return DailyCollectionResponse.builder()
                .id(collection.getId())
                .accountId(collection.getAccount().getId())
                .collectionDate(collection.getCollectionDate())
                .collectedAmount(collection.getCollectedAmount())
                .month(collection.getMonth())
                .year(collection.getYear())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .build();
    }

    public MonthlyAccountSummaryResponse getRemainingAndCollected(Long accountId, Integer month, Integer year) {
        try {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException("Account with ID " + accountId + " does not exist"));

            Customer customer = account.getCustomer();

            // All daily collections till the end of given month/year
            List<DailyCollection> allCollections = dailyCollectionRepository.findByAccount(account);

//            BigDecimal totalCollectedTillMonth = allCollections.stream()
//                    .filter(dc -> (dc.getYear() < year) || (dc.getYear() == year && dc.getMonth() <= month))
//                    .map(DailyCollection::getCollectedAmount)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCollectedTillMonth = dailyCollectionRepository.sumMonthlyCollection(accountId, month, year);

            BigDecimal remainingMonthsAmount = account.getMonthlyKist().subtract(totalCollectedTillMonth);

            // Remaining amount = TotalExpectedDeposit - collectedTillMonth
            BigDecimal remainingAmount = account.getTotalExpectedDeposit().subtract(totalCollectedTillMonth);

            return MonthlyAccountSummaryResponse.builder()
                    .accountId(account.getId())
                    .customerId(customer.getId())
                    .customerName(customer.getName())
                    .month(month)
                    .year(year)
                    .monthlyKist(account.getMonthlyKist())
                    .collectedThisMonth(totalCollectedTillMonth)
                    .remainingMonthsAmount(remainingMonthsAmount)
                    .remainingAmount(remainingAmount.max(BigDecimal.ZERO))
                    .remainingMonths(account.getRemainingMonths())
                    .build();

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to fetch remaining and collected data");
        }
    }

    public List<DailyCollectionFullResponse> getAllCollectionsFull() {
        try {
            return dailyCollectionRepository.findAll().stream()
                    .map(collection -> DailyCollectionFullResponse.builder()
                            .collectionId(collection.getId())
                            .accountId(collection.getAccount().getId())
                            .customerId(collection.getAccount().getCustomer().getId())
                            .customerName(collection.getAccount().getCustomer().getName())
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


    public List<DailyCollectionFullResponse> getCollectionsByCustomerId(Long customerId) {
        try {
            // Fetch all accounts of this customer
            List<Account> accounts = accountRepository.findByCustomerId(customerId);

            if (accounts.isEmpty()) {
                throw new ApiException("No accounts found for customer ID " + customerId);
            }

            // Fetch all collections for all accounts of the customer
            return accounts.stream()
                    .flatMap(account -> dailyCollectionRepository.findByAccount(account).stream())
                    .map(collection -> DailyCollectionFullResponse.builder()
                            .collectionId(collection.getId())
                            .accountId(collection.getAccount().getId())
                            .customerId(collection.getAccount().getCustomer().getId())
                            .customerName(collection.getAccount().getCustomer().getName())
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


    public List<DailyPaymentStatusResponse> getPaymentStatusByAccountId(Long accountId) {
        try {
            // Fetch account
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException("Account with ID " + accountId + " not found"));

            LocalDate startDate = account.getStartDate();
            if (startDate == null) {
                throw new ApiException("Start date not set for account ID " + accountId);
            }

            LocalDate today = LocalDate.now();

            //  Fetch all daily collections for this account
            List<DailyCollection> collections = dailyCollectionRepository.findByAccount(account);

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
            throw new ApiException("Failed to fetch daily payment status for account ID " + accountId + ": " + e.getMessage());
        }
    }




}