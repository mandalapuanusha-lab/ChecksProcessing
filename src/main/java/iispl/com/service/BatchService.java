package iispl.com.service;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zul.Listitem;

import iispl.com.model.Check;

public class BatchService {

    public void createBatch(List<Listitem> selectedItems) {

        List<Check> validChecks = new ArrayList<>();
        double totalAmount = 0;

        for (Listitem item : selectedItems) {
            Check c = (Check) item.getValue();

            // Only VALID checks allowed
            if ("valid".equalsIgnoreCase(c.getStatus())) {
                validChecks.add(c);
                totalAmount += c.getAmount();

                // Mark as approved
                c.setStatus("approved");
            }
        }

        System.out.println("Batch Created:");
        System.out.println("Total Checks: " + validChecks.size());
        System.out.println("Total Amount: " + totalAmount);
    }
}