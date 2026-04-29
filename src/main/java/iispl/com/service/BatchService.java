package iispl.com.service;

import java.util.ArrayList;
import java.util.List;

import iispl.com.dao.BatchDao;
import iispl.com.dao.BatchDaoImpl;
import iispl.com.dao.CheckDao;
import iispl.com.dao.CheckDaoImpl;
import iispl.com.model.Batch;
import iispl.com.model.Check;

public class BatchService {

    private BatchDao batchDao = new BatchDaoImpl();
    private CheckDao checkDao = new CheckDaoImpl();

    /**
     * Creates a batch record in the DB for the given valid checks,
     * marks each check as 'approved' and assigns the batch_id.
     *
     * @return the created Batch (with generated id/batchNo), or null if nothing to batch
     */
    public Batch createBatch(List<Check> validChecks) {

        if (validChecks == null || validChecks.isEmpty()) return null;

        double totalAmount = 0;
        List<Check> approved = new ArrayList<>();

        for (Check c : validChecks) {
            if ("valid".equalsIgnoreCase(c.getStatus())) {
                totalAmount += c.getAmount();
                approved.add(c);
            }
        }

        if (approved.isEmpty()) return null;

        // Build and persist the Batch record
        Batch batch = new Batch();
        batch.setBatchNo("BATCH" + System.currentTimeMillis());
        batch.setTotalChecks(approved.size());
        batch.setTotalAmount(totalAmount);
        batch.setStatus("created");

        try {
            int batchId = batchDao.save(batch);
            if (batchId < 0) throw new RuntimeException("Batch insert returned no generated key");

            // Now link each check to this batch and mark as approved
            for (Check c : approved) {
                checkDao.assignToBatch(c.getId(), batchId);
            }

            return batch;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create batch: " + e.getMessage(), e);
        }
    }

    /** Fetch all batches from DB */
    public List<Batch> getAllBatches() {
        try {
            return batchDao.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load batches: " + e.getMessage(), e);
        }
    }
}
