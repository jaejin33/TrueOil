package view;

import javax.swing.*;
import javax.swing.border.*;

import fuel.FuelController;
import fuel.FuelService;
import fuel.dto.FuelLogDto;
import fuel.dto.FuelStatsDto;
import maintenance.MaintenanceService;
import maintenance.dto.MaintenanceStatusDto;
import user.SessionManager;
import user.UserDao;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private String[] months = new String[6];
    private String[] monthQueries = new String[6];
    private MaintenanceService maintenanceService = new MaintenanceService();
    private UserDao userDao = new UserDao();
    private FuelController fuelController = new FuelController();

    public VehiclePage() {
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(20);
        setBorder(null);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(COLOR_BG_GRAY);
        container.setBorder(new EmptyBorder(30, 60, 30, 60));

        JLabel title = new JLabel("차량 관리 / 차계부");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setForeground(COLOR_TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(title);
        container.add(Box.createVerticalStrut(25)); 
        JPanel s1 = createHealthSection();
        s1.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(s1);
        
        container.add(Box.createVerticalStrut(25));
        
        JPanel s2 = createFuelHistorySection();
        s2.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(s2);
        
        container.add(Box.createVerticalStrut(25));
        
        JPanel s3 = createStatsSection();
        s3.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(s3);

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

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnWrapper.setOpaque(false);

        JButton settingsBtn = new JButton("⚙️ 소모품 기준 설정");
        settingsBtn.setPreferredSize(new Dimension(220, 50));
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

        JButton historyAllBtn = new JButton("📋 전체 교체 이력");
        historyAllBtn.setPreferredSize(new Dimension(220, 50));
        historyAllBtn.setBackground(Color.WHITE);
        historyAllBtn.setForeground(COLOR_PRIMARY);
        historyAllBtn.setBorder(new LineBorder(COLOR_PRIMARY, 2));
        historyAllBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        historyAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        historyAllBtn.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            new VehicleHealthHistoryDialog((Frame) parentWindow, "소모품 전체").setVisible(true);
        });

        btnWrapper.add(settingsBtn);
        btnWrapper.add(historyAllBtn);
        body.add(btnWrapper);
        return card;
    }

    /**
     * DB로부터 사용자의 최신 주행거리와 소모품 건강도 데이터를 가져와 화면을 갱신합니다.
     */
    public void refreshHealthData() {
        // 1. 컴포넌트 유효성 검사 (초기화 전 호출 방지)
        if (healthGrid == null || mLabel == null) return;

        // 2. 세션 매니저를 통한 현재 로그인 사용자 식별
        int userId = SessionManager.getUserId();
        if (userId == -1) {
            System.err.println("[오류] 로그인 세션 정보가 없습니다.");
            return; 
        }

        // 3. 데이터 로딩 (UserDao & MaintenanceService 활용)
        // 현재 총 주행거리 조회
        this.currentTotalMileage = userDao.getUserMileage(userId);
        
        // 소모품별 상세 상태 리스트 조회
        List<MaintenanceStatusDto> statusList = maintenanceService.getHealthDashboard(userId);

        // 4. UI 갱신 시작
        // 기존에 붙어있던 아이템 카드들을 모두 제거
        healthGrid.removeAll();

        // 상단 주행거리 라벨 업데이트
        mLabel.setText("<html><font color='gray' size='4'>현재 총 주행거리</font><br><b style='font-size:18pt; color:#1e293b;'>" 
                        + String.format("%,d", currentTotalMileage) + " km</b></html>");

        // 5. 소모품 리스트 동적 생성 (그리드에 카드 추가)
        if (statusList != null && !statusList.isEmpty()) {
            for (MaintenanceStatusDto item : statusList) {
                // DB 데이터(DTO)를 UI 컴포넌트(JPanel 카드)로 변환하여 추가
                // createHealthItem(아이템명, 마지막교체거리, 교체주기)
                healthGrid.add(createHealthItem(
                	item.getItemId(),
                    item.getItemName(), 
                    item.getLastReplaceMileage(), 
                    item.getCycleMileage()
                ));
            }
        } else {
            // 소모품 데이터가 아예 없는 경우 (비정상 가입 등)
            JLabel emptyLabel = new JLabel("등록된 소모품 데이터가 없습니다.");
            emptyLabel.setForeground(COLOR_TEXT_MUTED);
            healthGrid.add(emptyLabel);
        }

        // 6. Swing 화면 새로고침 (반드시 호출해야 변경사항이 보임)
        healthGrid.revalidate();
        healthGrid.repaint();
        
        System.out.println("[대시보드] 유저 ID " + userId + "의 데이터를 성공적으로 불러왔습니다.");
    }
    private JPanel createHealthItem(int itemId, String name, int lastKm, int cycle) {
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
                VehicleHealthDetailDialog dialog = new VehicleHealthDetailDialog((Frame) parentWindow, itemId, name, lastKm, cycle);
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
            AddFuelLogDialog addPage = new AddFuelLogDialog((Frame) parentWindow);
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
        
        // 기존 UI 초기화
        fuelGridContainer.removeAll();

        // 세션에서 유저 ID 가져오기
        int userId = SessionManager.getUserId();
        if (userId == -1) return;

        /** [DB 연동 포인트 3: 주유 이력 조회] */
        // 최신 4개 혹은 6개 정도의 주유 기록을 가져옵니다.
        List<FuelLogDto> fuelList = fuelController.getRecentHistory(userId);

        if (fuelList != null && !fuelList.isEmpty()) {
            for (fuel.dto.FuelLogDto log : fuelList) {
                // 가격 포맷 (예: 50,000원)
                String priceStr = String.format("%,d원", log.getFuelPrice());
                // 리터 포맷 (예: 35.5L)
                String literStr = String.format("%.1fL", log.getFuelAmount());
                
                // UI 아이템 생성 및 추가
                fuelGridContainer.add(createFuelItem(
                    log.getFuelDate(), 
                    log.getStationName(), 
                    priceStr, 
                    literStr
                ));
            }
        } else {
            // 데이터가 없을 때 표시
            JLabel emptyLabel = new JLabel("주유 기록이 없습니다.");
            emptyLabel.setForeground(COLOR_TEXT_MUTED);
            fuelGridContainer.add(emptyLabel);
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
                
                int w = getWidth(); 
                int h = getHeight() - 40;
                int leftMargin = 80; 
                int chartW = w - 160; 
                int chartH = h - 50;

                int maxDataValue = 100000; 
                for (int exp : monthlyExpenses) {
                    if (exp > maxDataValue) maxDataValue = exp;
                }

                int yUnit = maxDataValue / 4;

                for (int i = 0; i <= 4; i++) {
                    int currentYVal = yUnit * i;
                    int y = h - 20 - (int)((double)currentYVal / maxDataValue * chartH);
                    
                    g2.setColor(COLOR_BORDER_LIGHT); 
                    g2.drawLine(leftMargin, y, leftMargin + chartW, y);
                    
                    g2.setColor(COLOR_TEXT_MUTED); 
                    g2.drawString(String.format("%,d", currentYVal), 10, y + 5);
                }

                int barWidth = 70; 
                int barSpace = chartW / 6;
                FontMetrics fm = g2.getFontMetrics();

                for (int i = 0; i < monthlyExpenses.length; i++) {
                    int x = leftMargin + (i * barSpace) + (barSpace - barWidth) / 2;
                    int barHeight = 0;
                    if (maxDataValue > 0) {
                        barHeight = (int) ((double) monthlyExpenses[i] / maxDataValue * chartH);
                    }
                    int y = h - 20 - barHeight;
                    g2.setPaint(new GradientPaint(x, y, COLOR_PRIMARY.brighter(), x, h - 20, COLOR_PRIMARY));
                    g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);
                    String priceText = String.format("%,d", monthlyExpenses[i]);
                    int textWidth = fm.stringWidth(priceText);
                    g2.setColor(COLOR_TEXT_DARK); 
                    g2.drawString(priceText, x + (barWidth - textWidth) / 2, y - 10);
                    String monthText = months[i] != null ? months[i] : "";
                    int monthX = x + (barWidth - fm.stringWidth(monthText)) / 2;
                    g2.drawString(monthText, monthX, h + 18);
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

        // 1. 월 쿼리 생성 (이건 화면 표시용이기도 하니 유지)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            months[i] = (cal.get(java.util.Calendar.MONTH) + 1) + "월";
            monthQueries[i] = String.format("%04d-%02d", cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1);
            cal.add(java.util.Calendar.MONTH, -1);
        }

        // 2. 가공된 데이터 통째로 가져오기
        FuelStatsDto stats = fuelController.getProcessedStats(monthQueries);
        this.monthlyExpenses = stats.getMonthlyExpenses(); // 그래프 배열 업데이트

        // 3. UI 갱신 (View의 본업)
        infoGrid.removeAll();
        infoGrid.add(createInfoCard("평균", String.format("%,d원", stats.getAvg())));
        infoGrid.add(createInfoCard("최고", String.format("%,d원", stats.getMax())));
        infoGrid.add(createInfoCard("최저", String.format("%,d원", stats.getMin())));
        infoGrid.add(createInfoCard("총액", String.format("%,d원", stats.getTotal())));

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
        if (subtitleText != null && !subtitleText.isEmpty()) {
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