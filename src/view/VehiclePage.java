package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*; // DB 연동 시 필요

public class VehiclePage extends JScrollPane {

    private JPanel healthGrid;
    private JLabel mLabel; 
    private int currentTotalMileage = 52340; // DB에서 조회할 현재 주행거리

    public VehiclePage() {
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(20);
        setBorder(null);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(243, 244, 246));
        container.setBorder(new EmptyBorder(40, 80, 40, 80));

        // 상단 타이틀
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
        
        // 1. 소모품 건강도 섹션
        container.add(createHealthSection());
        container.add(Box.createVerticalStrut(25));
        
        // 2. 주유 기록 섹션 
        container.add(createFuelHistorySection());
        container.add(Box.createVerticalStrut(25));
        
        // 3. 월별 주유비 통계 섹션
        container.add(createStatsSection());

        container.add(Box.createVerticalStrut(60));
        setViewportView(container);
    }

    private JPanel createHealthSection() {
        JPanel card = createBaseCard("소모품 건강도 (항목 클릭 시 수정)");
        JPanel body = (JPanel) card.getComponent(1);

        JPanel mileageBox = new JPanel(new BorderLayout());
        mileageBox.setBackground(new Color(249, 250, 251));
        mileageBox.setBorder(new CompoundBorder(new LineBorder(new Color(229, 231, 235)), new EmptyBorder(20, 25, 20, 25)));
        mileageBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        mLabel = new JLabel(); 
        mileageBox.add(mLabel, BorderLayout.WEST);
        body.add(mileageBox);
        body.add(Box.createVerticalStrut(25));

        healthGrid = new JPanel(new GridLayout(0, 2, 20, 20));
        healthGrid.setOpaque(false);
        
        refreshHealthData(); 

        body.add(healthGrid);
        body.add(Box.createVerticalStrut(25));

        JButton settingsBtn = new JButton("⚙️ 소모품 알림 기준 설정");
        settingsBtn.setPreferredSize(new Dimension(280, 50));
        settingsBtn.setBackground(new Color(37, 99, 235));
        settingsBtn.setForeground(Color.WHITE);
        settingsBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        settingsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsBtn.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            VehicleHealthSettingDialog dialog = new VehicleHealthSettingDialog((Frame) parentWindow);
            dialog.setVisible(true);
            refreshHealthData(); 
        });

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setOpaque(false);
        btnWrapper.add(settingsBtn);
        body.add(btnWrapper);
        return card;
    }

    public void refreshHealthData() {
        if (healthGrid == null || mLabel == null) return;

        /* [DB 연동 포인트: SELECT]
         * 1. Connection conn = DriverManager.getConnection(url, user, pw);
         * 2. ResultSet rsMileage = conn.createStatement().executeQuery("SELECT mileage FROM car_info");
         * 3. if(rsMileage.next()) currentTotalMileage = rsMileage.getInt(1);
         */
        
        mLabel.setText("<html><font color='gray' size='4'>현재 총 주행거리</font><br><b style='font-size:18pt; color:#1e293b;'>" 
                        + String.format("%,d", currentTotalMileage) + " km</b></html>");

        healthGrid.removeAll();

        /* [DB 연동 가이드: 반복문]
         * ResultSet rs = stmt.executeQuery("SELECT item_name, last_km, cycle FROM maintenance");
         * while(rs.next()) {
         * healthGrid.add(createHealthItem(rs.getString(1), rs.getInt(2), rs.getInt(3)));
         * }
         */
        healthGrid.add(createHealthItem("엔진 오일", 48000, 10000));
        healthGrid.add(createHealthItem("타이어", 20000, 50000));
        healthGrid.add(createHealthItem("브레이크 패드", 45000, 30000));
        healthGrid.add(createHealthItem("배터리", 10000, 60000));

        healthGrid.revalidate();
        healthGrid.repaint();
    }

    private JPanel createHealthItem(String name, int lastKm, int cycle) {
        // 자동 계산 로직
        int driven = currentTotalMileage - lastKm;
        int percent = (int) (((double) (cycle - driven) / cycle) * 100);
        percent = Math.max(0, Math.min(100, percent));

        Color statusColor = (percent <= 20) ? new Color(239, 68, 68) : (percent <= 50) ? new Color(234, 179, 8) : new Color(34, 197, 94);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(new Color(235, 237, 240)), new EmptyBorder(18, 20, 18, 20)));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window parentWindow = SwingUtilities.getWindowAncestor(VehiclePage.this);
                VehicleHealthDetailDialog dialog = new VehicleHealthDetailDialog((Frame) parentWindow, name, lastKm, cycle);
                dialog.setVisible(true);
                if (dialog.isUpdated()) refreshHealthData();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                p.setBorder(new CompoundBorder(new LineBorder(new Color(37, 99, 235)), new EmptyBorder(18, 20, 18, 20)));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                p.setBorder(new CompoundBorder(new LineBorder(new Color(235, 237, 240)), new EmptyBorder(18, 20, 18, 20)));
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
        info.setForeground(Color.GRAY);
        p.add(Box.createVerticalStrut(8)); p.add(info);

        return p;
    }

    private JPanel createFuelHistorySection() {
        JPanel card = createBaseCard("주유 기록");
        JPanel body = (JPanel) card.getComponent(1);
        JPanel gridContainer = new JPanel(new GridLayout(0, 2, 15, 15));
        gridContainer.setOpaque(false);

        // 초기 데이터 로드 (DB 연동 시 이 메서드 내부에서 RS 돌림)
        loadFuelData(gridContainer);

        body.add(gridContainer);
        body.add(Box.createVerticalStrut(25));

        JButton addBtn = new JButton("+ 새로운 주유 기록 등록");
        addBtn.setPreferredSize(new Dimension(280, 50));
        addBtn.setBackground(new Color(37, 99, 235));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            AddStationDialog addPage = new AddStationDialog((Frame) parentWindow);
            addPage.setVisible(true);		
        });

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setOpaque(false);
        btnWrapper.add(addBtn);
        body.add(btnWrapper);
        return card;
    }

    private void loadFuelData(JPanel container) {
        container.removeAll();
        // SELECT * FROM fuel_history ORDER BY date DESC LIMIT 4
        String[][] history = { { "2026-01-25", "주유소 A", "45,000원", "30L" }, { "2026-01-18", "주유소 B", "40,000원", "26L" }, { "2026-01-12", "주유소 C", "50,000원", "32L" }, { "2026-01-05", "주유소 D", "38,000원", "24L" } };
        for (String[] h : history) container.add(createFuelItem(h[0], h[1], h[2], h[3]));
    }

    private JPanel createFuelItem(String date, String station, String price, String liter) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(Color.WHITE);
        item.setBorder(new CompoundBorder(new LineBorder(new Color(241, 245, 249)), new EmptyBorder(15, 18, 15, 18)));
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 3));
        left.setOpaque(false);
        left.add(new JLabel(date)); left.add(new JLabel("<html><b>"+station+"</b></html>"));
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 3));
        right.setOpaque(false);
        JLabel p = new JLabel(price, SwingConstants.RIGHT); p.setForeground(new Color(37, 99, 235));
        right.add(p); right.add(new JLabel(liter, SwingConstants.RIGHT));
        item.add(left, BorderLayout.CENTER); item.add(right, BorderLayout.EAST);
        return item;
    }

    private JPanel createStatsSection() {
        JPanel card = createBaseCard("월별 주유비 통계");
        JPanel body = (JPanel) card.getComponent(1);

        int[] monthlyExpenses = { 250000, 285000, 320000, 305000, 295000, 318000 };
        String[] months = { "1월", "2월", "3월", "4월", "5월", "6월" };

        // 커스텀 차트 복구
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(); int h = getHeight() - 40;
                int leftMargin = 80; int chartW = w - 160; int chartH = h - 50;
                for (int i = 0; i <= 4; i++) {
                    int y = h - 20 - (i * chartH / 4);
                    g2.setColor(new Color(235, 238, 242)); g2.drawLine(leftMargin, y, leftMargin + chartW, y);
                    g2.setColor(new Color(148, 163, 184)); g2.drawString(String.format("%,d", i * 100000), 10, y + 5);
                }
                int barWidth = 70; int barSpace = chartW / months.length;
                for (int i = 0; i < monthlyExpenses.length; i++) {
                    int x = leftMargin + (i * barSpace) + (barSpace - barWidth) / 2;
                    int barHeight = (int) ((double) monthlyExpenses[i] / 400000 * chartH);
                    int y = h - 20 - barHeight;
                    g2.setPaint(new GradientPaint(x, y, new Color(96, 165, 250), x, h - 20, new Color(37, 99, 235)));
                    g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);
                    g2.setColor(new Color(30, 64, 175)); g2.drawString(String.format("%,d", monthlyExpenses[i]), x, y - 10);
                    g2.setColor(new Color(71, 85, 105)); g2.drawString(months[i], x + 20, h + 18);
                }
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(0, 320));
        body.add(chartPanel);
        body.add(Box.createVerticalStrut(20));

        // 하단 요약 카드 복구
        JPanel infoGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        infoGrid.setOpaque(false);
        infoGrid.add(createInfoCard("평균", "291,667원"));
        infoGrid.add(createInfoCard("최고", "320,000원"));
        infoGrid.add(createInfoCard("최저", "250,000원"));
        infoGrid.add(createInfoCard("총액", "1,750,000원"));
        body.add(infoGrid);
        return card;
    }

    private JPanel createInfoCard(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 5));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(new Color(230, 235, 240), 1), new EmptyBorder(15, 10, 15, 10)));
        JLabel lbl = new JLabel(label, SwingConstants.CENTER); lbl.setForeground(Color.GRAY);
        JLabel val = new JLabel(value, SwingConstants.CENTER); val.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        p.add(lbl); p.add(val);
        return p;
    }

    private JPanel createBaseCard(String titleText) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(new Color(225, 228, 232), 1), new EmptyBorder(30, 35, 30, 35)));
        JLabel t = new JLabel(titleText);
        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        t.setBorder(new EmptyBorder(0, 0, 20, 0));
        p.add(t, BorderLayout.NORTH);
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        p.add(body, BorderLayout.CENTER);
        return p;
    }
}