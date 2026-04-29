package iispl.com.dao;

import iispl.com.model.Check;
import java.util.List;

/**
 * CheckDao - Data Access Interface for Check entity.
 * Defines CRUD operations and status-based queries for cheque records.
 * Implementation: CheckDaoImpl
 */
public interface CheckDao {

    /**
     * Insert a new check record into the database.
     * @param check the Check object to be saved
     * @return generated primary key (id) of the inserted record
     * @throws Exception on DB failure
     */
    int save(Check check) throws Exception;

    /**
     * Fetch a single check by its primary key.
     * @param id the record's primary key
     * @return Check object, or null if not found
     * @throws Exception on DB failure
     */
    Check findById(int id) throws Exception;

    /**
     * Fetch a check record by its cheque number.
     * @param checkNo the cheque number printed on the physical cheque
     * @return Check object, or null if not found
     * @throws Exception on DB failure
     */
    Check findByCheckNo(String checkNo) throws Exception;

    /**
     * Retrieve all check records from the database.
     * @return List of all Check objects; empty list if none exist
     * @throws Exception on DB failure
     */
    List<Check> findAll() throws Exception;

    /**
     * Retrieve all checks having a specific status.
     * Valid status values: "pending", "valid", "invalid", "approved"
     * @param status the status to filter by
     * @return List of Check objects matching the status
     * @throws Exception on DB failure
     */
    List<Check> findByStatus(String status) throws Exception;

    /**
     * Retrieve all checks that belong to a specific batch.
     * @param batchId the batch's primary key
     * @return List of Check objects linked to the batch
     * @throws Exception on DB failure
     */
    List<Check> findByBatchId(int batchId) throws Exception;

    /**
     * Update all fields of an existing check record.
     * @param check the Check object with updated values (id must be set)
     * @return true if update was successful, false otherwise
     * @throws Exception on DB failure
     */
    boolean update(Check check) throws Exception;

    /**
     * Update only the status and validation message of a check.
     * Used by the validation workflow without touching other fields.
     * @param id           the check's primary key
     * @param status       new status value ("valid" / "invalid" / "approved")
     * @param validationMsg descriptive message from the validation result
     * @return true if update was successful, false otherwise
     * @throws Exception on DB failure
     */
    boolean updateStatus(int id, String status, String validationMsg) throws Exception;

    /**
     * Link a check to a batch record and mark it as "approved".
     * @param checkId the check's primary key
     * @param batchId the batch's primary key
     * @return true if update was successful, false otherwise
     * @throws Exception on DB failure
     */
    boolean assignToBatch(int checkId, int batchId) throws Exception;

    /**
     * Delete a check record by its primary key.
     * @param id the check's primary key
     * @return true if deletion was successful, false otherwise
     * @throws Exception on DB failure
     */
    boolean delete(int id) throws Exception;

    /**
     * Count the total number of check records in the database.
     * @return total count
     * @throws Exception on DB failure
     */
    int countAll() throws Exception;

    /**
     * Count checks filtered by status.
     * @param status the status to count
     * @return count of records with that status
     * @throws Exception on DB failure
     */
    int countByStatus(String status) throws Exception;
}