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
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

// 가상의 백엔드 데이터 모델 (DTO)
class FuelStatDTO {
    String month;
    int amount;

    public FuelStatDTO(String month, int amount) {
        this.month = month;
        this.amount = amount;
    }
}

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

    // [DB 대체 로직] 가상의 백엔드 서비스
    private List<FuelStatDTO> fetchMockBackendData() {
        List<FuelStatDTO> data = new ArrayList<>();
        data.add(new FuelStatDTO("1월", 210000));
        data.add(new FuelStatDTO("2월", 185000));
        data.add(new FuelStatDTO("3월", 340000));
        data.add(new FuelStatDTO("4월", 290000));
        data.add(new FuelStatDTO("5월", 150000));
        data.add(new FuelStatDTO("6월", 500000));
        return data;
    }

    private JPanel createStatsSection() {
        JPanel card = createBaseCard("📊 월별 주유비 통계");
        JPanel body = (JPanel) card.getComponent(1);

        // 실제 DB 대신 가상 데이터 가져오기
        List<FuelStatDTO> statsList = fetchMockBackendData();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (FuelStatDTO dto : statsList) {
            dataset.addValue(dto.amount, "주유비", dto.month);
        }

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

    // 이하 기존 디자인 관련 메서드들은 동일 (생략 없이 유지)
    private void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setRangeGridlineStroke(new BasicStroke(1.0f));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        GradientPaint gp = new GradientPaint(
            0.0f, 0.0f, new Color(96, 165, 250), 
            0.0f, 0.0f, new Color(37, 99, 235)
        );
        renderer.setSeriesPaint(0, gp);
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
        rangeAxis.setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 12));
        rangeAxis.setTickLabelPaint(new Color(107, 114, 128));
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);
        rangeAxis.setNumberFormatOverride(java.text.NumberFormat.getIntegerInstance());
        rangeAxis.setUpperMargin(0.2);
    }

    private JPanel createHealthSection() {
        JPanel card = createBaseCard("🔧 소모품 건강도");
        JPanel body = (JPanel) card.getComponent(1);

        int currentTotalMileage = 50000; 
        
        Object[][] healthData = {
                {"엔진 오일", 45000, 10000},
                {"타이어", 35000, 50000},
                {"브레이크 패드", 42000, 40000},
                {"배터리", 10000, 60000}
        };

        JPanel mileageBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
            }
        };
        mileageBox.setOpaque(false);
        mileageBox.setBackground(new Color(248, 250, 252));
        mileageBox.setBorder(new CompoundBorder(
                new RoundBorder(new Color(226, 232, 240), 1, 8),
                new EmptyBorder(20, 25, 20, 25)
        ));
        mileageBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        mileageBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel mLabel = new JLabel("<html><body style='font-family:맑은 고딕;'><font color='#64748b'>현재 총 주행거리</font><br>" +
                "<b style='font-size:20pt; color:#0f172a;'>" + 
                java.text.NumberFormat.getInstance().format(currentTotalMileage) + " <small>km</small></b></body></html>");
        mileageBox.add(mLabel, BorderLayout.WEST);

        body.add(mileageBox);
        body.add(Box.createVerticalStrut(30));

        JPanel grid = new JPanel(new GridLayout(0, 2, 20, 20));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Object[] data : healthData) {
            String name = (String) data[0];
            int lastChanged = (int) data[1];
            int lifespan = (int) data[2];
            int driven = currentTotalMileage - lastChanged;
            int healthPercent = Math.max(0, 100 - (int)((double)driven / lifespan * 100));

            Color statusColor;
            if (healthPercent <= 25) statusColor = new Color(255, 107, 107);
            else if (healthPercent <= 50) statusColor = new Color(251, 191, 36);
            else if (healthPercent <= 75) statusColor = new Color(59, 130, 246);
            else statusColor = new Color(34, 197, 94);
            
            grid.add(createHealthItem(name, healthPercent, statusColor));
        }

        body.add(grid);
        return card;
    }

    private JPanel createHealthItem(String name, int value, Color color) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
                new RoundBorder(new Color(241, 245, 249), 1, 15),
                new EmptyBorder(20, 22, 20, 22)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        nameLabel.setForeground(new Color(51, 65, 85));
        JLabel valueLabel = new JLabel(value + "%");
        valueLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        valueLabel.setForeground(color);
        header.add(nameLabel, BorderLayout.WEST);
        header.add(valueLabel, BorderLayout.EAST);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(value);
        bar.setForeground(color);
        bar.setBackground(new Color(241, 245, 249));
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 10));

        p.add(header); 
        p.add(Box.createVerticalStrut(15)); 
        p.add(bar);
        return p;
    }

    private JPanel createFuelHistorySection() {
        JPanel card = createBaseCard("⛽ 주유 기록");
        JPanel body = (JPanel) card.getComponent(1);
        JPanel gridContainer = new JPanel(new GridLayout(0, 2, 15, 15));
        gridContainer.setOpaque(false);
        loadFuelData(gridContainer);
        body.add(gridContainer);
        body.add(Box.createVerticalStrut(25));

        JButton addBtn = new JButton("+ 새로운 주유 기록 등록") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        addBtn.setPreferredSize(new Dimension(280, 50));
        addBtn.setBackground(new Color(37, 99, 235));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.setOpaque(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setOpaque(false);
        btnWrapper.add(addBtn);
        body.add(btnWrapper);
        return card;
    }

    private void loadFuelData(JPanel container) {
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
        JPanel item = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setBackground(Color.WHITE);
        item.setBorder(new CompoundBorder(
                new RoundBorder(new Color(241, 245, 249), 1, 15),
                new EmptyBorder(18, 20, 18, 20)
        ));
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 5));
        left.setOpaque(false);
        JLabel dateLbl = new JLabel(date);
        dateLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        dateLbl.setForeground(new Color(148, 163, 184));
        JLabel stationLbl = new JLabel(station);
        stationLbl.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        stationLbl.setForeground(new Color(30, 41, 59));
        left.add(dateLbl); left.add(stationLbl);

        JPanel right = new JPanel(new GridLayout(2, 1, 0, 5));
        right.setOpaque(false);
        JLabel priceLbl = new JLabel(price, SwingConstants.RIGHT);
        priceLbl.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        priceLbl.setForeground(new Color(37, 99, 235));
        JLabel literLbl = new JLabel(liter, SwingConstants.RIGHT);
        literLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        literLbl.setForeground(new Color(100, 116, 139));
        right.add(priceLbl); right.add(literLbl);
        item.add(left, BorderLayout.CENTER);
        item.add(right, BorderLayout.EAST);
        return item;
    }

    private JPanel createBaseCard(String titleText) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
                new RoundBorder(new Color(232, 235, 240), 1, 15), 
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

    class RoundBorder implements Border {
        private Color color;
        private int thickness;
        private int radius;
        public RoundBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x + thickness/2f, y + thickness/2f, width - thickness, height - thickness, radius, radius));
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(thickness, thickness, thickness, thickness); }
        @Override
        public boolean isBorderOpaque() { return false; }
    }
}