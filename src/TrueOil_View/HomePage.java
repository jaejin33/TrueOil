package TrueOil_View;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 메인 홈 화면 클래스
 * 1. DB 연동: 사용자의 주유 통계 및 요약 정보
 * 2. API 연동: 전국 유가 브리핑 및 실시간 주유소 정보
 */
public class HomePage extends JScrollPane {

    public HomePage() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(243, 244, 246));
        container.setBorder(new EmptyBorder(30, 60, 30, 60));

        JLabel title = new JLabel("메인");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(title);
        container.add(Box.createVerticalStrut(25));

        container.add(createBriefingBox());
        container.add(Box.createVerticalStrut(25));
        container.add(createRecommendBox());
        container.add(Box.createVerticalStrut(25));
        container.add(createEfficiencyBox());
        container.add(Box.createVerticalStrut(25));
        container.add(createSummaryBox());

        setViewportView(container);
        setBorder(null);
        getVerticalScrollBar().setUnitIncrement(16);
    }

    // [섹션 1] 유가 브리핑 박스 (API 연동)
    private JPanel createBriefingBox() {
        JPanel card = createBaseCard("📈 오늘의 유가 한 줄 브리핑");
        
        // [API 연동 포인트] 
        // Opinet 등 유가 정보 API를 호출하여 전국 평균 데이터를 받아오세요.
        String avgPrice = "1,580원"; // apiResponse.getAvgPrice()
        String diffPrice = "20원";   // apiResponse.getDiff()
        String trend = "하락";       // apiResponse.getTrend() 

        JLabel content = new JLabel("오늘 전국 평균 휘발유 가격은 리터당 " + avgPrice + "원으로 지난주 대비 " + diffPrice + " " + trend + "했습니다.");
        content.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(content);
        return card;
    }

    // [섹션 2] 내 지역 추천 주유소 박스 (API 연동)
    private JPanel createRecommendBox() {
        JPanel card = createBaseCard("📍 내 지역 추천 주유소");
        
        // [API 연동 포인트] 
        // 1. DB에서 사용자 선호 지역 정보를 가져온 뒤 (Optional)
        // 2. 해당 지역의 주유소 리스트를 API로 호출하여 화면에 뿌려줍니다.
        card.add(createGasRow("TrueOil 강남 주유소", "서울시 강남구 역삼동", "1,550원", "1.1km"));
        card.add(Box.createVerticalStrut(12));
        card.add(createGasRow("Carset 논현 주유소", "서울시 강남구 논현동", "1,560원", "1.5km"));
        
        return card;
    }

    // [섹션 3] 가성비 추천 박스 (API 연동)
    private JPanel createEfficiencyBox() {
        JPanel card = createBaseCard("💰 가성비 추천");
        
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setBackground(Color.WHITE);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // [API 연동 포인트]
        // 실시간 최저가 API 데이터를 호출하여 가장 싼 곳과 가장 가까운 곳을 비교합니다.
        grid.add(createNestedBox("최저가 주유소", "주유소명 A", "1,520원/L", new Color(37, 99, 235)));
        grid.add(createNestedBox("거리 고려 추천", "주유소명 B", "1,550원/L (500m)", new Color(37, 99, 235)));
        
        card.add(grid);
        return card;
    }

    // [섹션 4] 주유비 요약 박스 (DB 연동)
    private JPanel createSummaryBox() {
        JPanel card = createBaseCard("📅 이번 달 주유비 요약");
        
        JPanel grid = new JPanel(new GridLayout(1, 4, 15, 0));
        grid.setBackground(Color.WHITE);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // [DB 연동 포인트] 
        // 로그인된 사용자의 주유 기록 테이블(FUEL_LOGS)에서 이번 달 데이터를 SELECT 하세요.
        // ex) SELECT COUNT(*), SUM(TOTAL_PRICE) FROM FUEL_LOGS WHERE USER_ID = 'hong' AND MONTH = '26-02'
        grid.add(createStatBox("총 주유 횟수", "8회", Color.DARK_GRAY));     // rs.getInt(1)
        grid.add(createStatBox("총 주유 금액", "320,000원", Color.DARK_GRAY)); // rs.getInt(2)
        grid.add(createStatBox("평균 가격", "1,560원", Color.DARK_GRAY));    // rs.getDouble(3)
        grid.add(createStatBox("지난달 대비", "-5%", new Color(22, 163, 74))); // 전월 대비 계산 로직
        
        card.add(grid);
        return card;
    }

    /* --- UI 헬퍼 메서드 (디자인 유지용) --- */
    
    private JPanel createBaseCard(String titleText) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new RoundBorder(new Color(209, 213, 219), 1, 15), 
            new EmptyBorder(25, 25, 25, 25)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(title);
        card.add(Box.createVerticalStrut(20));
        return card;
    }

    private JPanel createGasRow(String name, String addr, String price, String dist) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBackground(new Color(249, 250, 251));
        row.setBorder(new CompoundBorder(new RoundBorder(new Color(229, 231, 235), 1, 8), new EmptyBorder(15, 20, 15, 20)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(new JLabel("<html><b>" + name + "</b></html>"));
        JLabel sub = new JLabel(addr + " | " + dist);
        sub.setForeground(Color.GRAY);
        left.add(sub);

        JLabel p = new JLabel(price);
        p.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        p.setForeground(new Color(37, 99, 235));

        row.add(left, BorderLayout.WEST);
        row.add(p, BorderLayout.EAST);
        return row;
    }

    private JPanel createNestedBox(String label, String name, String val, Color valCol) {
        JPanel b = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
        b.setOpaque(false);
        b.setBackground(new Color(252, 252, 253));
        b.setBorder(new CompoundBorder(new RoundBorder(new Color(229, 231, 235), 1, 15), new EmptyBorder(15, 15, 15, 15)));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel l = new JLabel(label); l.setForeground(Color.GRAY);
        JLabel n = new JLabel(name); n.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        JLabel v = new JLabel(val); v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        v.setForeground(valCol);

        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        n.setAlignmentX(Component.LEFT_ALIGNMENT);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);

        b.add(l); b.add(Box.createVerticalStrut(5));
        b.add(n); b.add(Box.createVerticalStrut(5));
        b.add(v);
        return b;
    }

    private JPanel createStatBox(String label, String value, Color valCol) {
        JPanel b = new JPanel(new GridLayout(2, 1, 0, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        b.setOpaque(false);
        b.setBackground(new Color(252, 252, 253));
        b.setBorder(new CompoundBorder(new RoundBorder(new Color(229, 231, 235), 1, 15), new EmptyBorder(15, 10, 15, 10)));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel l = new JLabel(label, SwingConstants.CENTER); 
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        l.setForeground(Color.GRAY);
        
        JLabel v = new JLabel(value, SwingConstants.CENTER); 
        v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        v.setForeground(valCol);

        b.add(l); b.add(v);
        return b;
    }

    // 라운드 보더 클래스
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
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}