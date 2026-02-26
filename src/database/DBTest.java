package database;

import java.sql.Connection;

public class DBTest {
    public static void main(String[] args) {
        // 1. 매니저 객체 가져오기
        DBConnectionMgr pool = DBConnectionMgr.getInstance();
        Connection con = null;

        try {
            // 2. 연결 시도
            con = pool.getConnection();
            
            if (con != null) {
                System.out.println("DB 연결 성공!");
                System.out.println("연결된 DB 정보: " + con.getMetaData().getURL());
            }
        } catch (Exception e) {
            System.err.println("DB 연결 실패");
            System.err.println("이유: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 3. 연결 반납
            pool.freeConnection(con);
        }
    }
}