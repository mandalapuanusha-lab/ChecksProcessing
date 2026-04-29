package iispl.com.service;

import java.util.ArrayList;
import java.util.List;

import iispl.com.dao.CheckDao;
import iispl.com.dao.CheckDaoImpl;
import iispl.com.model.Check;

public class CheckService {

    private CheckDao checkDao = new CheckDaoImpl();

    // -----------------------------
    // SAVE CHECK
    // -----------------------------
    public void saveCheck(Check c) {
        c.setStatus("pending");
        try {
            checkDao.save(c);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save check: " + e.getMessage(), e);
        }
    }

    // -----------------------------
    // GET ALL CHECKS
    // -----------------------------
    public List<Check> getAllChecks() {
        try {
            return checkDao.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load checks: " + e.getMessage(), e);
        }
    }

    // -----------------------------
    // VALIDATE ALL CHECKS
    // -----------------------------
    public void validateChecks() {

        List<Check> checkList;
        try {
            checkList = checkDao.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load checks for validation: " + e.getMessage(), e);
        }

        for (Check c : checkList) {
            if ("batched".equalsIgnoreCase(c.getStatus())) {
                continue;
            }

            List<String> errors = new ArrayList<>();

            // 1. Account number validation
            if (c.getAccountNo() == null || !c.getAccountNo().matches("\\d{12}")) {
                errors.add("Account number must be 12 digits");
            }

            // 2. MICR validation
            if (c.getMicr() == null || !c.getMicr().matches("\\d{9}")) {
                errors.add("MICR must be 9 digits");
            }

            // 3. IFSC format validation
            if (c.getIfsc() == null || !c.getIfsc().matches("[A-Z]{4}0[A-Z0-9]{6}")) {
                errors.add("Invalid IFSC format");
            }

            // 4. Amount in words consistency
            if (!amountMatchesWords(c.getAmount(), c.getAmountInWords())) {
                errors.add("Amount and words do not match");
            }

            // 5. Bank name not empty
            if (c.getBankName() == null || c.getBankName().trim().isEmpty()) {
                errors.add("Bank name is required");
            }

            // 6. Branch name not empty
            if (c.getBranchName() == null || c.getBranchName().trim().isEmpty()) {
                errors.add("Branch name is required");
            }

            // 7. Stale cheque check — older than 6 months
            if (c.getDate() != null) {
                java.util.Calendar sixMonthsAgo = java.util.Calendar.getInstance();
                sixMonthsAgo.add(java.util.Calendar.MONTH, -6);
                if (c.getDate().before(sixMonthsAgo.getTime())) {
                    errors.add("Cheque is stale (older than 6 months)");
                }

                // 8. Post-dated cheque — more than 3 months in future
                java.util.Calendar threeMonthsLater = java.util.Calendar.getInstance();
                threeMonthsLater.add(java.util.Calendar.MONTH, 3);
                if (c.getDate().after(threeMonthsLater.getTime())) {
                    errors.add("Cheque is post-dated beyond 3 months");
                }
            } else {
                errors.add("Cheque date is missing");
            }

            // 9. High-value cheque flag — above ₹10,00,000
            if (c.getAmount() != null && c.getAmount() > 1000000) {
                errors.add("High-value cheque — requires manual review");
            }

            // FINAL STATUS — persist to DB
            String newStatus = errors.isEmpty() ? "valid" : "invalid";
            String newMsg = errors.isEmpty() ? "All validations passed" : String.join(", ", errors);

            try {
                checkDao.updateStatus(c.getId(), newStatus, newMsg);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update check status: " + e.getMessage(), e);
            }
        }
    }

    // -----------------------------
    // GET VALID CHECKS
    // -----------------------------
    public List<Check> getValidChecks() {
        try {
            return checkDao.findByStatus("valid");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load valid checks: " + e.getMessage(), e);
        }
    }

    // -----------------------------
    // FIXED AMOUNT MATCH LOGIC
    // -----------------------------
    private boolean amountMatchesWords(Double amount, String words) {
        if (amount == null || words == null) return false;
        String generatedWords = convertNumberToWords(amount.intValue());
        String expected = generatedWords.toLowerCase().replaceAll("\\s+", " ").trim();
        String actual = words.toLowerCase().replace("only", "").replaceAll("\\s+", " ").trim();
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
        if (number < 100) return (tens[number / 10] + " " + units[number % 10]).trim();
        if (number < 1000) return (units[number / 100] + " Hundred " + convertNumberToWords(number % 100)).trim();
        if (number < 100000) return (convertNumberToWords(number / 1000) + " Thousand " + convertNumberToWords(number % 1000)).trim();
        return String.valueOf(number);
    }
}
