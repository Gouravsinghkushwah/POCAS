package com.pocas.serviceimpl;

import com.pocas.constants.ApiMessages;
import com.pocas.entity.*;
import com.pocas.entity.CollectionAccount;
import com.pocas.exception.ApiException;
import com.pocas.repo.AccountRepository;
import com.pocas.repo.DailyCollectionRepository;
import com.pocas.request.DailyCollectionRequest;
import com.pocas.response.*;
import com.pocas.service.DailyCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
                    .orElseThrow(() -> new ApiException(String.format(ApiMessages.COLLECTION_ACCOUNT_DOES_NOT_EXIST, request.getAccountId())));

            // Check customer status
            Customer customer = collectionAccount.getCustomer();
            if (customer.getStatus() == CustomerStatus.CLOSED) {
                throw new ApiException(String.format(ApiMessages.CUSTOMER_DEACTIVATED_WITH_STATUS, "collect", customer.getStatus()));
            }

            // Check collectionAccount status
            if (collectionAccount.getStatus() != AccountStatus.ACTIVE) {
                throw new ApiException(String.format(ApiMessages.COLLECTION_ACCOUNT_STATUS_INVALID, collectionAccount.getStatus()));
            }

            // Validate amount
            if (request.getCollectedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException(ApiMessages.COLLECTION_AMOUNT_INVALID);
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
            throw new ApiException(ApiMessages.FAILED_TO_ADD_DAILY_COLLECTION);
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
            throw new ApiException(ApiMessages.FAILED_TO_UPDATE_COLLECTION_ACCOUNT);
        }
    }

    /**
     * Get all collections for collectionAccount
     */
    @Override
    public List<DailyCollectionResponse> getAllCollections(Long accountId) {
        try {
            CollectionAccount collectionAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException(String.format(ApiMessages.COLLECTION_ACCOUNT_DOES_NOT_EXIST, accountId)));

            // Check customer status
            Customer customer = collectionAccount.getCustomer();
            if (customer.getStatus() == CustomerStatus.CLOSED) {
                throw new ApiException(String.format(ApiMessages.CUSTOMER_DEACTIVATED_WITH_STATUS, "view collections", customer.getStatus()));
            }

            return dailyCollectionRepository.findByCollectionAccount(collectionAccount).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ApiMessages.FAILED_TO_FETCH_DAILY_COLLECTIONS);
        }
    }

    /**
     * Get monthly summary
     */
    @Override
    public MonthlyCollectionSummaryResponse getMonthlySummary(Long accountId, Integer month, Integer year) {
        try {
            CollectionAccount collectionAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException(String.format(ApiMessages.COLLECTION_ACCOUNT_DOES_NOT_EXIST, accountId)));

            List<DailyCollection> monthlyCollections = dailyCollectionRepository.findByCollectionAccountAndMonthAndYear(collectionAccount, month, year);

            BigDecimal totalCollected = monthlyCollections.stream()
                    .map(DailyCollection::getCollectedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return MonthlyCollectionSummaryResponse.builder()
                    .accountId(collectionAccount.getId())
                    .customerId(collectionAccount.getCustomer().getId())
                    .customerName(collectionAccount.getCustomer().getName())
                    .month(month)
                    .year(year)
                    .totalCollected(totalCollected)
                    .monthlyKist(collectionAccount.getMonthlyKist())
                    .build();

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ApiMessages.FAILED_TO_FETCH_MONTHLY_SUMMARY);
        }
    }

    /**
     * Get remaining and collected amounts for a month
     */
    @Override
    public MonthlyAccountSummaryResponse getRemainingAndCollected(Long accountId, Integer month, Integer year) {
        try {
            CollectionAccount collectionAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException(String.format(ApiMessages.COLLECTION_ACCOUNT_DOES_NOT_EXIST, accountId)));

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
            throw new ApiException(ApiMessages.FAILED_TO_FETCH_REMAINING_COLLECTED);
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
            throw new ApiException(ApiMessages.FAILED_TO_FETCH_ALL_COLLECTIONS);
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
                throw new ApiException(String.format(ApiMessages.NO_COLLECTION_ACCOUNTS_FOR_CUSTOMER, customerId));
            }

            // Check customer status
            Customer customer = collectionAccounts.get(0).getCustomer();
            if (customer.getStatus() == CustomerStatus.CLOSED) {
                throw new ApiException(String.format(ApiMessages.CUSTOMER_DEACTIVATED_WITH_STATUS, "view collections", customer.getStatus()));
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
            throw new ApiException(String.format(ApiMessages.FAILED_TO_FETCH_COLLECTIONS_FOR_CUSTOMER, customerId));
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
                    .orElseThrow(() -> new ApiException(String.format(ApiMessages.COLLECTION_ACCOUNT_NOT_FOUND_ALT, accountId)));

            // Check customer status
            Customer customer = collectionAccount.getCustomer();
            if (customer.getStatus() == CustomerStatus.CLOSED) {
                throw new ApiException(String.format(ApiMessages.CUSTOMER_DEACTIVATED_WITH_STATUS, "view payment status", customer.getStatus()));
            }

            LocalDate startDate = collectionAccount.getStartDate();
            if (startDate == null) {
                throw new ApiException(String.format(ApiMessages.COLLECTION_ACCOUNT_START_DATE_NOT_SET, accountId));
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
            throw new ApiException(String.format(ApiMessages.FAILED_TO_FETCH_PAYMENT_STATUS, accountId, e.getMessage()));
        }
    }

    @Override
    public MonthlyPaymentSummaryResponse getMonthlyPaymentSummary(Long accountId, Integer month, Integer year) {
        try {
            // Validate account exists and is active
            CollectionAccount account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException(String.format(ApiMessages.COLLECTION_ACCOUNT_NOT_FOUND, accountId)));

            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new ApiException(String.format(ApiMessages.COLLECTION_ACCOUNT_STATUS_INVALID, account.getStatus()));
            }

            // Get all payment statuses for the account
            List<DailyPaymentStatusResponse> paymentStatuses = getPaymentStatusByAccountId(accountId);

            // Filter for the requested month/year
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            LocalDate today = LocalDate.now();

            // Calculate days passed in current month
            int daysPassed = Math.min(today.getMonthValue() == month && today.getYear() == year
                    ? today.getDayOfMonth()
                    : startDate.lengthOfMonth(), startDate.lengthOfMonth());

            // Count paid and unpaid days
            int paidDays = 0;
            int unpaidDays = 0;
            BigDecimal totalPaidAmount = BigDecimal.ZERO;
            BigDecimal monthlyKist = account.getMonthlyKist();
            BigDecimal dailyAmount = monthlyKist.divide(BigDecimal.valueOf(startDate.lengthOfMonth()), 2, BigDecimal.ROUND_HALF_UP);

            for (DailyPaymentStatusResponse status : paymentStatuses) {
                LocalDate statusDate = status.getDate();

                // Only count days in the requested month and up to today
                if (!statusDate.isBefore(startDate) && !statusDate.isAfter(endDate) &&
                    (statusDate.isBefore(today) || statusDate.isEqual(today))) {

                    if (status.isPaid()) {
                        paidDays++;
                        totalPaidAmount = totalPaidAmount.add(status.getPaidAmount());
                    } else {
                        unpaidDays++;
                    }
                }
            }

            BigDecimal expectedAmount = BigDecimal.valueOf(daysPassed).multiply(dailyAmount);
            BigDecimal remainingAmount = expectedAmount.subtract(totalPaidAmount);

            return MonthlyPaymentSummaryResponse.builder()
                    .accountId(accountId)
                    .month(month)
                    .year(year)
                    .totalDays(daysPassed)
                    .paidDays(paidDays)
                    .unpaidDays(unpaidDays)
                    .expectedAmount(expectedAmount)
                    .totalPaidAmount(totalPaidAmount)
                    .remainingAmount(remainingAmount)
                    .build();

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(String.format(ApiMessages.FAILED_TO_FETCH_PAYMENT_STATUS, accountId, e.getMessage()));
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

    /**
     * Get all collections with pagination and search
     */
    @Override
    public Page<DailyCollectionFullResponse> getAllCollectionsPaginated(int page, int size, String search) {
        try {
            // Get latest collection per account
            List<DailyCollection> latestCollections = dailyCollectionRepository.findLatestCollectionPerAccount();

            // Filter based on search term
            List<DailyCollection> filteredCollections = latestCollections.stream()
                    .filter(collection -> {
                        if (search == null || search.trim().isEmpty()) {
                            return true;
                        }

                        String searchLower = search.toLowerCase();
                        String customerName = collection.getCollectionAccount().getCustomer().getName().toLowerCase();
                        String accountNumber = collection.getCollectionAccount().getAccountNumber().toLowerCase();
                        String collectionDateStr = collection.getCollectionDate().toString().toLowerCase();

                        return customerName.contains(searchLower) ||
                               accountNumber.contains(searchLower) ||
                               collectionDateStr.contains(searchLower);
                    })
                    .collect(Collectors.toList());

            // Convert to response DTOs
            List<DailyCollectionFullResponse> responseList = filteredCollections.stream()
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

            // Create pagination
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), responseList.size());
            List<DailyCollectionFullResponse> pageContent = responseList.subList(start, end);

            return new PageImpl<>(pageContent, pageable, responseList.size());

        } catch (Exception e) {
            throw new ApiException(ApiMessages.FAILED_TO_FETCH_ALL_COLLECTIONS);
        }
    }

    /**
     * Get latest collection per account (one per customer)
     */
    @Override
    public List<DailyCollectionFullResponse> getLatestCollectionPerAccount() {
        try {
            return dailyCollectionRepository.findLatestCollectionPerAccount().stream()
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
            throw new ApiException(ApiMessages.FAILED_TO_FETCH_ALL_COLLECTIONS);
        }
    }

    /**
     * Get all transactions for an account (full history)
     */
    @Override
    public List<DailyCollectionFullResponse> getAllTransactionsByAccountId(Long accountId) {
        try {
            CollectionAccount collectionAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException(String.format(ApiMessages.COLLECTION_ACCOUNT_DOES_NOT_EXIST, accountId)));

            // Check customer status
            Customer customer = collectionAccount.getCustomer();
            if (customer.getStatus() == CustomerStatus.CLOSED) {
                throw new ApiException(String.format(ApiMessages.CUSTOMER_DEACTIVATED_WITH_STATUS, "view transactions", customer.getStatus()));
            }

            LocalDate startDate = collectionAccount.getStartDate();

            return dailyCollectionRepository.findAllByAccountIdOrderByDateDesc(accountId).stream()
                    .filter(collection -> !collection.getCollectionDate().isBefore(startDate))
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
            throw new ApiException(String.format(ApiMessages.FAILED_TO_FETCH_COLLECTIONS_FOR_CUSTOMER, accountId));
        }
    }
}
