package user;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import database.DBConnectionMgr;
import user.UserDao;
import user.dto.UserDto;
import util.EmailService;
import util.PasswordUtil;
import util.SessionManager;
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
    private EmailService emailService = new EmailService();

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
    
    /**
     * 프로필 이미지를 물리적으로 저장하고 DB 경로를 업데이트합니다.
     * @param sourcePath 원본 파일 경로
     * @return 처리 결과 메시지 (성공 시 null, 실패 시 에러 메시지)
     */
    public String updateProfileImage(String sourcePath) {
        int userId = SessionManager.getUserId();
        String destDir = "resources/images/profiles/";
        String fileName;

        try {
            if (sourcePath == null || sourcePath.equals("DELETE_ACTION")) {
                // [삭제 로직] 사용자가 삭제를 원할 경우 기본 파일명으로 설정
                fileName = "default.png";
            } else {
                // [업로드 로직] 폴더 생성 및 파일 복사
                File dir = new File(destDir);
                if (!dir.exists()) dir.mkdirs();

                String ext = sourcePath.substring(sourcePath.lastIndexOf("."));
                fileName = "profile_" + userId + ext;
                
                Path targetPath = Paths.get(destDir + fileName);
                Files.copy(Paths.get(sourcePath), targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 공통: DB 업데이트
            if (userDao.updateProfileImg(userId, fileName)) {
                return null; // 성공
            } else {
                return "데이터베이스 업데이트에 실패했습니다.";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "파일 처리 중 오류가 발생했습니다.";
        }
    }
    
    /**
     * 아이디 찾기 비즈니스 로직을 수행합니다.
     * <p>DAO를 호출하여 이름과 차량 번호가 일치하는 이메일 정보를 가져옵니다.</p>
     * * @param name 사용자 이름
     * @param carNumber 차량 번호
     * @return 찾은 이메일 문자열
     */
    public String findEmail(String name, String carNumber) {
        return userDao.findEmail(name, carNumber);
    }

    /**
     * 비밀번호 재설정 및 임시 비밀번호 발급 로직을 수행합니다.
     * <p>1. {@link PasswordUtil#generateTempPassword(int)}를 통해 임시 비밀번호 생성</p>
     * <p>2. {@link UserDao#resetPassword(String, String, String)}를 호출하여 DB 업데이트 (암호화 저장)</p>
     * <p>3. 업데이트 성공 시 실제 이메일 발송 서비스(SMTP)를 호출합니다.</p>
     * * @param email 사용자 이메일
     * @param carNumber 차량 번호
     * @return 비밀번호 재설정 프로세스 완료 여부
     */
    public boolean resetPassword(String email, String carNumber) {
        // 1. 무작위 임시 비밀번호 생성 (사용자에게 보여줄 평문)
        String tempPw = PasswordUtil.generateTempPassword(10);
        
        // 2. DB 업데이트 시도 (Dao 내부에서 암호화 처리됨)
        boolean isUpdated = userDao.resetPassword(email, carNumber, tempPw);
        
        if (isUpdated) {
            // 3. 실제 이메일 발송 서비스 호출 (TODO: EmailService 구현 필요)
            emailService.sendTempPassword(email, tempPw);
            System.out.println("[UserService] " + email + " 유저에게 발송될 임시 비번: " + tempPw);
        }
        
        return isUpdated;
    }
}