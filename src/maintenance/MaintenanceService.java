package maintenance;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import database.DBConnectionMgr;
import maintenance.dto.*;

/**
 * 차량 소모품 유지보수와 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 소모품 교체 이력 저장, 건강도 조회 및 초기 데이터 생성 등의 기능을 제공하며,
 * 데이터의 일관성을 위해 트랜잭션 관리를 수행합니다.
 * * @author jaejin
 */
public class MaintenanceService {

    private DBConnectionMgr pool;
    private final MaintenanceDao maintenanceDao;

    /**
     * MaintenanceService 객체를 초기화하고 필요한 DAO 의존성을 설정합니다.
     */
    public MaintenanceService() {
        try {
            pool = DBConnectionMgr.getInstance();
        } catch (Exception e) {
            System.err.println("DB 연결자(pool) 초기화 실패: " + e.getMessage());
        }
        this.maintenanceDao = new MaintenanceDao();
    }
    
    /**
     * 사용자의 소모품 교체 처리를 수행합니다.
     * <p>
     * 이 작업은 트랜잭션으로 보호되며 다음 두 단계를 포함합니다:
     * 1. 교체 이력(History) 테이블에 새로운 기록 추가
     * 2. 해당 소모품의 현재 상태(Status) 테이블의 점수 및 기준점 리셋
     * </p>
     *
     * @param history 교체 이력 정보가 담긴 DTO 객체
     * @return 교체 처리가 성공적으로 커밋되면 true, 실패하여 롤백되면 false
     * @throws SQLException 데이터베이스 접근 중 예외 발생 시
     */
    public boolean processReplacement(MaintenanceHistoryDto history) {
        Connection con = null;
        boolean isSuccess = false;

        try {
            con = pool.getConnection();
            con.setAutoCommit(false); // 트랜잭션 시작

            // 이력 저장 및 상태 업데이트 수행
            int result = maintenanceDao.insertMaintenanceHistory(con, history);

            if (result >= 2) {
                con.commit();
                isSuccess = true;
                System.out.println("교체 처리 완료: Item ID " + history.getItemId());
            } else {
                con.rollback();
                System.err.println("교체 처리 실패: 필수 데이터 영향도 부족");
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
     * 사용자의 전체 소모품 건강도 현황을 조회합니다.
     * 점수가 임계값(20점) 미만인 항목이 있을 경우 알림 로그를 출력합니다.
     *
     * @param userId 조회할 사용자의 고유 번호
     * @return 사용자의 소모품 상태 리스트 (MaintenanceStatusDto 목록)
     */
    public List<MaintenanceStatusDto> getHealthDashboard(int userId) {
        List<MaintenanceStatusDto> list = null;
        try {
            list = maintenanceDao.getMaintenanceStatusList(userId);
            
            if (list != null) {
                for (MaintenanceStatusDto dto : list) {
                    if (dto.getHealthScore() < 20) {
                        System.out.println("[알림] " + dto.getItemName() + "의 교체 시기가 임박했습니다!");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    /**
     * [단독 실행용] 신규 사용자의 소모품 관리 데이터를 초기화합니다.
     * 내부적으로 자체 커넥션을 생성하여 트랜잭션을 처리합니다.
     *
     * @param userId 신규 가입한 사용자의 고유 번호
     * @param currentMileage 가입 시점의 차량 누적 주행거리
     */
    public void registInitialStatus(int userId, int currentMileage) {
        Connection con = null;
        try {
            con = pool.getConnection();
            con.setAutoCommit(false);

            maintenanceDao.initMaintenanceStatus(con, userId, currentMileage);

            con.commit();
            System.out.println("유저 [" + userId + "] 소모품 초기 데이터 생성 완료");
        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            }
            System.err.println("소모품 초기화 실패: " + e.getMessage());
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
                pool.freeConnection(con);
            }
        }
    }
    
    /**
     * [트랜잭션 공유용] 외부 서비스로부터 전달받은 커넥션을 사용하여 소모품 데이터를 초기화합니다.
     * 회원가입과 같은 상위 비즈니스 로직에 포함될 때 사용하며, 커넥션을 직접 닫지 않습니다.
     *
     * @param con 상위 서비스에서 전달한 데이터베이스 커넥션
     * @param userId 사용자의 고유 번호
     * @param currentMileage 차량의 초기 주행거리
     */
    public void registInitialStatus(Connection con, int userId, int currentMileage) {
        maintenanceDao.initMaintenanceStatus(con, userId, currentMileage);
    }
    
    /**
     * 소모품 교체 이력을 등록하고 건강도를 재계산하는 핵심 비즈니스 로직
     * @param history 교체 이력 정보가 담긴 DTO
     * @return 전체 프로세스 성공 여부
     */
    public boolean registerReplacement(MaintenanceHistoryDto history) {
        Connection con = null;
        boolean isSuccess = false;

        try {
            con = pool.getConnection();
            // [중요] 자동 커밋을 끕니다. (트랜잭션 시작)
            con.setAutoCommit(false); 

            // 1. DAO의 insertMaintenanceHistory 호출 
            // (이 안에서 이력 Insert와 상태 수식 Update가 일어납니다.)
            int result = maintenanceDao.insertMaintenanceHistory(con, history);

            // 2. 두 작업이 모두 정상 처리되었다면 (영향받은 행의 합이 2 이상)
            if (result >= 2) {
                con.commit(); // DB에 최종 반영
                isSuccess = true;
            } else {
                con.rollback(); // 하나라도 실패하면 원상복구
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
     * 특정 사용자 및 항목의 교체 이력을 가져옵니다.
     * @param userId 현재 로그인한 유저 ID
     * @param itemName 필터링할 항목 이름 (또는 '소모품 전체')
     * @return 교체 이력 리스트
     */
    public List<MaintenanceHistoryDto> getHistoryList(int userId, String itemName) {
        // 현재는 단순 조레이므로 별도의 트랜잭션 처리는 필요 없으나,
        // 나중에 데이터 가공(예: 날짜 형식 변환 등)이 필요하면 여기서 수행합니다.
        return maintenanceDao.getMaintenanceHistory(userId, itemName);
    }
}