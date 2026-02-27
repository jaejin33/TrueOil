package fuel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
}