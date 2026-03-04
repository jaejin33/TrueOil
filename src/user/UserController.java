package user;

import user.dto.UserDto;
import user.dto.UserSessionDto;
import util.SessionManager;

import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.Timer;
import java.awt.Component;
import java.io.File;

public class UserController {
    private final UserService userService;
    private final UserDao userDao;

    public UserController() {
        this.userService = new UserService();
        this.userDao = new UserDao();
    }

    /**
     * 회원가입 프로세스를 관리합니다.
     * @param parentView 팝업을 띄울 부모 컴포넌트
     * @param user 가입 정보 DTO
     * @return 가입 성공 여부
     */
    public boolean handleSignup(Component parentView, UserDto user) {
        // 1. 중복 체크
        if (userDao.checkEmail(user.getEmail())) {
            JOptionPane.showMessageDialog(parentView, "이미 가입된 이메일입니다.", "중복 확인", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 2. 서비스 호출 (회원가입 + 소모품 초기화)
        boolean isSuccess = userService.join(user);

        if (isSuccess) {
            JOptionPane.showMessageDialog(parentView, "회원가입이 완료되었습니다!", "성공", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(parentView, "가입 중 오류가 발생했습니다.", "실패", JOptionPane.ERROR_MESSAGE);
        }

        return isSuccess;
    }
    
    /**
     * 로그인 요청을 처리합니다.
     * @param parentView 다이얼로그를 띄울 부모 컴포넌트
     * @param email 입력받은 이메일
     * @param password 입력받은 비밀번호
     * @return 로그인 성공 시 세션 객체 반환, 실패 시 null 반환
     */
    public UserSessionDto handleLogin(Component parentView, String email, String password) {
        // 1. 유효성 검사
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(parentView, "이메일과 비밀번호를 모두 입력해주세요.", "로그인 알림", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        // 2. DAO 호출 (인증 확인)
        UserSessionDto sessionUser = userDao.loginUser(email, password);

        if (sessionUser != null) {
            JOptionPane pane = new JOptionPane(
                sessionUser.email + "님, 환영합니다!", 
                JOptionPane.INFORMATION_MESSAGE, 
                JOptionPane.DEFAULT_OPTION, 
                null, new Object[]{},null);
            JDialog dialog = pane.createDialog(parentView, "로그인 성공");
            Timer timer = new Timer(400, e -> dialog.dispose());
            timer.setRepeats(false);
            timer.start();
            dialog.setModal(false);
            dialog.setAlwaysOnTop(true); 
            dialog.setVisible(true);
            if (dialog.getGraphics() != null) {
                dialog.paintAll(dialog.getGraphics());
            }

            return sessionUser;
        } else {
            JOptionPane.showMessageDialog(parentView, "이메일 또는 비밀번호가 일치하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    /**
     * 마이페이지 조회를 위해 현재 로그인한 사용자의 상세 프로필 데이터를 가져옵니다.
     * * @return 현재 세션 사용자의 UserDto 객체 (세션 만료 시 null)
     */
    public UserDto getMyProfile() {
        int userId = SessionManager.getUserId();
        if (userId == -1) return null;
        return userDao.getUserInfo(userId);
    }
    
    /**
     * 현재 로그인된 사용자의 회원 탈퇴를 처리합니다.
     * * @return 탈퇴 처리 성공 여부
     */
    public boolean withdrawAccount() {
        int userId = SessionManager.getUserId();
        if (userId == -1) {
            return false;
        }
        return userDao.deleteUser(userId);
    }
    
    /**
     * 사용자의 프로필 정보를 업데이트합니다.
     * @param user 수정된 정보를 담은 DTO
     * @return 수정 성공 여부
     */
    public boolean updateProfile(UserDto user) {
        return userDao.updateUserInfo(user);
    }
    
    /**
     * 비밀번호 변경 전 사용자의 현재 비밀번호가 일치하는지 검증합니다.
     * * @param currentPw 입력받은 현재 비밀번호
     * @return 비밀번호 일치 여부
     */
    public boolean verifyPassword(String currentPw) {
        int userId = SessionManager.getUserId();
        return userDao.checkCurrentPassword(userId, currentPw);
    }

    /**
     * 사용자의 비밀번호를 새로운 비밀번호로 변경합니다.
     * * @param newPw 변경하고자 하는 새로운 비밀번호
     * @return 비밀번호 변경 성공 여부
     */
    public boolean changePassword(String newPw) {
        int userId = SessionManager.getUserId();
        return userDao.updatePassword(userId, newPw);
    }
    
    /**
     * 사용자의 프로필 이미지 변경 요청을 서비스 레이어로 전달하여 처리합니다.
     * 물리적 파일 복사와 데이터베이스 경로 업데이트를 포함합니다.
     * * @param sourcePath 사용자가 선택한 원본 이미지 파일의 절대 경로
     * @return 처리 성공 시 null, 실패 시 원인이 담긴 에러 메시지
     */
    public String requestProfileImageChange(String sourcePath) {
        return userService.updateProfileImage(sourcePath);
    }
    
    /**
     * 현재 로그인한 사용자의 유효한 프로필 이미지 경로를 반환합니다.
     */
    public String getProfileImagePath() {
        UserDto user = getMyProfile();
        
        // [핵심 디버깅] Dto에서 꺼낸 값이 실제로 무엇인지 확인합니다.
        if (user == null) {
            System.out.println("❌ 에러: 유저 정보를 불러오지 못했습니다.");
            return "resources/images/profiles/default.png";
        }

        // DB 컬럼명과 DTO 필드명이 'profileImg'가 맞는지 확인하세요!
        String fileName = user.getProfileImg();
        System.out.println("🔍 DB에서 읽어온 파일명: [" + fileName + "]");

        if (fileName == null || fileName.isEmpty()) {
            System.out.println("ℹ️ 파일명이 비어있어 default.png를 사용합니다.");
            fileName = "default.png";
        }
        
        // 경로 후보들
        String[] candidatePaths = {
            "resources/images/profiles/" + fileName,
            "src/resources/images/profiles/" + fileName
        };
        
        for (String path : candidatePaths) {
            File file = new File(path);
            if (file.exists()) {
                System.out.println("✅ 실제 파일 발견: " + file.getAbsolutePath());
                return path; 
            }
        }
        
        System.out.println("❌ 파일이 물리적으로 존재하지 않음: " + fileName);
        return "resources/images/profiles/default.png"; 
    }
}