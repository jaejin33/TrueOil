package user;

import database.DBConnectionMgr;
import user.dto.UserDto;
import user.dto.UserSessionDto;

import java.sql.*;

public class UserDao {
	private DBConnectionMgr pool;

	public UserDao() {
		pool = DBConnectionMgr.getInstance();
	}

	/**
     * [트랜잭션 공유용] 외부에서 전달받은 커넥션을 사용하여 신규 사용자를 등록합니다.
     * <p>비밀번호 암호화 로직은 추후 이 메소드 내부 또는 서비스 레이어에서 적용될 예정입니다.</p>
     * * @param con 상위 서비스로부터 전달받은 데이터베이스 커넥션
     * @param user 등록할 사용자 정보를 담은 DTO 객체
     * @return 등록 성공 시 true, 실패 시 false
     */
	public boolean insertUser(Connection con, UserDto user) {
	    PreparedStatement pstmt = null;
	    boolean flag = false;
	    
	    String sql = "INSERT INTO users (name, email, password, car_number, fuel_type, current_mileage) VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, user.getName()); // 추가된 name 필드
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getCarNumber());
            pstmt.setString(5, user.getFuelType());
            pstmt.setInt(6, user.getCurrentMileage());

	        if (pstmt.executeUpdate() == 1) {
	            flag = true;
	        }
	    } catch (SQLException e) {
	        System.err.println("회원가입 쿼리 실행 중 오류 발생: " + e.getMessage());
	        e.printStackTrace();
	    } finally {
	        // 서비스에서 트랜잭션을 관리하므로 pstmt만 닫고 con은 닫지 않습니다.
	        if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
	    }
	    return flag;
	}

	/**
     * 가입 전 입력한 이메일이 이미 존재하는지 확인합니다.
     * * @param email 중복 확인을 수행할 이메일 주소
     * @return 이미 존재하면 true, 가입 가능하면 false
     */
	public boolean checkEmail(String email) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean exists = false;

		String sql = "SELECT email FROM users WHERE email = ?";

		try {
			con = pool.getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, email);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				exists = true; // 이미 이메일이 존재함
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return exists;
	}

	/**
     * 로그인 인증을 수행하고 세션에 저장할 사용자 최소 정보를 반환합니다.
     * * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @return 로그인 성공 시 UserSessionDto 객체, 실패 시 null
     */
	public UserSessionDto loginUser(String email, String password) {
	    Connection con = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    UserSessionDto sessionUser = null;

	    // 식별에 필요한 최소한의 컬럼만 조회
	    String sql = "SELECT user_id, email, profile_img FROM users WHERE email = ? AND password = ?";

	    try {
	        con = pool.getConnection();
	        pstmt = con.prepareStatement(sql);
	        pstmt.setString(1, email);
	        pstmt.setString(2, password);

	        rs = pstmt.executeQuery();

	        if (rs.next()) {
	            sessionUser = new UserSessionDto();
	            sessionUser.userId = rs.getInt("user_id");
	            sessionUser.email = rs.getString("email");
	            sessionUser.profileImg = rs.getString("profile_img");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        pool.freeConnection(con, pstmt, rs);
	    }
	    return sessionUser; // 가벼운 세션용 객체 리턴
	}
	
	/**
	 * [트랜잭션용] 이메일을 통해 사용자의 고유 번호(user_id)를 조회합니다.
	 * 회원가입 직후 소모품 초기 데이터를 생성하기 위해 PK 값을 식별할 때 사용합니다.
	 * * @param con 상위 서비스로부터 전달받은 커넥션
	 * @param email 조회할 사용자의 이메일
	 * @return 사용자의 고유 번호 (조회 실패 시 -1)
	 */
	public int getUserIdByEmail(Connection con, String email) {
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    int userId = -1;

	    String sql = "SELECT user_id FROM users WHERE email = ?";

	    try {
	        pstmt = con.prepareStatement(sql);
	        pstmt.setString(1, email);
	        rs = pstmt.executeQuery();

	        if (rs.next()) {
	            userId = rs.getInt("user_id");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        // pstmt와 rs만 닫고 con은 닫지 않음
	        if (rs != null) try { rs.close(); } catch (SQLException e) {}
	        if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
	    }
	    return userId;
	}
	
	/**
	 * 사용자의 현재 누적 주행거리를 조회합니다.
	 * VehiclePage 상단 대시보드 및 소모품 건강도 계산의 기준점이 됩니다.
	 * * @param userId 조회할 사용자의 고유 번호
	 * @return 현재 누적 주행거리 (km), 조회 실패 시 0
	 */
	public int getUserMileage(int userId) {
	    Connection con = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    int mileage = 0;

	    String sql = "SELECT current_mileage FROM users WHERE user_id = ?";

	    try {
	        con = pool.getConnection();
	        pstmt = con.prepareStatement(sql);
	        pstmt.setInt(1, userId);
	        rs = pstmt.executeQuery();

	        if (rs.next()) {
	            mileage = rs.getInt("current_mileage");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        pool.freeConnection(con, pstmt, rs);
	    }
	    return mileage;
	}

	/**
	 * 사용자의 차량 정보를 상세 조회합니다. (차량번호, 연료타입 등)
	 * * @param userId 조회할 사용자의 고유 번호
	 * @return 유저 정보를 담은 UserDto (필요한 필드만 채움)
	 */
	public UserDto getUserVehicleInfo(int userId) {
	    Connection con = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    UserDto dto = null;

	    String sql = "SELECT car_number, fuel_type, current_mileage FROM users WHERE user_id = ?";

	    try {
	        con = pool.getConnection();
	        pstmt = con.prepareStatement(sql);
	        pstmt.setInt(1, userId);
	        rs = pstmt.executeQuery();

	        if (rs.next()) {
	            dto = new UserDto();
	            dto.setCarNumber(rs.getString("car_number"));
	            dto.setFuelType(rs.getString("fuel_type"));
	            dto.setCurrentMileage(rs.getInt("current_mileage"));
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        pool.freeConnection(con, pstmt, rs);
	    }
	    return dto;
	}
	
	/**
	 * 사용자의 현재 주행거리를 최신화합니다. (주유 기록 등록 시 호출)
	 * @param con Service에서 관리하는 트랜잭션용 커넥션
	 * @param userId 사용자 고유 번호
	 * @param newMileage 새롭게 입력된 누적 주행거리
	 * @return 업데이트된 행의 수
	 * @throws SQLException 트랜잭션 롤백 처리를 위해 예외를 상위로 던짐
	 */
	public int updateUserMileage(Connection con, int userId, int newMileage) throws SQLException {
	    PreparedStatement pstmt = null;
	    int result = 0;

	    String sql = "UPDATE users SET current_mileage = ? WHERE user_id = ?";

	    try {
	        pstmt = con.prepareStatement(sql);
	        pstmt.setInt(1, newMileage);
	        pstmt.setInt(2, userId);
	        
	        result = pstmt.executeUpdate();
	        
	        if (result > 0) {
	            System.out.println("[DAO] 유저(" + userId + ")의 주행거리가 " + newMileage + "km로 업데이트되었습니다.");
	        }
	    } finally {
	        // 커넥션은 Service에서 닫으므로 pstmt만 닫습니다.
	        if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
	    }
	    return result;
	}
	
	/**
	 * 사용자의 고유 번호(ID)를 기반으로 상세 정보를 조회합니다. (프로필 이미지 포함)
	 * @param userId 조회할 사용자의 고유 번호
	 * @return 사용자 상세 정보를 담은 UserDto 객체
	 */
	public UserDto getUserInfo(int userId) {
	    Connection con = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    UserDto user = null;

	    String sql = "SELECT name, email, car_number, fuel_type, profile_img, created_at FROM users WHERE user_id = ?";

	    try {
	        con = pool.getConnection();
	        pstmt = con.prepareStatement(sql);
	        pstmt.setInt(1, userId);
	        rs = pstmt.executeQuery();

	        if (rs.next()) {
	            user = new UserDto();
	            user.setUserId(userId);
	            user.setName(rs.getString("name"));
	            user.setEmail(rs.getString("email"));
	            user.setCarNumber(rs.getString("car_number"));
	            user.setFuelType(rs.getString("fuel_type"));
	            user.setProfileImg(rs.getString("profile_img")); 
	            
	            user.setCreatedAt(rs.getString("created_at"));
	        }
	    } catch (Exception e) {
	        System.err.println("[UserDao] getUserInfo 상세 조회 오류: " + e.getMessage());
	        e.printStackTrace();
	    } finally {
	        pool.freeConnection(con, pstmt, rs);
	    }
	    return user;
	}
	
	/**
     * 고유 번호(ID)를 기반으로 사용자 레코드를 데이터베이스에서 영구 삭제합니다.
     * <p>주의: 탈퇴 시 해당 사용자와 연결된 모든 참조 데이터가 삭제되거나 처리되어야 합니다.</p>
     * * @param userId 삭제할 사용자의 고유 번호
     * @return 삭제 성공 시 true, 실패 시 false
     */
	public boolean deleteUser(int userId) {
	    Connection con = null;
	    PreparedStatement pstmt = null;
	    boolean flag = false;

	    // 1. 자식 테이블(정비 상태) 먼저 삭제
	    String sql1 = "DELETE FROM maintenance_status WHERE user_id = ?";
	    // 2. 부모 테이블(유저) 삭제
	    String sql2 = "DELETE FROM users WHERE user_id = ?";

	    try {
	        con = pool.getConnection();
	        // 트랜잭션 시작 (자동 커밋 방지)
	        con.setAutoCommit(false);

	        // [작업 1] maintenance_status 삭제
	        pstmt = con.prepareStatement(sql1);
	        pstmt.setInt(1, userId);
	        pstmt.executeUpdate();
	        pstmt.close(); // 다음 쿼리를 위해 닫기

	        // [작업 2] users 삭제
	        pstmt = con.prepareStatement(sql2);
	        pstmt.setInt(1, userId);
	        int result = pstmt.executeUpdate();
	        if (result == 1) {
	            con.commit();
	            flag = true;
	        } else {
	            con.rollback();
	        }
	    } catch (Exception e) {
	        try {
	            if (con != null) con.rollback(); 
	        } catch (Exception re) {
	            re.printStackTrace();
	        }
	        System.err.println("[UserDao] deleteUser 실행 중 오류 발생: " + e.getMessage());
	        e.printStackTrace();
	    } finally {
	        pool.freeConnection(con, pstmt);
	    }
	    return flag;
	}
    
    /**
     * 사용자의 이름, 차량번호, 연료 타입, 주행거리를 일괄 업데이트합니다.
     * @param user 수정할 정보가 담긴 DTO 객체
     * @return 업데이트 성공 여부
     */
    public boolean updateUserInfo(UserDto user) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;

        // email을 조건으로 나머지 정보 수정
        String sql = "UPDATE users SET name = ?, car_number = ?, fuel_type = ?, current_mileage = ? WHERE email = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getCarNumber());
            pstmt.setString(3, user.getFuelType());
            pstmt.setInt(4, user.getCurrentMileage());
            pstmt.setString(5, user.getEmail());

            if (pstmt.executeUpdate() == 1) {
                flag = true;
            }
        } catch (Exception e) {
            System.err.println("[UserDao] updateUserInfo 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }
    
    /**
     * 사용자의 현재 비밀번호가 일치하는지 확인합니다.
     * @param userId 사용자 고유 번호
     * @param password 입력받은 현재 비밀번호
     * @return 일치 여부 (true: 일치, false: 불일치)
     */
    public boolean checkCurrentPassword(int userId, String password) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isMatch = false;

        String sql = "SELECT password FROM users WHERE user_id = ? AND password = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();

            if (rs.next()) isMatch = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return isMatch;
    }

    /**
     * 새로운 비밀번호로 업데이트합니다.
     * @param userId 사용자 고유 번호
     * @param newPassword 변경할 새 비밀번호
     * @return 성공 여부
     */
    public boolean updatePassword(int userId, String newPassword) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;

        String sql = "UPDATE users SET password = ? WHERE user_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);

            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }
    
    /**
     * 사용자의 프로필 이미지 파일명을 업데이트합니다.
     * @param userId 사용자 고유 번호
     * @param fileName 저장된 파일 이름 (예: profile_26.jpg)
     * @return 성공 여부
     */
    public boolean updateProfileImg(int userId, String fileName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE users SET profile_img = ? WHERE user_id = ?";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, fileName);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }
}