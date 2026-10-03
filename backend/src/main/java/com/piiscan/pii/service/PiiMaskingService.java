package com.piiscan.pii.service;

import com.piiscan.pii.enums.PiiEntityType;
import org.springframework.stereotype.Service;

/**
 * Generates masked representations for detected PII values.
 * Each masking strategy is tailored to the entity type.
 */
@Service
public class PiiMaskingService {

    public String mask(String originalValue, PiiEntityType entityType) {
        if (originalValue == null || originalValue.isBlank()) return originalValue;

        return switch (entityType) {
            case AADHAAR         -> maskAadhaar(originalValue);
            case PAN             -> maskPan(originalValue);
            case PASSPORT        -> maskPassport(originalValue);
            case DRIVING_LICENSE -> maskDrivingLicense(originalValue);
            case CREDIT_CARD,
                 DEBIT_CARD      -> maskCard(originalValue);
            case BANK_ACCOUNT    -> maskBankAccount(originalValue);
            case IFSC            -> maskIfsc(originalValue);
            case UPI_ID          -> maskUpi(originalValue);
            case EMAIL           -> maskEmail(originalValue);
            case PHONE           -> maskPhone(originalValue);
            case PIN_CODE        -> maskPinCode(originalValue);
            case PERSON_NAME     -> maskName(originalValue);
            case DATE_OF_BIRTH   -> maskDob(originalValue);
            case ADDRESS         -> maskAddress(originalValue);
            default              -> maskGeneric(originalValue);
        };
    }

    /** 123456789012 → XXXXXXXX9012 */
    private String maskAadhaar(String v) {
        if (v.length() < 4) return "XXXX";
        return "X".repeat(v.length() - 4) + v.substring(v.length() - 4);
    }

    /** ABCDE1234F → ABCDE****F */
    private String maskPan(String v) {
        if (v.length() < 2) return "****";
        return v.substring(0, 5) + "****" + v.charAt(v.length() - 1);
    }

    /** A1234567 → A*****67 */
    private String maskPassport(String v) {
        if (v.length() < 3) return "****";
        return v.charAt(0) + "*".repeat(v.length() - 3) + v.substring(v.length() - 2);
    }

    /** DL123456789012 → DL**********12 */
    private String maskDrivingLicense(String v) {
        if (v.length() < 4) return "****";
        return v.substring(0, 2) + "*".repeat(v.length() - 4) + v.substring(v.length() - 2);
    }

    /** 4111111111111111 → 4111 **** **** 1111 */
    private String maskCard(String v) {
        String digits = v.replaceAll("\\D", "");
        if (digits.length() < 4) return "****";
        return "*".repeat(digits.length() - 4) + digits.substring(digits.length() - 4);
    }

    /** 1234567890 → XXXXXX7890 */
    private String maskBankAccount(String v) {
        if (v.length() < 4) return "XXXX";
        return "X".repeat(v.length() - 4) + v.substring(v.length() - 4);
    }

    /** SBIN0001234 → SBIN****234 */
    private String maskIfsc(String v) {
        if (v.length() < 5) return "****";
        return v.substring(0, 4) + "****" + v.substring(v.length() - 3);
    }

    /** user@upi → u***@upi */
    private String maskUpi(String v) {
        int at = v.indexOf('@');
        if (at <= 1) return "***@upi";
        String user = v.substring(0, at);
        String domain = v.substring(at);
        return user.charAt(0) + "*".repeat(Math.max(1, user.length() - 1)) + domain;
    }

    /** john@gmail.com → jo****@gmail.com */
    private String maskEmail(String v) {
        int at = v.indexOf('@');
        if (at < 2) return "****@****.com";
        String local = v.substring(0, at);
        String domain = v.substring(at);
        int show = Math.min(2, local.length());
        return local.substring(0, show) + "*".repeat(local.length() - show) + domain;
    }

    /** 9876543210 → 98******10 */
    private String maskPhone(String v) {
        String digits = v.replaceAll("\\D", "");
        if (digits.length() < 4) return "****";
        return digits.substring(0, 2) + "*".repeat(digits.length() - 4) + digits.substring(digits.length() - 2);
    }

    /** 560001 → 5****1 */
    private String maskPinCode(String v) {
        if (v.length() < 2) return "****";
        return v.charAt(0) + "*".repeat(v.length() - 2) + v.charAt(v.length() - 1);
    }

    /** John Doe → J*** D** */
    private String maskName(String v) {
        String[] parts = v.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.length() > 1) sb.append(p.charAt(0)).append("*".repeat(p.length() - 1));
            else sb.append(p);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /** 01-01-1990 → **-**-1990 */
    private String maskDob(String v) {
        if (v.length() < 4) return "****";
        return "**/**/" + v.substring(v.length() - 4);
    }

    /** 123 Main Street, City → *** **** ******, City */
    private String maskAddress(String v) {
        if (v.length() < 5) return "*".repeat(v.length());
        return v.substring(0, 3) + "*".repeat(v.length() - 3);
    }

    /** Generic: show first 2 and last 2 */
    private String maskGeneric(String v) {
        if (v.length() <= 4) return "*".repeat(v.length());
        return v.substring(0, 2) + "*".repeat(v.length() - 4) + v.substring(v.length() - 2);
    }
}
