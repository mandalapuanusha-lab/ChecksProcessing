package iispl.com.dao;

import iispl.com.model.Batch;
import java.util.List;

/**
 * BatchDao - Data Access Interface for Batch entity.
 * Defines CRUD operations and summary queries for cheque batch records.
 * Implementation: BatchDaoImpl
 */
public interface BatchDao {

    /**
     * Insert a new batch record into the database.
     * @param batch the Batch object to be saved
     * @return generated primary key (id) of the inserted record
     * @throws Exception on DB failure
     */
    int save(Batch batch) throws Exception;

    /**
     * Fetch a single batch by its primary key.
     * @param id the batch's primary key
     * @return Batch object, or null if not found
     * @throws Exception on DB failure
     */
    Batch findById(int id) throws Exception;

    /**
     * Fetch a batch by its business batch number (e.g. "BATCH1234567890").
     * @param batchNo the batch number string
     * @return Batch object, or null if not found
     * @throws Exception on DB failure
     */
    Batch findByBatchNo(String batchNo) throws Exception;

    /**
     * Retrieve all batch records from the database.
     * @return List of all Batch objects; empty list if none exist
     * @throws Exception on DB failure
     */
    List<Batch> findAll() throws Exception;

    /**
     * Retrieve all batches with a specific status.
     * Valid status values: "created", "submitted"
     * @param status the status to filter by
     * @return List of Batch objects matching the status
     * @throws Exception on DB failure
     */
    List<Batch> findByStatus(String status) throws Exception;

    /**
     * Update all fields of an existing batch record.
     * @param batch the Batch object with updated values (id must be set)
     * @return true if update was successful, false otherwise
     * @throws Exception on DB failure
     */
    boolean update(Batch batch) throws Exception;

    /**
     * Update only the status of a batch record.
     * Used when transitioning a batch from "created" to "submitted".
     * @param id     the batch's primary key
     * @param status new status value
     * @return true if update was successful, false otherwise
     * @throws Exception on DB failure
     */
    boolean updateStatus(int id, String status) throws Exception;

    /**
     * Update the totalChecks and totalAmount summary fields of a batch.
     * Called after checks are added to or removed from a batch.
     * @param id          the batch's primary key
     * @param totalChecks updated check count
     * @param totalAmount updated cumulative amount
     * @return true if update was successful, false otherwise
     * @throws Exception on DB failure
     */
    boolean updateSummary(int id, int totalChecks, double totalAmount) throws Exception;

    /**
     * Delete a batch record by its primary key.
     * Note: associated checks must be unlinked before calling this.
     * @param id the batch's primary key
     * @return true if deletion was successful, false otherwise
     * @throws Exception on DB failure
     */
    boolean delete(int id) throws Exception;

    /**
     * Count the total number of batch records in the database.
     * @return total count
     * @throws Exception on DB failure
     */
    int countAll() throws Exception;

    /**
     * Count batches filtered by status.
     * @param status the status to count ("created" / "submitted")
     * @return count of records with that status
     * @throws Exception on DB failure
     */
    int countByStatus(String status) throws Exception;
}