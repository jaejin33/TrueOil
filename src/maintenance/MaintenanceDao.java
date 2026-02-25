package maintenance;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

import database.DBConnectionMgr;
import maintenance.dto.*;

public class MaintenanceDao {
	
	private DBConnectionMgr pool;

    public MaintenanceDao() {
        try {
            pool = DBConnectionMgr.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 특정 사용자의 모든 소모품 건강도 상태를 조회합니다.
     */
    public List<MaintenanceStatusDto> getMaintenanceStatusList(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<MaintenanceStatusDto> list = new ArrayList<>();

        String sql = "SELECT s.*, i.item_name FROM maintenance_status s " +
                     "JOIN maintenance_items i ON s.item_id = i.item_id " +
                     "WHERE s.user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                MaintenanceStatusDto dto = new MaintenanceStatusDto();
                dto.setmId(rs.getInt("m_id"));
                dto.setItemId(rs.getInt("item_id"));
                dto.setItemName(rs.getString("item_name"));
                dto.setHealthScore(rs.getInt("health_score"));
                dto.setCustomCycleMileage(rs.getInt("custom_cycle_mileage"));
                dto.setLastReplaceMileage(rs.getInt("last_replace_mileage"));
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
     * 주유 후 주행거리가 변경되었을 때, 모든 소모품의 건강도를 일괄 재계산합니다.
     */
    public int updateAllHealthScores(Connection con, int userId, int currentMileage) {
        PreparedStatement pstmt = null;
        int result = 0;

        // 건강도 계산 공식: 100 - ((현재주행거리 - 마지막교체거리) / 주기 * 100)
        // custom_cycle_mileage가 -1이면 기본 주기(i.cycle_mileage)를 사용합니다.
        String sql = "UPDATE maintenance_status s " +
                     "JOIN maintenance_items i ON s.item_id = i.item_id " +
                     "SET s.health_score = GREATEST(0, ROUND(100 - ((? - s.last_replace_mileage) / " +
                     "IF(s.custom_cycle_mileage = -1, i.cycle_mileage, s.custom_cycle_mileage) * 100))) " +
                     "WHERE s.user_id = ?";

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, currentMileage);
            pstmt.setInt(2, userId);
            result = pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Service에서 트랜잭션을 관리할 예정이므로 con은 닫지 않고 pstmt만 닫습니다.
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
        }
        return result;
    }
    
    /**
     * 소모품 교체 시 이력을 남기고 현재 상태(점수, 거리)를 리셋합니다.
     */
    public int insertMaintenanceHistory(Connection con, MaintenanceHistoryDto history) {
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        int result = 0;

        // 1. 교체 이력 추가 (History Table)
        String insertSql = "INSERT INTO maintenance_history (user_id, item_id, replace_date, replace_mileage, cost) VALUES (?, ?, ?, ?, ?)";
        
        // 2. 상태 테이블 리셋 (Status Table) - 건강도를 100으로, 마지막 교체 거리를 현재 거리를 업데이트
        String updateSql = "UPDATE maintenance_status SET health_score = 100, last_replace_mileage = ? " +
                           "WHERE user_id = ? AND item_id = ?";

        try {
            // 이력 추가 실행
            pstmt1 = con.prepareStatement(insertSql);
            pstmt1.setInt(1, history.getUserId());
            pstmt1.setInt(2, history.getItemId());
            pstmt1.setString(3, history.getReplaceDate());
            pstmt1.setInt(4, history.getReplaceMileage());
            pstmt1.setInt(5, history.getCost());      
            result += pstmt1.executeUpdate();

            // 상태 리셋 실행
            pstmt2 = con.prepareStatement(updateSql);
            pstmt2.setInt(1, history.getReplaceMileage());
            pstmt2.setInt(2, history.getUserId());
            pstmt2.setInt(3, history.getItemId());
            result += pstmt2.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (pstmt1 != null) try { pstmt1.close(); } catch (SQLException e) {}
            if (pstmt2 != null) try { pstmt2.close(); } catch (SQLException e) {}
        }
        return result; // 두 쿼리가 모두 성공하면 2를 반환
    }

    /**
     * 회원가입 시 사용자의 초기 소모품 상태 데이터를 생성합니다.
     */
    public void initMaintenanceStatus(Connection con, int userId, int currentMileage) {
        PreparedStatement pstmt = null;

        // 가입 시 입력한 주행거리를 마지막 교체 거리로 설정 (입력 안 했으면 0)
        // 건강도는 100으로 시작합니다.
        String sql = "INSERT INTO maintenance_status (user_id, item_id, health_score, last_replace_mileage, custom_cycle_mileage) " +
                     "SELECT ?, item_id, 100, ?, -1 FROM maintenance_items";

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, currentMileage); 
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("소모품 초기화 중 오류: " + e.getMessage());
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
        }
    }
}
