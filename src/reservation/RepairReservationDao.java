package reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import database.DBConnectionMgr;
import reservation.dto.RepairReservationDto;

/**
 * 정비 예약 데이터베이스(repair_reservations 테이블)에 접근하여 
 * CRUD(Create, Read, Update, Delete) 작업을 수행하는 데이터 액세스 객체입니다.
 * * @author Bae Jae-jin
 * @version 1.0
 */
public class RepairReservationDao {
    private final DBConnectionMgr pool;

    /**
     * RepairReservationDao 객체를 초기화하며 데이터베이스 커넥션 풀 인스턴스를 가져옵니다.
     */
    public RepairReservationDao() {
        this.pool = DBConnectionMgr.getInstance();
    }

    /**
     * 새로운 정비 예약 정보를 데이터베이스에 등록합니다.
     * * @param dto 등록할 예약 정보가 담긴 RepairReservationDto 객체
     * @return 등록 성공 시 true, 실패 시 false
     */
    public boolean insertReservation(RepairReservationDto dto) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;

        String sql = "INSERT INTO repair_reservations (user_id, shop_name, res_date, res_time, services, note) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, dto.getUserId());
            pstmt.setString(2, dto.getShopName());
            pstmt.setString(3, dto.getResDate());
            pstmt.setString(4, dto.getResTime());
            pstmt.setString(5, dto.getServices());
            pstmt.setString(6, dto.getNote());

            if (pstmt.executeUpdate() == 1) {
                flag = true;
            }
        } catch (Exception e) {
            System.err.println("[RepairReservationDao] insertReservation 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    /**
     * 특정 사용자의 모든 정비 예약 내역을 최신순으로 조회합니다.
     * 마이페이지의 '나의 예약 현황' 섹션에서 사용됩니다.
     * * @param userId 조회할 사용자의 고유 번호
     * @return 예약 정보 DTO 리스트
     */
    public List<RepairReservationDto> getReservationsByUserId(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<RepairReservationDto> list = new ArrayList<>();

        String sql = "SELECT * FROM repair_reservations WHERE user_id = ? ORDER BY res_date DESC, res_time DESC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                RepairReservationDto dto = new RepairReservationDto();
                dto.setResId(rs.getInt("res_id"));
                dto.setUserId(rs.getInt("user_id"));
                dto.setShopName(rs.getString("shop_name"));
                dto.setResDate(rs.getString("res_date"));
                dto.setResTime(rs.getString("res_time"));
                dto.setServices(rs.getString("services"));
                dto.setNote(rs.getString("note"));
                dto.setStatus(rs.getString("status"));
                dto.setCreatedAt(rs.getString("created_at"));
                list.add(dto);
            }
        } catch (Exception e) {
            System.err.println("[RepairReservationDao] getReservationsByUserId 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }

    /**
     * 예약 번호를 기반으로 특정 예약을 취소(삭제) 처리합니다.
     * * @param resId 취소할 예약의 고유 번호
     * @return 취소 성공 시 true, 실패 시 false
     */
    public boolean deleteReservation(int resId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;

        String sql = "DELETE FROM repair_reservations WHERE res_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, resId);

            if (pstmt.executeUpdate() == 1) {
                flag = true;
            }
        } catch (Exception e) {
            System.err.println("[RepairReservationDao] deleteReservation 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }
}