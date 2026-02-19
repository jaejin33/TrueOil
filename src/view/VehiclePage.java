package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*; // DB 연동 시 필요

public class VehiclePage extends JScrollPane {

    // 갱신을 위해 메서드에서 직접 접근해야 하는 컴포넌트들
    private JPanel healthGrid;
    private JLabel mLabel; 

    public VehiclePage() {
        // 스크롤바 설정
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(20);
        setBorder(null);

        // 전체 컨테이너
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(243, 244, 246));
        container.setBorder(new EmptyBorder(40, 80, 40, 80));

        // 상단 타이틀 섹션
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
        
        // 각 섹션 추가 (생략 없음)
        container.add(createHealthSection());
        container.add(Box.createVerticalStrut(25));
        container.add(createFuelHistorySection());
        container.add(Box.createVerticalStrut(25));
        container.add(createStatsSection());

        container.add(Box.createVerticalStrut(60));
        setViewportView(container);
    }

    private JPanel createHealthSection() {
        JPanel card = createBaseCard("소모품 건강도");
        JPanel body = (JPanel) card.getComponent(1);

        // 주행거리 표시 박스
        JPanel mileageBox = new JPanel(new BorderLayout());
        mileageBox.setBackground(new Color(249, 250, 251));
        mileageBox.setBorder(new CompoundBorder(new LineBorder(new Color(229, 231, 235)), new EmptyBorder(20, 25, 20, 25)));
        mileageBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        mLabel = new JLabel(); // refreshHealthData()에서 텍스트 설정
        mileageBox.add(mLabel, BorderLayout.WEST);

        body.add(mileageBox);
        body.add(Box.createVerticalStrut(25));

        // 건강도 아이템들이 들어갈 그리드
        healthGrid = new JPanel(new GridLayout(0, 2, 20, 20));
        healthGrid.setOpaque(false);
        
        // 데이터 초기 로드
        refreshHealthData(); 

        body.add(healthGrid);
        body.add(Box.createVerticalStrut(25));

        // 설정 버튼
        JButton settingsBtn = new JButton("⚙️ 소모품 알림 기준 설정");
        settingsBtn.setPreferredSize(new Dimension(280, 50));
        settingsBtn.setBackground(new Color(37, 99, 235));
        settingsBtn.setForeground(Color.WHITE);
        settingsBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        settingsBtn.setFocusPainted(false);
        settingsBtn.setBorderPainted(false);
        settingsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        settingsBtn.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            VehicleHealthSettingDialog dialog = new VehicleHealthSettingDialog((Frame) parentWindow);
            
            // 모달 다이얼로그가 닫힐 때까지 대기
            dialog.setVisible(true);
            
            // 다이얼로그가 닫히면 DB 값을 다시 읽어와서 UI 갱신
            refreshHealthData(); 
        });

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setOpaque(false);
        btnWrapper.add(settingsBtn);
        body.add(btnWrapper);
        return card;
    }

    /**
     * [DB 연동 안내 및 갱신 로직]
     */
    public void refreshHealthData() {
        if (healthGrid == null || mLabel == null) return;

        /*
         * [DB 연결 순서]
         * 1. Connection conn = DriverManager.getConnection(url, user, pw);
         * 2. Statement stmt = conn.createStatement();
         * 3. ResultSet rs = stmt.executeQuery("SELECT mileage FROM car_info");
         * 4. if(rs.next()) { int km = rs.getInt(1); ... }
         */
        
        // 1. 주행거리 갱신
        int currentMileage = 50000; // 실제 DB 조회값 대입
        mLabel.setText("<html><font color='gray' size='4'>현재 총 주행거리</font><br><b style='font-size:18pt; color:#1e293b;'>" 
                        + String.format("%,d", currentMileage) + " km</b></html>");

        // 2. 건강도 아이템 갱신
        healthGrid.removeAll();

        /*
         * [색상 결정 로직 상세]
         * DB에서 소모품별 '현재수치', '위험기준', '주의기준'을 가져옵니다.
         * int current = rs.getInt("value");
         * int redLimit = rs.getInt("red_limit");
         * int yellowLimit = rs.getInt("yellow_limit");
         * * Color statusColor;
         * if (current <= redLimit) statusColor = new Color(239, 68, 68); // Red
         * else if (current <= yellowLimit) statusColor = new Color(234, 179, 8); // Yellow
         * else statusColor = new Color(34, 197, 94); // Green
         */

        // DB 조회 결과를 반복문으로 돌리며 추가하는 부분
        healthGrid.add(createHealthItem("엔진 오일", 85, new Color(34, 197, 94)));
        healthGrid.add(createHealthItem("타이어", 65, new Color(234, 179, 8)));
        healthGrid.add(createHealthItem("브레이크 패드", 40, new Color(234, 88, 12)));
        healthGrid.add(createHealthItem("배터리", 90, new Color(34, 197, 94)));

        // 컴포넌트 재배치 및 다시 그리기
        healthGrid.revalidate();
        healthGrid.repaint();
    }

    private JPanel createFuelHistorySection() {
        JPanel card = createBaseCard("주유 기록");
        JPanel body = (JPanel) card.getComponent(1);

        JPanel gridContainer = new JPanel(new GridLayout(0, 2, 15, 15));
        gridContainer.setOpaque(false);

        loadFuelData(gridContainer);

        body.add(gridContainer);
        body.add(Box.createVerticalStrut(25));

        JButton addBtn = new JButton("+ 새로운 주유 기록 등록");
        addBtn.setPreferredSize(new Dimension(280, 50));
        addBtn.setBackground(new Color(37, 99, 235));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        addBtn.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            // 주유 기록 등록 다이얼로그 연결
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
        // 실제로는 DB에서 최근 4건을 가져옴: SELECT * FROM fuel_history ORDER BY date DESC LIMIT 4
        String[][] history = { 
            { "2026-01-25", "주유소 A", "45,000원", "30L" }, 
            { "2026-01-18", "주유소 B", "40,000원", "26L" },
            { "2026-01-12", "주유소 C", "50,000원", "32L" }, 
            { "2026-01-05", "주유소 D", "38,000원", "24L" } 
        };

        for (String[] h : history) {
            container.add(createFuelItem(h[0], h[1], h[2], h[3]));
        }
    }

    private JPanel createFuelItem(String date, String station, String price, String liter) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(Color.WHITE);
        item.setBorder(new CompoundBorder(new LineBorder(new Color(241, 245, 249)), new EmptyBorder(15, 18, 15, 18)));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 3));
        left.setOpaque(false);
        JLabel dateLbl = new JLabel(date);
        dateLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        dateLbl.setForeground(Color.GRAY);
        JLabel stationLbl = new JLabel(station);
        stationLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        left.add(dateLbl); left.add(stationLbl);

        JPanel right = new JPanel(new GridLayout(2, 1, 0, 3));
        right.setOpaque(false);
        JLabel priceLbl = new JLabel(price, SwingConstants.RIGHT);
        priceLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        priceLbl.setForeground(new Color(37, 99, 235));
        JLabel literLbl = new JLabel(liter, SwingConstants.RIGHT);
        literLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        literLbl.setForeground(Color.GRAY);
        right.add(priceLbl); right.add(literLbl);

        item.add(left, BorderLayout.CENTER);
        item.add(right, BorderLayout.EAST);
        return item;
    }

    private JPanel createStatsSection() {
        JPanel card = createBaseCard("월별 주유비 통계");
        JPanel body = (JPanel) card.getComponent(1);

        int[] monthlyExpenses = { 250000, 285000, 320000, 305000, 295000, 318000 };
        String[] months = { "1월", "2월", "3월", "4월", "5월", "6월" };

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight() - 40;
                int leftMargin = 80;
                int rightMargin = 80;
                int chartW = w - leftMargin - rightMargin;
                int chartH = h - 50;
                int maxVal = 400000;

                g2.setStroke(new BasicStroke(1.0f));
                for (int i = 0; i <= 4; i++) {
                    int y = h - 20 - (i * chartH / 4);
                    g2.setColor(new Color(235, 238, 242));
                    g2.drawLine(leftMargin, y, leftMargin + chartW, y);
                    g2.setColor(new Color(148, 163, 184));
                    g2.drawString(String.format("%,d", i * 100000), 10, y + 5);
                }

                int barWidth = 70; 
                int barSpace = chartW / months.length;
                for (int i = 0; i < monthlyExpenses.length; i++) {
                    int x = leftMargin + (i * barSpace) + (barSpace - barWidth) / 2;
                    int barHeight = (int) ((double) monthlyExpenses[i] / maxVal * chartH);
                    int y = h - 20 - barHeight;

                    g2.setPaint(new GradientPaint(x, y, new Color(96, 165, 250), x, h - 20, new Color(37, 99, 235)));
                    g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

                    g2.setColor(new Color(30, 64, 175));
                    String priceText = String.format("%,d", monthlyExpenses[i]);
                    int textW = g2.getFontMetrics().stringWidth(priceText);
                    g2.drawString(priceText, x + (barWidth / 2) - (textW / 2), y - 10);

                    g2.setColor(new Color(71, 85, 105));
                    int mTextW = g2.getFontMetrics().stringWidth(months[i]);
                    g2.drawString(months[i], x + (barWidth / 2) - (mTextW / 2), h + 18);
                }
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(0, 320));
        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        body.add(chartPanel);

        body.add(Box.createVerticalStrut(20));

        // 하단 요약 카드 그리드
        JPanel infoGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        infoGrid.setOpaque(false);
        infoGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
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
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        val.setForeground(new Color(15, 23, 42));
        p.add(lbl); p.add(val);
        return p;
    }

    private JPanel createHealthItem(String name, int value, Color color) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(new Color(235, 237, 240)), new EmptyBorder(18, 20, 18, 20)));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(new JLabel(name), BorderLayout.WEST);
        header.add(new JLabel(value + "%"), BorderLayout.EAST);
        
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(value);
        bar.setForeground(color);
        
        p.add(header); 
        p.add(Box.createVerticalStrut(12)); 
        p.add(bar);
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