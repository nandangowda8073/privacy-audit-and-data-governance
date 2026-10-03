package com.piiscan.masking.service;

import com.piiscan.masking.enums.MaskingType;
import com.piiscan.pii.enums.PiiEntityType;
import org.springframework.stereotype.Service;

/**
 * PII-specific masking service that applies FULL or PARTIAL masks.
 * Never modifies the original database value — operates only on DTO strings.
 */
@Service
public class DataMaskingService {

    /**
     * Apply masking to a value based on PII type and masking level.
     */
    public String applyMask(String originalValue, PiiEntityType piiType, MaskingType maskingType) {
        if (originalValue == null || originalValue.isBlank()) return originalValue;
        if (maskingType == MaskingType.NONE) return originalValue;

        return switch (maskingType) {
            case FULL -> applyFullMask(originalValue, piiType);
            case PARTIAL -> applyPartialMask(originalValue, piiType);
            default -> originalValue;
        };
    }

    // ── FULL MASKING ───────────────────────────────────────────────────────────

    private String applyFullMask(String value, PiiEntityType type) {
        return switch (type) {
            case AADHAAR -> "XXXXXXXXXXXX";
            case PAN -> "XXXXXXXXXX";
            case CREDIT_CARD, DEBIT_CARD -> "XXXXXXXXXXXXXXXX";
            case BANK_ACCOUNT -> "XXXXXXXXXXXX";
            case IFSC -> "XXXXXXXXXXX";
            case UPI_ID -> "*".repeat(value.length());
            case EMAIL -> "*".repeat(value.length());
            case PHONE -> "XXXXXXXXXX";
            case PERSON_NAME -> "X".repeat(value.length());
            case ADDRESS -> "X".repeat(Math.min(value.length(), 30));
            case PASSPORT -> "XXXXXXXX";
            case DRIVING_LICENSE -> "X".repeat(value.length());
            case DATE_OF_BIRTH -> "XX-XX-XXXX";
            case PIN_CODE -> "XXXXXX";
            default -> "X".repeat(Math.min(value.length(), 12));
        };
    }

    // ── PARTIAL MASKING ────────────────────────────────────────────────────────

    private String applyPartialMask(String value, PiiEntityType type) {
        return switch (type) {
            case AADHAAR -> maskAadhaarPartial(value);
            case PAN -> maskPanPartial(value);
            case CREDIT_CARD, DEBIT_CARD -> maskCardPartial(value);
            case BANK_ACCOUNT -> maskBankAccountPartial(value);
            case IFSC -> maskIfscPartial(value);
            case UPI_ID -> maskUpiPartial(value);
            case EMAIL -> maskEmailPartial(value);
            case PHONE -> maskPhonePartial(value);
            case PERSON_NAME -> maskNamePartial(value);
            case ADDRESS -> maskAddressPartial(value);
            case PASSPORT -> maskPassportPartial(value);
            case DRIVING_LICENSE -> maskDrivingLicensePartial(value);
            case DATE_OF_BIRTH -> "**-**-" + value.substring(Math.max(0, value.length() - 4));
            case PIN_CODE -> value.charAt(0) + "****" + value.charAt(value.length() - 1);
            default -> maskGenericPartial(value);
        };
    }

    // ── SPECIFIC PARTIAL MASKERS ───────────────────────────────────────────────

    /** 456789123456 → XXXX XXXX 3456 */
    private String maskAadhaarPartial(String v) {
        String digits = v.replaceAll("\\D", "");
        if (digits.length() < 4) return "XXXX XXXX " + digits;
        return "XXXX XXXX " + digits.substring(digits.length() - 4);
    }

    /** ABCDE1234F → XXXXX1234X */
    private String maskPanPartial(String v) {
        if (v.length() < 10) return "XXXXXXXXXX";
        return "XXXXX" + v.substring(5, 9) + "X";
    }

    /** 4532123412345678 → XXXX XXXX XXXX 5678 */
    private String maskCardPartial(String v) {
        String digits = v.replaceAll("\\D", "");
        if (digits.length() < 4) return "XXXX XXXX XXXX XXXX";
        return "XXXX XXXX XXXX " + digits.substring(digits.length() - 4);
    }

    /** 123456789012 → XXXXXXXX9012 */
    private String maskBankAccountPartial(String v) {
        if (v.length() < 4) return "XXXX";
        return "X".repeat(v.length() - 4) + v.substring(v.length() - 4);
    }

    /** SBIN0001234 → SBINXXXXXXX */
    private String maskIfscPartial(String v) {
        if (v.length() < 4) return "XXXXXXX";
        return v.substring(0, 4) + "X".repeat(Math.max(0, v.length() - 4));
    }

    /** nandan@upi → n****@upi */
    private String maskUpiPartial(String v) {
        int at = v.indexOf('@');
        if (at <= 1) return "****" + (at >= 0 ? v.substring(at) : "");
        return v.charAt(0) + "*".repeat(at - 1) + v.substring(at);
    }

    /** nandan@gmail.com → n****@gmail.com */
    private String maskEmailPartial(String v) {
        int at = v.indexOf('@');
        if (at <= 1) return "****@****.com";
        String local = v.substring(0, at);
        String domain = v.substring(at);
        return local.charAt(0) + "*".repeat(Math.max(1, local.length() - 1)) + domain;
    }

    /** 9876543210 → XXXXXX3210 */
    private String maskPhonePartial(String v) {
        String digits = v.replaceAll("\\D", "");
        if (digits.length() < 4) return "XXXXXXXX";
        return "X".repeat(digits.length() - 4) + digits.substring(digits.length() - 4);
    }

    /** Nandan Gowda → N**** G**** */
    private String maskNamePartial(String v) {
        StringBuilder sb = new StringBuilder();
        for (String word : v.split(" ")) {
            if (word.isEmpty()) continue;
            sb.append(word.charAt(0));
            sb.append("*".repeat(Math.max(0, word.length() - 1)));
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    /** 12 MG Road, Bengaluru → XX MG Road, Bengaluru */
    private String maskAddressPartial(String v) {
        if (v.length() < 3) return "XX " + v;
        return "XX " + v.substring(Math.min(3, v.length()));
    }

    /** A1234567 → A*****67 */
    private String maskPassportPartial(String v) {
        if (v.length() < 3) return "XXXXXXXX";
        return v.charAt(0) + "*".repeat(v.length() - 3) + v.substring(v.length() - 2);
    }

    /** DL123456789012 → DL**********12 */
    private String maskDrivingLicensePartial(String v) {
        if (v.length() < 4) return "XXXXXXXX";
        return v.substring(0, 2) + "*".repeat(v.length() - 4) + v.substring(v.length() - 2);
    }

    /** Generic: show first 2 and last 2 */
    private String maskGenericPartial(String v) {
        if (v.length() <= 4) return "*".repeat(v.length());
        return v.substring(0, 2) + "*".repeat(v.length() - 4) + v.substring(v.length() - 2);
    }
}
