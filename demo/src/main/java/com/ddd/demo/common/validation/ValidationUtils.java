package com.ddd.demo.common.validation;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Utility class for common validation methods
 */
public final class ValidationUtils {

    // Common regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,50}$"
    );

    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile(
            "^[0-9]{5,10}$"
    );

    private static final Pattern VIETNAM_POSTAL_CODE_PATTERN = Pattern.compile(
            "^[0-9]{6}$"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"
    );

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========== EMAIL VALIDATION ==========
    public static boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidEmailDomain(String email, String... allowedDomains) {
        if (!isValidEmail(email)) {
            return false;
        }

        String domain = email.substring(email.lastIndexOf('@') + 1).toLowerCase();
        for (String allowedDomain : allowedDomains) {
            if (domain.equals(allowedDomain.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // ========== USERNAME VALIDATION ==========
    public static boolean isValidUsername(String username) {
        return StringUtils.hasText(username) && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isUsernameAvailable(String username, UsernameChecker checker) {
        return isValidUsername(username) && !checker.exists(username);
    }

    @FunctionalInterface
    public interface UsernameChecker {
        boolean exists(String username);
    }

    // ========== STRING VALIDATION ==========
    public static boolean isValidStringLength(String str, int minLength, int maxLength) {
        if (!StringUtils.hasText(str)) {
            return minLength == 0;
        }
        int length = str.trim().length();
        return length >= minLength && length <= maxLength;
    }

    public static boolean isNotBlank(String str) {
        return StringUtils.hasText(str);
    }

    public static boolean containsOnlyAlphanumeric(String str) {
        return StringUtils.hasText(str) && str.matches("^[a-zA-Z0-9]+$");
    }

    public static boolean containsOnlyLetters(String str) {
        return StringUtils.hasText(str) && str.matches("^[a-zA-Z\\s]+$");
    }

    public static boolean containsOnlyDigits(String str) {
        return StringUtils.hasText(str) && str.matches("^[0-9]+$");
    }

    // ========== NUMBER VALIDATION ==========
    public static boolean isPositiveNumber(Number number) {
        return number != null && number.doubleValue() > 0;
    }

    public static boolean isNonNegativeNumber(Number number) {
        return number != null && number.doubleValue() >= 0;
    }

    public static boolean isValidRange(Number value, Number min, Number max) {
        if (value == null) {
            return false;
        }
        double val = value.doubleValue();
        double minVal = min != null ? min.doubleValue() : Double.MIN_VALUE;
        double maxVal = max != null ? max.doubleValue() : Double.MAX_VALUE;
        return val >= minVal && val <= maxVal;
    }

    public static boolean isValidBigDecimalRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) {
            return false;
        }
        boolean minValid = min == null || value.compareTo(min) >= 0;
        boolean maxValid = max == null || value.compareTo(max) <= 0;
        return minValid && maxValid;
    }

    public static boolean isValidPercentage(Number percentage) {
        return isValidRange(percentage, 0, 100);
    }

    // ========== DATE VALIDATION ==========
    public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !startDate.isAfter(endDate);
    }

    public static boolean isValidDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return false;
        }
        return !startDateTime.isAfter(endDateTime);
    }

    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    public static boolean isPastDate(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    public static boolean isTodayOrFuture(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }

    public static boolean isValidAge(LocalDate birthDate, int minAge, int maxAge) {
        if (birthDate == null) {
            return false;
        }
        LocalDate now = LocalDate.now();
        int age = now.getYear() - birthDate.getYear();
        if (birthDate.plusYears(age).isAfter(now)) {
            age--;
        }
        return age >= minAge && age <= maxAge;
    }

    // ========== COLLECTION VALIDATION ==========
    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean isValidCollectionSize(Collection<?> collection, int minSize, int maxSize) {
        if (collection == null) {
            return minSize == 0;
        }
        int size = collection.size();
        return size >= minSize && size <= maxSize;
    }

    public static boolean allNotNull(Collection<?> collection) {
        return collection != null && collection.stream().allMatch(java.util.Objects::nonNull);
    }

    // ========== ADDRESS VALIDATION ==========
    public static boolean isValidPostalCode(String postalCode) {
        return StringUtils.hasText(postalCode) && POSTAL_CODE_PATTERN.matcher(postalCode).matches();
    }

    public static boolean isValidVietnamPostalCode(String postalCode) {
        return StringUtils.hasText(postalCode) && VIETNAM_POSTAL_CODE_PATTERN.matcher(postalCode).matches();
    }

    // ========== URL VALIDATION ==========
    public static boolean isValidUrl(String url) {
        return StringUtils.hasText(url) && URL_PATTERN.matcher(url).matches();
    }

    public static boolean isValidHttpsUrl(String url) {
        return isValidUrl(url) && url.toLowerCase().startsWith("https://");
    }

    // ========== IP ADDRESS VALIDATION ==========
    public static boolean isValidIPv4(String ip) {
        return StringUtils.hasText(ip) && IPV4_PATTERN.matcher(ip).matches();
    }

    // ========== SANITIZATION METHODS ==========
    public static String sanitizeInput(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        // Basic sanitization - remove potential harmful characters
        return input.trim()
                .replaceAll("[<>\"'%;()&+]", "")
                .replaceAll("\\s+", " ");
    }

    public static String sanitizeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return filename;
        }
        // Remove potentially dangerous characters from filename
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public static String normalizeVietnameseText(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }

        // Remove Vietnamese diacritics
        String[][] vietnameseDiacritics = {
                {"à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ", "a"},
                {"è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ", "e"},
                {"ì|í|ị|ỉ|ĩ", "i"},
                {"ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ", "o"},
                {"ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ", "u"},
                {"ỳ|ý|ỵ|ỷ|ỹ", "y"},
                {"đ", "d"},
                {"À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ", "A"},
                {"È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ", "E"},
                {"Ì|Í|Ị|Ỉ|Ĩ", "I"},
                {"Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ", "O"},
                {"Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ", "U"},
                {"Ỳ|Ý|Ỵ|Ỷ|Ỹ", "Y"},
                {"Đ", "D"}
        };

        String result = text;
        for (String[] pair : vietnameseDiacritics) {
            result = result.replaceAll(pair[0], pair[1]);
        }

        return result;
    }

    // ========== BUSINESS VALIDATION ==========
    public static boolean isValidTaxCode(String taxCode) {
        // Vietnamese tax code validation
        return StringUtils.hasText(taxCode) &&
                taxCode.matches("^[0-9]{10}(-[0-9]{3})?$");
    }

    public static boolean isValidBankAccount(String accountNumber) {
        // Basic bank account validation (6-20 digits)
        return StringUtils.hasText(accountNumber) &&
                accountNumber.matches("^[0-9]{6,20}$");
    }

    public static boolean isValidCreditCard(String cardNumber) {
        if (!StringUtils.hasText(cardNumber)) {
            return false;
        }

        // Remove spaces and dashes
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");

        // Check if all digits and proper length
        if (!cleanNumber.matches("^[0-9]{13,19}$")) {
            return false;
        }

        // Luhn algorithm check
        return luhnCheck(cleanNumber);
    }

    private static boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    // ========== FILE VALIDATION ==========
    public static boolean isValidFileExtension(String filename, String... allowedExtensions) {
        if (!StringUtils.hasText(filename)) {
            return false;
        }

        String extension = getFileExtension(filename);
        if (!StringUtils.hasText(extension)) {
            return false;
        }

        for (String allowed : allowedExtensions) {
            if (extension.equalsIgnoreCase(allowed)) {
                return true;
            }
        }

        return false;
    }

    public static String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }

    public static boolean isValidFileSize(long fileSizeBytes, long maxSizeBytes) {
        return fileSizeBytes > 0 && fileSizeBytes <= maxSizeBytes;
    }
}