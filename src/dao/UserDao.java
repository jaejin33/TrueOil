package dao;

import database.DBConnectionMgr;
import dto.UserDto;
import java.sql.*;

public class UserDao {
	private DBConnectionMgr pool;

	public UserDao() {
		pool = DBConnectionMgr.getInstance();
	}

	// 회원가입 (비밀번호 암호화는 나중에 이 안에서 처리)
	public boolean insertUser(UserDto user) {
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			String sql = "INSERT INTO users (email, password, fuel_type, efficiency, current_mileage, profile_img) VALUES (?, ?, ?, ?, ?, ?)";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, user.getEmail());
			pstmt.setString(2, user.getPassword()); // 나중에 암호화 로직 적용 지점
			pstmt.setString(3, user.getFuelType());
			pstmt.setDouble(4, user.getEfficiency());
			pstmt.setInt(5, user.getCurrentMileage());
			pstmt.setString(6, user.getProfileImg());

			if (pstmt.executeUpdate() == 1)
				flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
		return flag;
	}

	// 이메일 중복 체크 (가입 전 필수 확인 로직)
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

	// 로그인 기능 (추가됨)
	public UserDto loginUser(String email, String password) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		UserDto user = null;

		// 입력받은 이메일과 비밀번호가 일치하는 행을 찾음
		String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

		try {
			con = pool.getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, email);
			pstmt.setString(2, password); // TODO: 암호화 적용 후에는 로직 변경 필요

			rs = pstmt.executeQuery();

			if (rs.next()) {
				// 로그인 성공 시 Dto 객체 생성 및 데이터 담기
				user = new UserDto();
				user.setUserId(rs.getInt("user_id"));
				user.setEmail(rs.getString("email"));
				user.setFuelType(rs.getString("fuel_type"));
				user.setEfficiency(rs.getDouble("efficiency"));
				user.setCurrentMileage(rs.getInt("current_mileage"));
				user.setProfileImg(rs.getString("profile_img"));

				// LocalDateTime 변환
				Timestamp ts = rs.getTimestamp("created_at");
				if (ts != null) {
					user.setCreatedAt(ts.toLocalDateTime());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return user; // 성공 시 유저 객체, 실패 시 null 반환
	}
}