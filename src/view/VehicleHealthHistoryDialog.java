package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*; // DB 연동을 위해 추가

public class VehicleHealthHistoryDialog extends JDialog {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER_DEFAULT = new Color(225, 228, 232);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);
    private static final Color COLOR_TEXT_MUTED = new Color(120, 130, 140);

    private JPanel listWrapper; // 데이터 동적 추가를 위해 멤버 변수로 승격

    public VehicleHealthHistoryDialog(Frame parent, String itemName) {
        super(parent, itemName + " 교체 이력", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(420, 520);

        /* ===== 전체 배경 및 카드 설정 (기존 UI 유지) ===== */
        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(COLOR_BG_GRAY);
        background.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 2), new EmptyBorder(20, 20, 20, 20)));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_DEFAULT, 2), new EmptyBorder(16, 24, 24, 24)));

        /* ===== 상단 헤더 & 제목 (기존 UI 유지) ===== */
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_CARD_BG);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel closeLabel = new JLabel("✕");
        closeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        closeLabel.setForeground(Color.LIGHT_GRAY);
        closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { dispose(); }
        });
        header.add(closeLabel, BorderLayout.EAST);

        JLabel titleLabel = new JLabel(itemName + " 교체 이력");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 컬럼 헤더 ===== */
        JPanel columnHeader = new JPanel(new GridLayout(1, 3, 5, 0));
        columnHeader.setBackground(new Color(249, 250, 251));
        columnHeader.setMaximumSize(new Dimension(340, 30));
        columnHeader.setBorder(new EmptyBorder(0, 10, 0, 10));
        String[] headers = {"교체 날짜", "주행거리", "비용"};
        for (String h : headers) {
            JLabel hl = new JLabel(h, SwingConstants.CENTER);
            hl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            hl.setForeground(COLOR_TEXT_MUTED);
            columnHeader.add(hl);
        }

        /* ===== 리스트 영역 ===== */
        listWrapper = new JPanel();
        listWrapper.setLayout(new BoxLayout(listWrapper, BoxLayout.Y_AXIS));
        listWrapper.setBackground(COLOR_CARD_BG);

        JScrollPane scrollPane = new JScrollPane(listWrapper);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER_DEFAULT));
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        scrollPane.getViewport().setBackground(COLOR_CARD_BG);
        scrollPane.setMaximumSize(new Dimension(340, 270));

        // 데이터 로드 호출
        loadHistoryFromDB(itemName);

        /* ===== 하단 버튼 (기존 UI 유지) ===== */
        JButton okBtn = new JButton("확인");
        okBtn.setBackground(COLOR_PRIMARY);
        okBtn.setForeground(COLOR_CARD_BG);
        okBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        okBtn.setMaximumSize(new Dimension(340, 45));
        okBtn.addActionListener(e -> dispose());

        /* ===== 카드 조립 ===== */
        card.add(header); card.add(Box.createVerticalStrut(5));
        card.add(titleLabel); card.add(Box.createVerticalStrut(20));
        card.add(columnHeader); card.add(scrollPane);
        card.add(Box.createVerticalStrut(20)); card.add(okBtn);

        background.add(card, BorderLayout.CENTER);
        add(background);
        setLocationRelativeTo(parent);
    }

    /** [DB Point] 교체 이력을 데이터베이스에서 불러오는 메서드 */
    private void loadHistoryFromDB(String itemName) {
        listWrapper.removeAll();

        /*
         * [필요한 쿼리 예시]
         * SELECT replace_date, last_replace_km, cost 
         * FROM maintenance_history 
         * WHERE user_id = ? AND item_name = ? 
         * ORDER BY replace_date DESC;
         * * [작업 순서]
         * 1. DBUtil 등을 통해 Connection 확보
         * 2. PreparedStatement에 현재 로그인 유저 ID와 itemName(엔진오일 등) 세팅
         * 3. ResultSet 반복문을 돌며 listWrapper.add(createHistoryItem(...)) 실행
         */

        // 임시 샘플 데이터 (DB 연결 전까지 확인용)
        listWrapper.add(createHistoryItem("2026-02-10", "52,000 km", "45,000원"));
        listWrapper.add(createHistoryItem("2025-08-15", "42,000 km", "40,000원"));

        /*
        // 실제 연동 코드 구조:
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT replace_date, last_replace_km, cost FROM maintenance_history WHERE user_id = ? AND item_name = ? ORDER BY replace_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, CurrentUser.getId());
            pstmt.setString(2, itemName);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String date = rs.getString("replace_date");
                String km = String.format("%,d km", rs.getInt("last_replace_km"));
                String cost = String.format("%,d원", rs.getInt("cost"));
                listWrapper.add(createHistoryItem(date, km, cost));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // 에러 시 사용자 알림창 추가 가능
        }
        */

        listWrapper.revalidate();
        listWrapper.repaint();
    }

    private JPanel createHistoryItem(String date, String km, String cost) {
        JPanel p = new JPanel(new GridLayout(1, 3, 5, 0));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        p.setBackground(COLOR_CARD_BG);
        p.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_BG_GRAY));
        
        JLabel dLabel = new JLabel(date, SwingConstants.CENTER);
        dLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        JLabel kLabel = new JLabel(km, SwingConstants.CENTER);
        kLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        JLabel cLabel = new JLabel(cost == null || cost.equals("0원") ? "-" : cost, SwingConstants.CENTER);
        cLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cLabel.setForeground(COLOR_PRIMARY);

        p.add(dLabel); p.add(kLabel); p.add(cLabel);
        p.setBorder(new CompoundBorder(p.getBorder(), new EmptyBorder(0, 10, 0, 10)));
        
        return p;
    }
}