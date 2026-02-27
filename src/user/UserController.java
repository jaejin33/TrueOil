package user;

import user.dto.UserDto;
import user.dto.UserSessionDto;

import javax.swing.JOptionPane;
import java.awt.Component;

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
            JOptionPane.showMessageDialog(parentView, sessionUser.email + "님, 환영합니다!", "로그인 성공", JOptionPane.INFORMATION_MESSAGE);
            return sessionUser;
        } else {
            JOptionPane.showMessageDialog(parentView, "이메일 또는 비밀번호가 일치하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}