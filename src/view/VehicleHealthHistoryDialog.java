package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class VehicleHealthHistoryDialog extends JDialog {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER_DEFAULT = new Color(225, 228, 232);
    private static final Color COLOR_TEXT_MUTED = new Color(120, 130, 140);

    private JPanel listWrapper;

    public VehicleHealthHistoryDialog(Frame parent, String itemName) {
        super(parent, itemName + " 교체 이력", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(480, 520);

        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(COLOR_BG_GRAY);
        background.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 2), new EmptyBorder(20, 20, 20, 20)));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_DEFAULT, 2), new EmptyBorder(16, 24, 24, 24)));

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
        JPanel columnHeader = new JPanel(new GridLayout(1, 4, 5, 0));
        columnHeader.setBackground(new Color(249, 250, 251));
        columnHeader.setMaximumSize(new Dimension(400, 30));
        columnHeader.setBorder(new EmptyBorder(0, 10, 0, 10));
        String[] headers = {"교체 날짜", "교체 항목", "주행거리", "비용"};
        for (String h : headers) {
            JLabel hl = new JLabel(h, SwingConstants.CENTER);
            hl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            hl.setForeground(COLOR_TEXT_MUTED);
            columnHeader.add(hl);
        }

        listWrapper = new JPanel();
        listWrapper.setLayout(new BoxLayout(listWrapper, BoxLayout.Y_AXIS));
        listWrapper.setBackground(COLOR_CARD_BG);

        JScrollPane scrollPane = new JScrollPane(listWrapper);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER_DEFAULT));
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        scrollPane.getViewport().setBackground(COLOR_CARD_BG);
        scrollPane.setMaximumSize(new Dimension(400, 270));

        loadHistoryFromDB(itemName);

        JButton okBtn = new JButton("확인");
        okBtn.setBackground(COLOR_PRIMARY);
        okBtn.setForeground(COLOR_CARD_BG);
        okBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        okBtn.setMaximumSize(new Dimension(400, 45));
        okBtn.addActionListener(e -> dispose());

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

        /**
         * [필요한 쿼리 예시]
         * SELECT replace_date, item_name, last_replace_km, cost 
         * FROM maintenance_history 
         * WHERE user_id = ? 
         * AND (item_name = ? OR ? = '소모품 전체') 
         * ORDER BY replace_date DESC;
         */

        // 임시 샘플 데이터 (항목 이름 추가)
        listWrapper.add(createHistoryItem("2026-02-10", "엔진오일", "52,000 km", "45,000원"));
        listWrapper.add(createHistoryItem("2025-08-15", "타이어", "42,000 km", "120,000원"));

        /*
        // 실제 연동 코드 구조:
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT replace_date, item_name, last_replace_km, cost FROM maintenance_history " +
                         "WHERE user_id = ? AND (item_name = ? OR ? = '소모품 전체') ORDER BY replace_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "USER123"); // 실제 세션 ID
            pstmt.setString(2, itemName);
            pstmt.setString(3, itemName);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String date = rs.getString("replace_date");
                String name = rs.getString("item_name");
                String km = String.format("%,d km", rs.getInt("last_replace_km"));
                String cost = String.format("%,d원", rs.getInt("cost"));
                listWrapper.add(createHistoryItem(date, name, km, cost));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        */

        listWrapper.revalidate();
        listWrapper.repaint();
    }

    private JPanel createHistoryItem(String date, String name, String km, String cost) {
        JPanel p = new JPanel(new GridLayout(1, 4, 5, 0));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        p.setBackground(COLOR_CARD_BG);
        p.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_BG_GRAY));
        
        JLabel dLabel = new JLabel(date, SwingConstants.CENTER);
        dLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        
        JLabel nLabel = new JLabel(name, SwingConstants.CENTER);
        nLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        nLabel.setForeground(COLOR_TEXT_MUTED);

        JLabel kLabel = new JLabel(km, SwingConstants.CENTER);
        kLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        JLabel cLabel = new JLabel(cost == null || cost.equals("0원") ? "-" : cost, SwingConstants.CENTER);
        cLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cLabel.setForeground(COLOR_PRIMARY);

        p.add(dLabel); p.add(nLabel); p.add(kLabel); p.add(cLabel);
        p.setBorder(new CompoundBorder(p.getBorder(), new EmptyBorder(0, 10, 0, 10)));
        
        return p;
    }
}