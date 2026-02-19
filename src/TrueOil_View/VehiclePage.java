package TrueOil_View;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class VehiclePage extends JScrollPane {

    public VehiclePage() {
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(20);
        setBorder(null);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(243, 244, 246));
        container.setBorder(new EmptyBorder(40, 80, 40, 80));

        JLabel title = new JLabel("차량 관리 / 차계부");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        title.setForeground(new Color(30, 41, 59));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(title);
        container.add(Box.createVerticalStrut(30));

        container.add(createHealthSection());
        container.add(Box.createVerticalStrut(25));
        container.add(createFuelHistorySection());
        container.add(Box.createVerticalStrut(25));
        container.add(createStatsSection());

        container.add(Box.createVerticalStrut(60));
        setViewportView(container);
    }

    private JPanel createHealthSection() {
        JPanel card = createBaseCard("🔧 소모품 건강도");
        JPanel body = (JPanel) card.getComponent(1);

        // 상단 마일리지 박스 디자인 개선
        JPanel mileageBox = new JPanel(new BorderLayout());
        mileageBox.setBackground(new Color(248, 250, 252));
        mileageBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 25, 20, 25)
        ));
        mileageBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        mileageBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel mLabel = new JLabel("<html><font color='#64748b' style='font-family:맑은 고딕;'>현재 총 주행거리</font><br>" +
                "<b style='font-size:20pt; color:#0f172a; font-family:Arial;'>50,000 <small>km</small></b></html>");
        mileageBox.add(mLabel, BorderLayout.WEST);

        body.add(mileageBox);
        body.add(Box.createVerticalStrut(30));

        // 소모품 그리드
        JPanel grid = new JPanel(new GridLayout(0, 2, 20, 20));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        Object[][] healthItems = {
                {"엔진 오일", 50},
                {"타이어", 30},
                {"브레이크 패드", 45},
                {"배터리", 85}
        };

        for (Object[] item : healthItems) {
            String name = (String) item[0];
            int value = (int) item[1];
            
            // 모던 파스텔 컬러 팔레트 적용
            Color statusColor;
            if (value <= 30) statusColor = new Color(255, 107, 107);      // 위기 (Soft Red)
            else if (value <= 55) statusColor = new Color(251, 191, 36); // 주의 (Amber)
            else if (value <= 80) statusColor = new Color(59, 130, 246); // 보통 (Royal Blue)
            else statusColor = new Color(34, 197, 94);                  // 양호 (Emerald)
            
            grid.add(createHealthItem(name, value, statusColor));
        }

        body.add(grid);
        return card;
    }

    private JPanel createHealthItem(String name, int value, Color color) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
                new LineBorder(new Color(241, 245, 249), 1, true),
                new EmptyBorder(20, 22, 20, 22)
        ));

        // 항목 헤더 (이름과 백분율)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        nameLabel.setForeground(new Color(51, 65, 85));
        
        JLabel valueLabel = new JLabel(value + "%");
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valueLabel.setForeground(color); // 값의 색상을 바 색상과 통일시켜 강조

        header.add(nameLabel, BorderLayout.WEST);
        header.add(valueLabel, BorderLayout.EAST);

        // 프로그레스 바 설정
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(value);
        bar.setForeground(color);
        bar.setBackground(new Color(241, 245, 249)); // 바의 배경을 연한 회색으로
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 10));

        p.add(header); 
        p.add(Box.createVerticalStrut(15)); 
        p.add(bar);
        
        return p;
    }

    private JPanel createStatsSection() {
        JPanel card = createBaseCard("📊 월별 주유비 통계");
        JPanel body = (JPanel) card.getComponent(1);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(250000, "주유비", "1월");
        dataset.addValue(280000, "주유비", "2월");
        dataset.addValue(320000, "주유비", "3월");
        dataset.addValue(300000, "주유비", "4월");
        dataset.addValue(290000, "주유비", "5월");
        dataset.addValue(310000, "주유비", "6월");

        JFreeChart chart = ChartFactory.createBarChart(
                null, null, null, dataset,
                PlotOrientation.VERTICAL, false, true, false
        );

        customizeChart(chart);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(0, 300));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setDisplayToolTips(true);
        chartPanel.setMouseWheelEnabled(false);
        
        body.add(chartPanel);
        return card;
    }

    private void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setRangeGridlineStroke(new BasicStroke(1.0f));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        // 차트 바 컬러도 건강도 섹션과 통일감 있게 부드러운 파란색 적용
        renderer.setSeriesPaint(0, new Color(59, 130, 246));
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.3);
        renderer.setMaximumBarWidth(0.1); 
        renderer.setDrawBarOutline(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 12));
        domainAxis.setTickLabelPaint(new Color(107, 114, 128));
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
        rangeAxis.setTickLabelPaint(new Color(107, 114, 128));
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);
        rangeAxis.setNumberFormatOverride(java.text.NumberFormat.getIntegerInstance());
        rangeAxis.setUpperMargin(0.2);
    }

    private JPanel createFuelHistorySection() {
        JPanel card = createBaseCard("⛽ 주유 기록");
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
        addBtn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setOpaque(false);
        btnWrapper.add(addBtn);
        body.add(btnWrapper);

        return card;
    }

    private void loadFuelData(JPanel container) {
        container.removeAll();
        String[][] history = {
                {"2026-01-25", "주유소 A", "45,000원", "30L"},
                {"2026-01-18", "주유소 B", "40,000원", "26L"},
                {"2026-01-12", "주유소 C", "50,000원", "32L"},
                {"2026-01-05", "주유소 D", "38,000원", "24L"}
        };

        for (String[] h : history) {
            container.add(createFuelItem(h[0], h[1], h[2], h[3]));
        }
    }

    private JPanel createFuelItem(String date, String station, String price, String liter) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(Color.WHITE);
        item.setBorder(new CompoundBorder(
                new LineBorder(new Color(241, 245, 249), 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 5));
        left.setOpaque(false);
        JLabel dateLbl = new JLabel(date);
        dateLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLbl.setForeground(new Color(148, 163, 184));
        JLabel stationLbl = new JLabel(station);
        stationLbl.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        stationLbl.setForeground(new Color(30, 41, 59));
        left.add(dateLbl); left.add(stationLbl);

        JPanel right = new JPanel(new GridLayout(2, 1, 0, 5));
        right.setOpaque(false);
        JLabel priceLbl = new JLabel(price, SwingConstants.RIGHT);
        priceLbl.setFont(new Font("Arial", Font.BOLD, 16));
        priceLbl.setForeground(new Color(37, 99, 235));
        JLabel literLbl = new JLabel(liter, SwingConstants.RIGHT);
        literLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        literLbl.setForeground(new Color(100, 116, 139));
        right.add(priceLbl); right.add(literLbl);

        item.add(left, BorderLayout.CENTER);
        item.add(right, BorderLayout.EAST);

        return item;
    }

    private JPanel createBaseCard(String titleText) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        // 카드 외곽선 색상을 더 연하게 하고 라운딩 느낌 부여
        p.setBorder(new CompoundBorder(
                new LineBorder(new Color(232, 235, 240), 1, true), 
                new EmptyBorder(35, 40, 35, 40)
        ));
        
        JLabel t = new JLabel(titleText);
        t.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        t.setForeground(new Color(15, 23, 42));
        t.setBorder(new EmptyBorder(0, 0, 25, 0));
        p.add(t, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        p.add(body, BorderLayout.CENTER);
        
        return p;
    }
}