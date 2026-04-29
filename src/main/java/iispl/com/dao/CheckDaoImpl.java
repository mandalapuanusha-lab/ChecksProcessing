package iispl.com.dao;

import iispl.com.model.Check;
import iispl.com.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CheckDaoImpl - Concrete JDBC implementation of {@link CheckDao}.
 * Resides in iispl.com.dao (same package as the interface).
 */
public class CheckDaoImpl implements CheckDao {

    private static final String SQL_INSERT =
            "INSERT INTO checks (check_no, account_no, amount, amount_in_words, " +
            "bank_name, ifsc, micr, branch_name, status, validation_msg, date) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT * FROM checks WHERE id = ?";

    private static final String SQL_SELECT_BY_CHECK_NO =
            "SELECT * FROM checks WHERE check_no = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT * FROM checks ORDER BY id";

    private static final String SQL_SELECT_BY_STATUS =
            "SELECT * FROM checks WHERE status = ? ORDER BY id";

    private static final String SQL_SELECT_BY_BATCH =
            "SELECT * FROM checks WHERE batch_id = ? ORDER BY id";

    private static final String SQL_UPDATE =
            "UPDATE checks SET check_no = ?, account_no = ?, amount = ?, " +
            "amount_in_words = ?, bank_name = ?, ifsc = ?, micr = ?, " +
            "branch_name = ?, status = ?, validation_msg = ?, date = ? WHERE id = ?";

    private static final String SQL_UPDATE_STATUS =
            "UPDATE checks SET status = ?, validation_msg = ? WHERE id = ?";

    private static final String SQL_ASSIGN_BATCH =
            "UPDATE checks SET batch_id = ?, status = 'approved' WHERE id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM checks WHERE id = ?";

    private static final String SQL_COUNT_ALL =
            "SELECT COUNT(*) FROM checks";

    private static final String SQL_COUNT_BY_STATUS =
            "SELECT COUNT(*) FROM checks WHERE status = ?";

    @Override
    public int save(Check check) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, check.getCheckNo());
            ps.setString(2, check.getAccountNo());
            ps.setDouble(3, check.getAmount());
            ps.setString(4, check.getAmountInWords());
            ps.setString(5, check.getBankName());
            ps.setString(6, check.getIfsc());
            ps.setString(7, check.getMicr());
            ps.setString(8, check.getBranchName());
            ps.setString(9, check.getStatus() != null ? check.getStatus() : "pending");
            ps.setString(10, check.getValidationMsg());
            if (check.getDate() != null) {
                ps.setDate(11, new java.sql.Date(check.getDate().getTime()));
            } else {
                ps.setNull(11, java.sql.Types.DATE);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    check.setId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1;
    }

    @Override
    public Check findById(int id) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public Check findByCheckNo(String checkNo) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_CHECK_NO)) {

            ps.setString(1, checkNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Check> findAll() throws Exception {
        List<Check> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<Check> findByStatus(String status) throws Exception {
        List<Check> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_STATUS)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<Check> findByBatchId(int batchId) throws Exception {
        List<Check> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_BATCH)) {

            ps.setInt(1, batchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public boolean update(Check check) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, check.getCheckNo());
            ps.setString(2, check.getAccountNo());
            ps.setDouble(3, check.getAmount());
            ps.setString(4, check.getAmountInWords());
            ps.setString(5, check.getBankName());
            ps.setString(6, check.getIfsc());
            ps.setString(7, check.getMicr());
            ps.setString(8, check.getBranchName());
            ps.setString(9, check.getStatus());
            ps.setString(10, check.getValidationMsg());
            if (check.getDate() != null) {
                ps.setDate(11, new java.sql.Date(check.getDate().getTime()));
            } else {
                ps.setNull(11, java.sql.Types.DATE);
            }
            ps.setInt(12, check.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int id, String status, String validationMsg) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {

            ps.setString(1, status);
            ps.setString(2, validationMsg);
            ps.setInt(3, id);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean assignToBatch(int checkId, int batchId) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_ASSIGN_BATCH)) {

            ps.setInt(1, batchId);
            ps.setInt(2, checkId);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int countAll() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_ALL);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    @Override
    public int countByStatus(String status) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_BY_STATUS)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Check mapRow(ResultSet rs) throws SQLException {
        Check c = new Check();
        c.setId(rs.getInt("id"));
        c.setCheckNo(rs.getString("check_no"));
        c.setAccountNo(rs.getString("account_no"));
        c.setAmount(rs.getDouble("amount"));
        c.setAmountInWords(rs.getString("amount_in_words"));
        c.setBankName(rs.getString("bank_name"));
        c.setIfsc(rs.getString("ifsc"));
        c.setMicr(rs.getString("micr"));
        c.setBranchName(rs.getString("branch_name"));
        c.setStatus(rs.getString("status"));
        c.setValidationMsg(rs.getString("validation_msg"));
        java.sql.Date sqlDate = rs.getDate("date");
        if (sqlDate != null) {
            c.setDate(new java.util.Date(sqlDate.getTime()));
        }
        return c;
    }
}