package user;

import java.sql.Connection;
import java.sql.SQLException;
import database.DBConnectionMgr;
import user.UserDao;
import user.dto.UserDto;
import maintenance.MaintenanceService;

/**
 * 사용자 관련 고차원 비즈니스 로직을 수행하는 서비스 클래스입니다.
 * 회원가입 시 사용자 정보 저장과 소모품 초기 데이터 생성을 하나의 트랜잭션으로 관리합니다.
 * * @author True Oil Project Team
 */
public class UserService {
    private DBConnectionMgr pool;
    private UserDao userDao;
    private MaintenanceService maintenanceService;

    /**
     * UserService를 초기화하고 필요한 DAO 및 서비스 의존성을 주입합니다.
     */
    public UserService() {
        pool = DBConnectionMgr.getInstance();
        userDao = new UserDao();
        maintenanceService = new MaintenanceService();
    }

    /**
     * [통합 회원가입] 신규 유저를 등록하고 해당 유저의 소모품 관리 데이터를 초기화합니다.
     * <p>
     * 수행 순서:
     * 1. users 테이블에 기본 정보 저장
     * 2. 가입된 유저의 고유 ID(PK) 조회
     * 3. 해당 ID를 기반으로 maintenance_status 테이블 초기 데이터 생성
     * 이 모든 과정은 하나의 Connection을 공유하며, 하나라도 실패 시 전체 롤백됩니다.
     * </p>
     *
     * @param user 가입할 유저의 정보가 담긴 DTO
     * @return 가입 및 초기화 전체 성공 시 true, 실패 시 false
     */
    public boolean join(UserDto user) {
        Connection con = null;
        boolean isSuccess = false;

        try {
            con = pool.getConnection();
            // [중요] 여러 작업을 하나의 트랜잭션으로 묶기 위해 자동 커밋 해제
            con.setAutoCommit(false); 

            // 1. 유저 정보 저장 (동일한 커넥션 전달)
            boolean userInserted = userDao.insertUser(con, user);

            if (userInserted) {
                // 2. 소모품 데이터 생성을 위해 가입된 유저의 ID 식별
                int generatedId = userDao.getUserIdByEmail(con, user.getEmail());
                
                if (generatedId != -1) {
                    // 3. 소모품 서비스에 커넥션을 넘겨 초기 상태 데이터 생성
                    maintenanceService.registInitialStatus(con, generatedId, user.getCurrentMileage());
                    
                    // 모든 작업이 정상적으로 수행되었을 때만 DB에 최종 반영
                    con.commit(); 
                    isSuccess = true;
                    System.out.println("회원가입 및 소모품 초기화 성공: " + user.getEmail());
                } else {
                    con.rollback();
                }
            } else {
                con.rollback();
            }

        } catch (Exception e) {
            // 예외 발생 시 모든 작업을 가입 전 상태로 되돌림
            if (con != null) {
                try { con.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            // 자원 반납 및 커넥션 상태 복구
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
                pool.freeConnection(con);
            }
        }
        return isSuccess;
    }
}