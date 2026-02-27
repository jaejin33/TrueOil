package fuel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
     * 주유 기록을 등록합니다. 
     * 최신 날짜인 경우에만 차량 주행거리와 소모품 건강도를 갱신합니다.
     */
    public boolean registerFueling(FuelLogDto log) {
        Connection con = null;
        boolean isSuccess = false;

        try {
            con = pool.getConnection();
            con.setAutoCommit(false); // 트랜잭션 시작

            // 1. 주유 로그 저장 (과거/현재 상관없이 무조건 저장)
            int insertResult = fuelDao.insertFuelLog(con, log);

            // 2. 주행거리 업데이트 판단 (mileage가 -1이면 과거 기록으로 간주)
            boolean isLatest = log.getCurrentMileage() != -1;
            
            if (isLatest) {
                // 최신 기록일 때만 유저 주행거리와 소모품 건강도 갱신
                int mileageResult = userDao.updateUserMileage(con, log.getUserId(), log.getCurrentMileage());
                int maintenanceResult = maintenanceDao.updateAllHealthScores(con, log.getUserId(), log.getCurrentMileage());
                
                if (insertResult > 0 && mileageResult > 0) {
                    con.commit();
                    isSuccess = true;
                } else {
                    con.rollback();
                }
            } else {
                // 과거 기록일 때는 로그만 저장 성공하면 커밋
                if (insertResult > 0) {
                    con.commit();
                    isSuccess = true;
                } else {
                    con.rollback();
                }
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
     * 사용자의 가장 최신 주유 날짜를 가져옵니다. (AddFuelLogDialog에서 비교용)
     */
    public String getLastFuelDate(int userId) {
        return fuelDao.getLastFuelDate(userId);
    }

    public List<FuelLogDto> getRecentFuelLogs(int userId) {
        return fuelDao.getRecentFuelLogs(userId);
    }
    
    public Map<String, Integer> getMonthlyStats(int userId) {
        return fuelDao.getMonthlyFuelExpenses(userId);
    }
}