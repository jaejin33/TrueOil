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
     * 마스터 테이블(maintenance_items)과 조인하여 아이템 이름과 기준 교체 주기를 함께 가져옵니다.
     */
    public List<MaintenanceStatusDto> getMaintenanceStatusList(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<MaintenanceStatusDto> list = new ArrayList<>();

        //i.cycle_mileage 컬럼을 SELECT 절에 추가했습니다.
        String sql = "SELECT s.*, i.item_name, i.cycle_mileage FROM maintenance_status s " +
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
                dto.setUserId(rs.getInt("user_id")); // 명시적으로 유저 ID 설정
                dto.setItemName(rs.getString("item_name"));
                dto.setCycleMileage(rs.getInt("cycle_mileage"));
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
        
        // 2. 상태 테이블 리셋 (단순 100점이 아닌, 현재 차량 주행거리에 맞춰 재계산)
        // users 테이블의 current_mileage를 가져와서 (현재거리 - 교체거리)를 계산합니다.
        String updateSql = "UPDATE maintenance_status s " +
                           "JOIN maintenance_items i ON s.item_id = i.item_id " +
                           "JOIN users u ON s.user_id = u.user_id " +
                           "SET s.last_replace_mileage = ?, " +
                           "    s.health_score = GREATEST(0, ROUND(100 - ((u.current_mileage - ?) / " +
                           "    IF(s.custom_cycle_mileage = -1, i.cycle_mileage, s.custom_cycle_mileage) * 100))) " +
                           "WHERE s.user_id = ? AND s.item_id = ?";

        try {
            // 1. 이력 추가
            pstmt1 = con.prepareStatement(insertSql);
            pstmt1.setInt(1, history.getUserId());
            pstmt1.setInt(2, history.getItemId());
            pstmt1.setString(3, history.getReplaceDate());
            pstmt1.setInt(4, history.getReplaceMileage());
            pstmt1.setInt(5, history.getCost());      
            result += pstmt1.executeUpdate();

            // 2. 상태 재계산 업데이트
            pstmt2 = con.prepareStatement(updateSql);
            pstmt2.setInt(1, history.getReplaceMileage()); // last_replace_mileage 업데이트 값
            pstmt2.setInt(2, history.getReplaceMileage()); // 건강도 계산에 쓰일 값
            pstmt2.setInt(3, history.getUserId());
            pstmt2.setInt(4, history.getItemId());
            result += pstmt2.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (pstmt1 != null) try { pstmt1.close(); } catch (SQLException e) {}
            if (pstmt2 != null) try { pstmt2.close(); } catch (SQLException e) {}
        }
        return result;
    }

    /**
     * 신규 사용자의 초기 소모품 상태 데이터를 생성합니다.
     * <p>
     * maintenance_items 테이블에 등록된 모든 소모품 항목을 참조하여 
     * 해당 사용자의 초기 건강도(100)와 교체 기준 거리(가입 시 주행거리)를 설정합니다.
     * </p>
     *
     * @param con 상위 서비스로부터 전달받은 트랜잭션용 커넥션
     * @param userId 신규 가입한 사용자의 고유 번호
     * @param currentMileage 가입 시점의 차량 누적 주행거리 (last_replace_mileage로 설정)
     * @throws RuntimeException SQL 예외 발생 시 상위 트랜잭션에서 롤백할 수 있도록 예외를 던짐
     */
    public void initMaintenanceStatus(Connection con, int userId, int currentMileage) {
        PreparedStatement pstmt = null;

        // SELECT 문을 통해 maintenance_items에 정의된 모든 아이템을 해당 유저용으로 복사 삽입
        String sql = "INSERT INTO maintenance_status (user_id, item_id, health_score, last_replace_mileage, custom_cycle_mileage) " +
                     "SELECT ?, item_id, 100, ?, -1 FROM maintenance_items";

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, currentMileage); 
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("[DAO] 유저 [" + userId + "]를 위한 " + rows + "개의 소모품 항목 생성 완료.");
            } else {
                // 이 로그가 찍힌다면 maintenance_items 테이블이 비어있는 것입니다.
                System.err.println("[경고] maintenance_items 테이블에 데이터가 없어 초기화가 수행되지 않았습니다.");
            }
            
        } catch (SQLException e) {
            System.err.println("소모품 초기화 중 SQL 오류: " + e.getMessage());
            // 예외를 다시 던져서 UserService가 실패를 인지하고 rollback하게 함
            throw new RuntimeException(e); 
        } finally {
            // 커넥션(con)은 서비스에서 관리하므로 pstmt만 닫음
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
        }
    }
    
    /**
     * 사용자의 특정 소모품 커스텀 주기를 업데이트하고 건강도를 재계산합니다.
     */
    public void updateCustomCycle(int userId, String itemName, int newCycle) {
        Connection con = null;
        PreparedStatement pstmt = null;
        
        // 1. 커스텀 주기 업데이트
        // 2. 새로운 주기를 바탕으로 건강도(health_score) 재계산
        String sql = "UPDATE maintenance_status s " +
                     "JOIN maintenance_items i ON s.item_id = i.item_id " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "SET s.custom_cycle_mileage = ?, " +
                     "    s.health_score = GREATEST(0, ROUND(100 - ((u.current_mileage - s.last_replace_mileage) / ? * 100))) " +
                     "WHERE s.user_id = ? AND i.item_name = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, newCycle);
            pstmt.setInt(2, newCycle);
            pstmt.setInt(3, userId);
            pstmt.setString(4, itemName);
            
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }
    
    /**
     * 특정 사용자의 교체 이력을 조회합니다. (항목별 필터링 가능)
     */
    public List<MaintenanceHistoryDto> getMaintenanceHistory(int userId, String itemName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<MaintenanceHistoryDto> list = new ArrayList<>();

        // '소모품 전체'일 경우 모든 항목을, 아닐 경우 특정 항목만 조회
        String sql = "SELECT h.*, i.item_name FROM maintenance_history h " +
                     "JOIN maintenance_items i ON h.item_id = i.item_id " +
                     "WHERE h.user_id = ? " +
                     "AND (? = '소모품 전체' OR i.item_name = ?) " +
                     "ORDER BY h.replace_date DESC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, itemName);
            pstmt.setString(3, itemName);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                MaintenanceHistoryDto dto = new MaintenanceHistoryDto();
                dto.setReplaceDate(rs.getString("replace_date"));
                dto.setItemName(rs.getString("item_name")); // DTO에 담기 위해 필드 필요
                dto.setReplaceMileage(rs.getInt("replace_mileage"));
                dto.setCost(rs.getInt("cost"));
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
