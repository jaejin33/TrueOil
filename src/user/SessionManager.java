package user;

import user.dto.UserSessionDto;

public class SessionManager {
	// 프로그램이 꺼질 때까지 유지되는 전역 변수
    private static UserSessionDto loginUser;

    public static void setLoginUser(UserSessionDto user) {
        loginUser = user;
    }

    public static UserSessionDto getLoginUser() {
        return loginUser;
    }

    public static int getUserId() {
        return (loginUser != null) ? loginUser.getUserId() : -1;
    }

    public static void logout() {
        loginUser = null;
    }
    
    public static String getFuelType() {
        return (loginUser != null) ? loginUser.getFuelType() : "휘발유"; // 기본값 설정
    }
}
