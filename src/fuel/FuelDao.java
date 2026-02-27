package fuel;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.DBConnectionMgr;
import fuel.dto.FuelLogDto;

public class FuelDao {
    private DBConnectionMgr pool;

    public FuelDao() {
        pool = DBConnectionMgr.getInstance();
    }

    /**
     * 새로운 주유 기록을 저장합니다.
     */
    public int insertFuelLog(Connection con, FuelLogDto log) throws SQLException {
    	
        String sql = "INSERT INTO fuel_logs (user_id, fuel_date, station_name, fuel_price, fuel_amount, current_mileage) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, log.getUserId());
            pstmt.setString(2, log.getFuelDate());
            pstmt.setString(3, log.getStationName());
            pstmt.setInt(4, log.getFuelPrice());
            pstmt.setDouble(5, log.getFuelAmount());
            pstmt.setInt(6, log.getCurrentMileage());
            
            return pstmt.executeUpdate();
        }
    }
    
    public List<FuelLogDto> getRecentFuelLogs(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<FuelLogDto> list = new ArrayList<>();

        // 최근 순으로 최대 6개 조회
        String sql = "SELECT * FROM fuel_logs WHERE user_id = ? ORDER BY fuel_date DESC, fuel_id DESC LIMIT 6";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                FuelLogDto dto = new FuelLogDto();
                dto.setFuelDate(rs.getString("fuel_date"));
                dto.setStationName(rs.getString("station_name"));
                dto.setFuelPrice(rs.getInt("fuel_price"));
                dto.setFuelAmount(rs.getDouble("fuel_amount"));
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 최근 6개월간의 월별 주유비 합계를 조회합니다.
     */
    public Map<String, Integer> getMonthlyFuelExpenses(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // 결과 저장용 (키: "2026-02", 값: 55000)
        Map<String, Integer> stats = new HashMap<>();

        // 최근 6개월 데이터를 월별로 그룹화하여 합산
        String sql = "SELECT DATE_FORMAT(fuel_date, '%Y-%m') AS month, SUM(fuel_price) AS total " +
                     "FROM fuel_logs " +
                     "WHERE user_id = ? AND fuel_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                     "GROUP BY month " +
                     "ORDER BY month ASC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                stats.put(rs.getString("month"), rs.getInt("total"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return stats;
    }
    
    public String getLastFuelDate(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String lastDate = "0000-00-00"; // 기록이 없을 경우 대비

        String sql = "SELECT MAX(fuel_date) FROM fuel_logs WHERE user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getString(1) != null) {
                lastDate = rs.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return lastDate;
    }
}