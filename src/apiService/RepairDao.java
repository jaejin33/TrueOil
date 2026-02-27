package apiService;

import java.sql.*;
import java.util.*;
import database.DBConnectionMgr;

public class RepairDao {
    private DBConnectionMgr pool;

    public RepairDao() {
        pool = DBConnectionMgr.getInstance();
    }

    /**
     * 특정 지역(provider LIKE ?)에서 내 위치 기준 가까운 정비소 Top N 가져오기
     */
    public List<RepairDto> getNearestShops(double myLat, double myLng, String keyword, int limit) throws Exception {
        Connection conn = pool.getConnection();

        String sql = "SELECT id, name, COALESCE(addr_road, addr_jibun) AS address, lat, lng, provider, " +
                     "(6371 * acos(cos(radians(?)) * cos(radians(lat)) * " +
                     "cos(radians(lng) - radians(?)) + sin(radians(?)) * sin(radians(lat)))) AS distance " +
                     "FROM repair_stations WHERE provider LIKE ? " +
                     "ORDER BY distance ASC LIMIT ?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDouble(1, myLat);
        ps.setDouble(2, myLng);
        ps.setDouble(3, myLat);
        ps.setString(4, "%" + keyword + "%");
        ps.setInt(5, limit);

        ResultSet rs = ps.executeQuery();
        List<RepairDto> list = new ArrayList<>();

        while (rs.next()) {
            list.add(new RepairDto(
                rs.getString("name"),
                rs.getString("address"),
                rs.getDouble("lat"),
                rs.getDouble("lng"),
                rs.getDouble("distance")
            ));
        }

        pool.freeConnection(conn, ps, rs);
        return list;
    }
}
