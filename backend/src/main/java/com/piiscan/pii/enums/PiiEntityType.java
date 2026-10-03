package com.piiscan.pii.enums;

public enum PiiEntityType {
    // Personal Identity
    AADHAAR,
    PAN,
    PASSPORT,
    DRIVING_LICENSE,

    // Financial
    BANK_ACCOUNT,
    CREDIT_CARD,
    DEBIT_CARD,
    IFSC,
    UPI_ID,

    // Contact
    EMAIL,
    PHONE,
    ADDRESS,
    PIN_CODE,
    CITY,
    COUNTRY,

    // Personal Information
    PERSON_NAME,
    DATE_OF_BIRTH,

    // Custom
    CUSTOM
}
