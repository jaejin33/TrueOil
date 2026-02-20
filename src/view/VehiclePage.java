package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiclePage extends JScrollPane {

    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER_DEFAULT = new Color(225, 228, 232);
    private static final Color COLOR_BORDER_LIGHT = new Color(235, 237, 240);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);
    private static final Color COLOR_WARNING = new Color(234, 179, 8);
    private static final Color COLOR_SUCCESS = new Color(34, 197, 94);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);
    private static final Color COLOR_TEXT_MUTED = new Color(120, 130, 140);

    private JPanel healthGrid;
    private JLabel mLabel; 
    private JPanel fuelGridContainer;
    private JPanel chartPanel;
    private JPanel infoGrid;
    
    private int currentTotalMileage = 0;
    private int[] monthlyExpenses = {0, 0, 0, 0, 0, 0}; 
    private String[] months = {"1월", "2월", "3월", "4월", "5월", "6월"};

    public VehiclePage() {
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(20);
        setBorder(null);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(COLOR_BG_GRAY);
        container.setBorder(new EmptyBorder(40, 80, 40, 80));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel title = new JLabel("차량 관리 / 차계부");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        titlePanel.add(Box.createHorizontalGlue());
        titlePanel.add(title);
        titlePanel.add(Box.createHorizontalGlue()); 
        container.add(titlePanel);
        container.add(Box.createVerticalStrut(30));
        
        container.add(createHealthSection());
        container.add(Box.createVerticalStrut(25));
        container.add(createFuelHistorySection());
        container.add(Box.createVerticalStrut(25));
        container.add(createStatsSection());

        container.add(Box.createVerticalStrut(60));
        setViewportView(container);

        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                refreshAllData();
            }
        });
        refreshAllData();
    }

    public void refreshAllData() {
        refreshHealthData();  
        loadFuelData();       
        refreshStatsData();   
    }

    private JPanel createHealthSection() {
        JPanel card = createBaseCard("소모품 건강도", "💡 항목을 클릭하여 상세 내용을 수정할 수 있습니다.");
        JPanel body = (JPanel) card.getComponent(1);

        JPanel mileageBox = new JPanel(new BorderLayout());
        mileageBox.setBackground(COLOR_BG_GRAY);
        mileageBox.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_DEFAULT), new EmptyBorder(20, 25, 20, 25)));
        mileageBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        mLabel = new JLabel(); 
        mileageBox.add(mLabel, BorderLayout.WEST);
        body.add(mileageBox);
        body.add(Box.createVerticalStrut(25));

        healthGrid = new JPanel(new GridLayout(0, 2, 20, 20));
        healthGrid.setOpaque(false);
        body.add(healthGrid);
        body.add(Box.createVerticalStrut(25));

        JButton settingsBtn = new JButton("⚙️ 소모품 알림 기준 설정");
        settingsBtn.setPreferredSize(new Dimension(280, 50));
        settingsBtn.setBackground(COLOR_PRIMARY);
        settingsBtn.setForeground(COLOR_CARD_BG);
        settingsBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        settingsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsBtn.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            VehicleHealthSettingDialog dialog = new VehicleHealthSettingDialog((Frame) parentWindow);
            dialog.setVisible(true);
            refreshAllData(); 
        });

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setOpaque(false);
        btnWrapper.add(settingsBtn);
        body.add(btnWrapper);
        return card;
    }

    public void refreshHealthData() {
        if (healthGrid == null || mLabel == null) return;

        /** [DB 연동 포인트 1: 총 주행거리 조회] */
        /*
        String sql = "SELECT total_mileage FROM user_vehicle WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, CurrentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) currentTotalMileage = rs.getInt("total_mileage");
        } catch (SQLException e) { e.printStackTrace(); }
        */
        currentTotalMileage = 52340; 

        mLabel.setText("<html><font color='gray' size='4'>현재 총 주행거리</font><br><b style='font-size:18pt; color:#1e293b;'>" 
                        + String.format("%,d", currentTotalMileage) + " km</b></html>");

        healthGrid.removeAll();

        /** [DB 연동 포인트 2: 소모품 현황 리스트 조회] */
        /*
        String sql = "SELECT item_name, last_replace_km, change_cycle FROM vehicle_health WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, CurrentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                healthGrid.add(createHealthItem(rs.getString("item_name"), rs.getInt("last_replace_km"), rs.getInt("change_cycle")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        */
        healthGrid.add(createHealthItem("엔진 오일", 48000, 10000));
        healthGrid.add(createHealthItem("타이어", 20000, 50000));
        healthGrid.add(createHealthItem("브레이크 패드", 45000, 30000));
        healthGrid.add(createHealthItem("배터리", 10000, 60000));

        healthGrid.revalidate();
        healthGrid.repaint();
    }

    private JPanel createHealthItem(String name, int lastKm, int cycle) {
        int driven = currentTotalMileage - lastKm;
        int percent = (int) (((double) (cycle - driven) / cycle) * 100);
        percent = Math.max(0, Math.min(100, percent));

        Color statusColor = (percent <= 20) ? COLOR_DANGER : 
                            (percent <= 50) ? COLOR_WARNING : COLOR_SUCCESS;

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COLOR_CARD_BG);
        p.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_LIGHT), new EmptyBorder(18, 20, 18, 20)));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window parentWindow = SwingUtilities.getWindowAncestor(VehiclePage.this);
                VehicleHealthDetailDialog dialog = new VehicleHealthDetailDialog((Frame) parentWindow, name, lastKm, cycle);
                dialog.setVisible(true);
                if (dialog.isUpdated()) refreshAllData(); 
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                p.setBorder(new CompoundBorder(new LineBorder(COLOR_PRIMARY), new EmptyBorder(18, 20, 18, 20)));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                p.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_LIGHT), new EmptyBorder(18, 20, 18, 20)));
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(new JLabel("<html><b>"+name+"</b></html>"), BorderLayout.WEST);
        header.add(new JLabel(percent + "%"), BorderLayout.EAST);
        
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(percent);
        bar.setForeground(statusColor);
        
        p.add(header); p.add(Box.createVerticalStrut(12)); p.add(bar);
        
        JLabel info = new JLabel("교체까지 약 " + Math.max(0, cycle - driven) + "km 남음");
        info.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        info.setForeground(COLOR_TEXT_MUTED);
        p.add(Box.createVerticalStrut(8)); p.add(info);

        return p;
    }

    private JPanel createFuelHistorySection() {
        JPanel card = createBaseCard("최근 주유 기록","");
        JPanel body = (JPanel) card.getComponent(1);
        fuelGridContainer = new JPanel(new GridLayout(0, 2, 15, 15));
        fuelGridContainer.setOpaque(false);
        body.add(fuelGridContainer);
        body.add(Box.createVerticalStrut(25));

        JButton addBtn = new JButton("+ 새로운 주유 기록 등록");
        addBtn.setPreferredSize(new Dimension(280, 50));
        addBtn.setBackground(COLOR_PRIMARY);
        addBtn.setForeground(COLOR_CARD_BG);
        addBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            AddStationDialog addPage = new AddStationDialog((Frame) parentWindow);
            addPage.setVisible(true);
            if (addPage.isUpdated()) refreshAllData(); 
        });

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setOpaque(false);
        btnWrapper.add(addBtn);
        body.add(btnWrapper);
        return card;
    }

    public void loadFuelData() {
        if (fuelGridContainer == null) return;
        fuelGridContainer.removeAll();

        /** [DB 연동 포인트 3: 주유 이력 조회] */
        /*
        String sql = "SELECT fuel_date, station_name, fuel_price, fuel_liter FROM fuel_history WHERE user_id = ? ORDER BY fuel_date DESC LIMIT 4";
        try (Connection conn = DBUtil.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, CurrentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                fuelGridContainer.add(createFuelItem(rs.getString("fuel_date"), rs.getString("station_name"), 
                                     String.format("%,d원", rs.getInt("fuel_price")), rs.getString("fuel_liter") + "L"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        */
        String[][] history = { { "2026-01-25", "주유소 A", "45,000원", "30L" }, { "2026-01-18", "주유소 B", "40,000원", "26L" },
        { "2026-01-31", "주유소 C", "50,000원", "40L" }};
        for (String[] h : history) {
            fuelGridContainer.add(createFuelItem(h[0], h[1], h[2], h[3]));
        }
        fuelGridContainer.revalidate();
        fuelGridContainer.repaint();
    }

    private JPanel createFuelItem(String date, String station, String price, String liter) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(COLOR_CARD_BG);
        item.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_LIGHT), new EmptyBorder(15, 18, 15, 18)));
        
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 3));
        left.setOpaque(false);
        left.add(new JLabel(date)); 
        left.add(new JLabel("<html><b>"+station+"</b></html>"));
        
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 3));
        right.setOpaque(false);
        JLabel p = new JLabel(price, SwingConstants.RIGHT); 
        p.setForeground(COLOR_PRIMARY);
        right.add(p); 
        right.add(new JLabel(liter, SwingConstants.RIGHT));
        
        item.add(left, BorderLayout.CENTER); 
        item.add(right, BorderLayout.EAST);
        return item;
    }

    private JPanel createStatsSection() {
        JPanel card = createBaseCard("월별 주유비 통계", "최근 6개월간의 소비 흐름");
        JPanel body = (JPanel) card.getComponent(1);

        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(); int h = getHeight() - 40;
                int leftMargin = 80; int chartW = w - 160; int chartH = h - 50;
                for (int i = 0; i <= 4; i++) {
                    int y = h - 20 - (i * chartH / 4);
                    g2.setColor(COLOR_BORDER_LIGHT); g2.drawLine(leftMargin, y, leftMargin + chartW, y);
                    g2.setColor(COLOR_TEXT_MUTED); g2.drawString(String.format("%,d", i * 100000), 10, y + 5);
                }
                int barWidth = 70; int barSpace = chartW / months.length;
                for (int i = 0; i < monthlyExpenses.length; i++) {
                    int x = leftMargin + (i * barSpace) + (barSpace - barWidth) / 2;
                    int barHeight = (int) ((double) monthlyExpenses[i] / 400000 * chartH);
                    int y = h - 20 - barHeight;
                    g2.setPaint(new GradientPaint(x, y, COLOR_PRIMARY.brighter(), x, h - 20, COLOR_PRIMARY));
                    g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);
                    g2.setColor(COLOR_TEXT_DARK); g2.drawString(String.format("%,d", monthlyExpenses[i]), x, y - 10);
                    g2.setColor(COLOR_TEXT_DARK); g2.drawString(months[i], x + 20, h + 18);
                }
            }
        };
        chartPanel.setBackground(COLOR_CARD_BG);
        chartPanel.setPreferredSize(new Dimension(0, 320));
        body.add(chartPanel);
        body.add(Box.createVerticalStrut(20));

        infoGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        infoGrid.setOpaque(false);
        body.add(infoGrid);
        return card;
    }

    public void refreshStatsData() {
        if (infoGrid == null || chartPanel == null) return;

        /** [DB 연동 포인트 4 & 5: 월별 통계 데이터 집계 조회] */
        /*
        String sql = "SELECT MONTH(fuel_date) as m, SUM(fuel_price) as s FROM fuel_history WHERE user_id = ? AND fuel_date >= DATE_SUB(NOW(), INTERVAL 6 MONTH) GROUP BY MONTH(fuel_date)";
        // 조회 결과를 monthlyExpenses 배열 및 요약 정보 변수에 할당
        */
        monthlyExpenses = new int[]{ 250000, 285000, 320000, 305000, 295000, 318000 };

        infoGrid.removeAll();
        infoGrid.add(createInfoCard("평균", "291,667원"));
        infoGrid.add(createInfoCard("최고", "320,000원"));
        infoGrid.add(createInfoCard("최저", "250,000원"));
        infoGrid.add(createInfoCard("총액", "1,750,000원"));

        chartPanel.repaint(); 
        infoGrid.revalidate();
        infoGrid.repaint();
    }

    private JPanel createInfoCard(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 5));
        p.setBackground(COLOR_CARD_BG);
        p.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_DEFAULT, 1), new EmptyBorder(15, 10, 15, 10)));
        JLabel lbl = new JLabel(label, SwingConstants.CENTER); lbl.setForeground(COLOR_TEXT_MUTED);
        JLabel val = new JLabel(value, SwingConstants.CENTER); val.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        p.add(lbl); p.add(val);
        return p;
    }

    private JPanel createBaseCard(String titleText, String subtitleText) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_CARD_BG);
        p.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_DEFAULT, 1), new EmptyBorder(30, 35, 30, 35)));
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel t = new JLabel(titleText);
        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        header.add(t);
        if (subtitleText != null) {
            JLabel s = new JLabel(subtitleText);
            s.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            s.setForeground(COLOR_TEXT_MUTED);
            header.add(s);
        }
        p.add(header, BorderLayout.NORTH);
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        p.add(body, BorderLayout.CENTER);
        return p;
    }
}