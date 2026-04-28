package iispl.com.service;

import java.util.ArrayList;
import java.util.List;

import iispl.com.model.Check;

public class CheckService {

    private static List<Check> checkList = new ArrayList<>();

    // -----------------------------
    // SAVE CHECK
    // -----------------------------
    public void saveCheck(Check c) {
        c.setStatus("pending");
        checkList.add(c);
    }

    // -----------------------------
    // GET ALL CHECKS
    // -----------------------------
    public List<Check> getAllChecks() {
        return checkList;
    }

    // -----------------------------
    // VALIDATE ALL CHECKS
    // -----------------------------
    public void validateChecks() {

        for (Check c : checkList) {

            List<String> errors = new ArrayList<>();

            // 1. Account number validation
            if (c.getAccountNo() == null || !c.getAccountNo().matches("\\d{12}")) {
                errors.add("Account number must be 12 digits");
            }

            // 2. MICR validation
            if (c.getMicr() == null || !c.getMicr().matches("\\d{9}")) {
                errors.add("MICR must be 9 digits");
            }

            // 3. Amount vs Words validation (✅ FIXED HERE)
            if (!amountMatchesWords(c.getAmount(), c.getAmountInWords())) {
                errors.add("Amount and words do not match");
            }

            // FINAL STATUS
            if (errors.isEmpty()) {
                c.setStatus("valid");
                c.setValidationMsg("All validations passed");
            } else {
                c.setStatus("invalid");
                c.setValidationMsg(String.join(", ", errors));
            }
        }
    }

    // -----------------------------
    // GET VALID CHECKS
    // -----------------------------
    public List<Check> getValidChecks() {
        List<Check> validList = new ArrayList<>();

        for (Check c : checkList) {
            if ("valid".equalsIgnoreCase(c.getStatus())) {
                validList.add(c);
            }
        }

        return validList;
    }

    // -----------------------------
    // ✅ FIXED AMOUNT MATCH LOGIC
    // -----------------------------
    private boolean amountMatchesWords(Double amount, String words) {

        if (amount == null || words == null) return false;

        String generatedWords = convertNumberToWords(amount.intValue());

        // normalize expected
        String expected = generatedWords.toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();

        // normalize user input
        String actual = words.toLowerCase()
                .replace("only", "")   // ignore "only"
                .replaceAll("\\s+", " ")
                .trim();

        return expected.equals(actual);
    }

    // -----------------------------
    // NUMBER → WORDS
    // -----------------------------
    private String convertNumberToWords(int number) {

        String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six",
                "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve",
                "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen",
                "Eighteen", "Nineteen"};

        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty",
                "Sixty", "Seventy", "Eighty", "Ninety"};

        if (number < 20) return units[number];

        if (number < 100) {
            return (tens[number / 10] + " " + units[number % 10]).trim();
        }

        if (number < 1000) {
            return (units[number / 100] + " Hundred " +
                    convertNumberToWords(number % 100)).trim();
        }

        if (number < 100000) {
            return (convertNumberToWords(number / 1000) + " Thousand " +
                    convertNumberToWords(number % 1000)).trim();
        }

        return String.valueOf(number);
    }
}