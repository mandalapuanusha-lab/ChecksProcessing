package iispl.com.dao;

import iispl.com.model.Batch;
import iispl.com.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BatchDaoImpl - Concrete JDBC implementation of {@link BatchDao}.
 * Resides in iispl.com.dao (same package as the interface).
 */
public class BatchDaoImpl implements BatchDao {

    private static final String SQL_INSERT =
            "INSERT INTO batches (batch_no, total_checks, total_amount, status) " +
            "VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT * FROM batches WHERE id = ?";

    private static final String SQL_SELECT_BY_BATCH_NO =
            "SELECT * FROM batches WHERE batch_no = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT * FROM batches ORDER BY id";

    private static final String SQL_SELECT_BY_STATUS =
            "SELECT * FROM batches WHERE status = ? ORDER BY id";

    private static final String SQL_UPDATE =
            "UPDATE batches SET batch_no = ?, total_checks = ?, " +
            "total_amount = ?, status = ? WHERE id = ?";

    private static final String SQL_UPDATE_STATUS =
            "UPDATE batches SET status = ? WHERE id = ?";

    private static final String SQL_UPDATE_SUMMARY =
            "UPDATE batches SET total_checks = ?, total_amount = ? WHERE id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM batches WHERE id = ?";

    private static final String SQL_COUNT_ALL =
            "SELECT COUNT(*) FROM batches";

    private static final String SQL_COUNT_BY_STATUS =
            "SELECT COUNT(*) FROM batches WHERE status = ?";

    @Override
    public int save(Batch batch) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, batch.getBatchNo());
            ps.setInt(2, batch.getTotalChecks());
            ps.setDouble(3, batch.getTotalAmount() != null ? batch.getTotalAmount() : 0.0);
            ps.setString(4, batch.getStatus() != null ? batch.getStatus() : "created");

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    batch.setId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1;
    }

    @Override
    public Batch findById(int id) throws Exception {
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
    public Batch findByBatchNo(String batchNo) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_BATCH_NO)) {

            ps.setString(1, batchNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Batch> findAll() throws Exception {
        List<Batch> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<Batch> findByStatus(String status) throws Exception {
        List<Batch> list = new ArrayList<>();
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
    public boolean update(Batch batch) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, batch.getBatchNo());
            ps.setInt(2, batch.getTotalChecks());
            ps.setDouble(3, batch.getTotalAmount() != null ? batch.getTotalAmount() : 0.0);
            ps.setString(4, batch.getStatus());
            ps.setInt(5, batch.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int id, String status) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateSummary(int id, int totalChecks, double totalAmount) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_SUMMARY)) {

            ps.setInt(1, totalChecks);
            ps.setDouble(2, totalAmount);
            ps.setInt(3, id);

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

    private Batch mapRow(ResultSet rs) throws SQLException {
        Batch b = new Batch();
        b.setId(rs.getInt("id"));
        b.setBatchNo(rs.getString("batch_no"));
        b.setTotalChecks(rs.getInt("total_checks"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setStatus(rs.getString("status"));
        return b;
    }
}