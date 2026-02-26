package fuel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import database.DBConnectionMgr;
import fuel.dto.FuelLogDto;
import user.UserDao;
import maintenance.MaintenanceDao;

public class FuelService {
    private DBConnectionMgr pool;
    private FuelDao fuelDao;
    private UserDao userDao;
    private MaintenanceDao maintenanceDao;

    public FuelService() {
        pool = DBConnectionMgr.getInstance();
        fuelDao = new FuelDao();
        userDao = new UserDao();
        maintenanceDao = new MaintenanceDao();
    }

    /**
     * 주유 기록을 등록하고, 차량 주행거리 갱신 및 소모품 건강도를 일괄 업데이트합니다.
     * @param log 주유 기록 정보 DTO
     * @return 트랜잭션 성공 여부
     */
    public boolean registerFueling(FuelLogDto log) {
        Connection con = null;
        boolean isSuccess = false;

        try {
            con = pool.getConnection();
            // [트랜잭션 시작] 모든 작업이 성공해야만 DB에 반영됨
            con.setAutoCommit(false);

            // 1. 주유 로그 테이블에 기록 저장
            int insertResult = fuelDao.insertFuelLog(con, log);

            // 2. 사용자 테이블(users)의 현재 주행거리(current_mileage) 업데이트
            int mileageResult = userDao.updateUserMileage(con, log.getUserId(), log.getCurrentMileage());

            // 3. 주행거리 변화에 따른 모든 소모품 건강도(health_score) 재계산
            // (MaintenanceDao에 작성했던 정밀 수식 업데이트 쿼리 호출)
            int maintenanceResult = maintenanceDao.updateAllHealthScores(con, log.getUserId(), log.getCurrentMileage());

            // 세 작업이 모두 정상적으로 실행되었는지 확인 (결과값이 0보다 커야 함)
            if (insertResult > 0 && mileageResult > 0) {
                con.commit(); // 모든 변경사항 확정
                isSuccess = true;
                System.out.println("[Service] 주유 등록 및 건강도 갱신 완료 (User: " + log.getUserId() + ")");
            } else {
                con.rollback(); // 하나라도 실패 시 무효화
            }

        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
                pool.freeConnection(con);
            }
        }
        return isSuccess;
    }

    /**
     * 최근 주유 기록 리스트를 가져옵니다 (VehiclePage 하단 리스트용)
     */
    public List<FuelLogDto> getRecentFuelLogs(int userId) {
        return fuelDao.getRecentFuelLogs(userId);
    }
}