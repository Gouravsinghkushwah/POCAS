package com.pocas.constants;

public interface ApiMessages {

    // Customer Messages
    String CUSTOMER_NOT_FOUND = "Customer not found with ID %d";
    String CUSTOMER_MOBILE_EXISTS = "Customer with mobile number %s already exists";
    String CUSTOMER_EMAIL_EXISTS = "Customer with email %s already exists";
    String CUSTOMER_DEACTIVATED = "Cannot perform operation. Customer is deactivated";
    String CUSTOMER_DEACTIVATED_WITH_STATUS = "Cannot %s for deactivated customer. Customer status: %s";
    String FAILED_TO_CREATE_CUSTOMER = "Failed to create customer. Please try again later.";
    String FAILED_TO_FETCH_CUSTOMERS = "Failed to fetch customers. Please try again later.";
    String FAILED_TO_UPDATE_CUSTOMER = "Failed to update customer. Please try again later.";
    String FAILED_TO_UPDATE_CUSTOMER_STATUS = "Failed to update customer status. Please try again later.";

    // Collection Account Messages
    String COLLECTION_ACCOUNT_NOT_FOUND = "CollectionAccount not found with ID %d";
    String COLLECTION_ACCOUNT_ALREADY_EXISTS = "Customer already has an active collectionAccount";
    String COLLECTION_ACCOUNT_UPDATED_SUCCESSFULLY = "CollectionAccount updated successfully";
    String COLLECTION_ACCOUNT_DOES_NOT_EXIST = "CollectionAccount with ID %d does not exist";
    String COLLECTION_ACCOUNT_NOT_FOUND_ALT = "CollectionAccount with ID %d not found";
    String COLLECTION_ACCOUNT_STATUS_INVALID = "Cannot collect for collectionAccount with status %s";
    String COLLECTION_ACCOUNT_START_DATE_NOT_SET = "Start date not set for collectionAccount ID %d";
    String FAILED_TO_CREATE_COLLECTION_ACCOUNT = "Failed to create collectionAccount. Please try again later.";
    String FAILED_TO_FETCH_COLLECTION_ACCOUNTS = "Failed to fetch accounts. Please try again later.";
    String FAILED_TO_FETCH_COLLECTION_ACCOUNT = "Failed to fetch collectionAccount. Please try again later.";
    String FAILED_TO_UPDATE_COLLECTION_ACCOUNT = "Failed to update collectionAccount after collection";

    // Daily Collection Messages
    String COLLECTION_AMOUNT_INVALID = "Collected amount must be greater than 0";
    String NO_COLLECTION_ACCOUNTS_FOR_CUSTOMER = "No collectionAccounts found for customer ID %d";
    String FAILED_TO_ADD_DAILY_COLLECTION = "Failed to add daily collection. Please try again later.";
    String FAILED_TO_FETCH_DAILY_COLLECTIONS = "Failed to fetch daily collections";
    String FAILED_TO_FETCH_MONTHLY_SUMMARY = "Failed to fetch monthly summary";
    String FAILED_TO_FETCH_REMAINING_COLLECTED = "Failed to fetch remaining and collected data";
    String FAILED_TO_FETCH_ALL_COLLECTIONS = "Failed to fetch all collection data";
    String FAILED_TO_FETCH_COLLECTIONS_FOR_CUSTOMER = "Failed to fetch collections for customer ID %d";
    String FAILED_TO_FETCH_PAYMENT_STATUS = "Failed to fetch daily payment status for collectionAccount ID %d: %s";

    // Common Messages
    String FAILED_OPERATION = "Failed to %s. Please try again later.";
}
