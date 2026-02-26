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
	    
	    String sql = "INSERT INTO users (email, password, car_number, fuel_type, current_mileage) VALUES (?, ?, ?, ?, ?)";
	    
	    try {
	        pstmt = con.prepareStatement(sql);
	        pstmt.setString(1, user.getEmail());
	        pstmt.setString(2, user.getPassword());
	        pstmt.setString(3, user.getCarNumber()); // 추가된 차량번호
	        pstmt.setString(4, user.getFuelType());
	        pstmt.setInt(5, user.getCurrentMileage());

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
}