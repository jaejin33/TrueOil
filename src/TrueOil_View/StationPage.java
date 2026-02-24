package TrueOil_View;

import TrueOil_Utils.EnvLoader;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

// JavaFX 관련 임포트 (pom.xml 설정 필수)
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

public class StationPage extends JScrollPane {

    private final String NAVER_CLIENT_ID = EnvLoader.get("NAVER_CLIENT_ID");

    public StationPage() {
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(20);
        setBorder(null);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(243, 244, 246));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 50, 10, 50);

        JLabel title = new JLabel("주유소 찾기");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        gbc.insets = new Insets(30, 50, 20, 50);
        contentPanel.add(title, gbc);

        gbc.insets = new Insets(10, 50, 10, 50);
        contentPanel.add(createMapSection(), gbc); // 동적 지도 섹션
        contentPanel.add(createSearchFilterSection(), gbc);
        contentPanel.add(createStationListSection(), gbc);

        gbc.weighty = 1.0;
        contentPanel.add(new JPanel() {{ setOpaque(false); }}, gbc);

        setViewportView(contentPanel);
    }

    /**
     * 동적 지도를 생성하는 섹션 (JavaFX WebView 이용)
     */
    private JPanel createMapSection() {
        JPanel card = createBaseCard("🗺️ 주변 지도 확인 (동적 지도)");
        JPanel body = (JPanel) card.getComponent(1);
        
        // Swing 내부에 JavaFX를 임베딩하기 위한 패널
        final JFXPanel fxPanel = new JFXPanel();
        fxPanel.setPreferredSize(new Dimension(0, 400)); // 지도 높이 조절
        
        // JavaFX UI 업데이트는 반드시 Platform.runLater 내에서 실행
        Platform.runLater(() -> {
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            // 네이버 지도 API V3 로드 및 지도 초기화 HTML
            String htmlContent = "<html>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='initial-scale=1.0, user-scalable=no, width=device-width'>"
                + "<script type='text/javascript' src='https://openapi.map.naver.com/openapi/v3/maps.js?ncpClientId=" + NAVER_CLIENT_ID + "'></script>"
                + "<style>"
                + "  body, html, #map { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div id='map'></div>"
                + "<script>"
                + "  var map = new naver.maps.Map('map', {"
                + "    center: new naver.maps.LatLng(37.5547, 126.9706)," // 기본 위치: 서울역
                + "    zoom: 15,"
                + "    zoomControl: true,"
                + "    mapTypeControl: true"
                + "  });"
                + "  var marker = new naver.maps.Marker({"
                + "    position: new naver.maps.LatLng(37.5547, 126.9706),"
                + "    map: map,"
                + "    title: '서울역'"
                + "  });"
                + "</script>"
                + "</body></html>";

            webEngine.loadContent(htmlContent);
            Scene scene = new Scene(webView);
            fxPanel.setScene(scene);
        });

        body.add(fxPanel);
        return card;
    }

    private JPanel createSearchFilterSection() {
        JPanel card = createBaseCard("🔍 주유소 검색 및 필터");
        JPanel body = (JPanel) card.getComponent(1);

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        JTextField searchInput = new JTextField(" 주유소 이름이나 동네를 입력하세요");
        searchInput.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        searchInput.setForeground(Color.GRAY);
        searchInput.setBorder(new CompoundBorder(
            new RoundBorder(new Color(226, 232, 240), 1, 15),
            new EmptyBorder(0, 10, 0, 10)
        ));
        
        JButton searchBtn = new JButton("검색") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        searchBtn.setPreferredSize(new Dimension(100, 0));
        searchBtn.setBackground(new Color(37, 99, 235));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        searchBtn.setContentAreaFilled(false);
        searchBtn.setFocusPainted(false);
        searchBtn.setBorderPainted(false);

        searchBar.add(searchInput, BorderLayout.CENTER);
        searchBar.add(searchBtn, BorderLayout.EAST);
        
        body.add(searchBar);
        body.add(Box.createVerticalStrut(25));
        
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        filterRow.setOpaque(false);
        filterRow.setBackground(new Color(249, 250, 251));
        filterRow.setBorder(new CompoundBorder(
            new RoundBorder(new Color(226, 232, 240), 1, 15),
            new EmptyBorder(12, 15, 12, 15)
        ));
        
        filterRow.add(createFilterLabel("유종"));
        filterRow.add(createCommonCombo(new String[]{"휘발유", "경유", "LPG"}));
        filterRow.add(Box.createHorizontalStrut(15));
        filterRow.add(createFilterLabel("정렬"));
        filterRow.add(createCommonCombo(new String[]{"가격순", "거리순"}));
        
        body.add(filterRow);
        return card;
    }

    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        label.setForeground(new Color(100, 116, 139));
        return label;
    }

    private JComboBox<String> createCommonCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(120, 34));
        combo.setBorder(new RoundBorder(new Color(203, 213, 225), 1, 15));
        return combo;
    }

    private JPanel createStationListSection() {
        JPanel card = createBaseCard("📄 실시간 유가 목록");
        JPanel body = (JPanel) card.getComponent(1);
        JPanel gridContainer = new JPanel(new GridLayout(0, 2, 15, 15));
        gridContainer.setOpaque(false);

        for (int i = 0; i < 6; i++) {
            gridContainer.add(createStationItem("주유소 " + (char)('A'+i), "서울시 강남구 역삼동", 1520 + (i*10), (1.1+i) + "km"));
        }

        body.add(gridContainer);
        return card;
    }

    private JPanel createStationItem(String name, String addr, int price, String dist) {
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
            new RoundBorder(new Color(235, 237, 240), 1, 15), 
            new EmptyBorder(15, 18, 15, 18)
        ));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 5));
        info.setOpaque(false);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        nameLabel.setForeground(new Color(30, 41, 59));
        JLabel subLabel = new JLabel("<html><body style='font-family:맑은 고딕;'>" + addr + "<br>" + dist + "</body></html>");
        subLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        subLabel.setForeground(new Color(100, 116, 139));
        info.add(nameLabel);
        info.add(subLabel);

        JLabel priceLabel = new JLabel(String.format("%,d원", price), SwingConstants.RIGHT);
        priceLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        priceLabel.setForeground(new Color(37, 99, 235));

        item.add(info, BorderLayout.CENTER);
        item.add(priceLabel, BorderLayout.EAST);

        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                item.setBackground(new Color(248, 250, 252));
                item.setBorder(new CompoundBorder(new RoundBorder(new Color(37, 99, 235), 1, 15), new EmptyBorder(15, 18, 15, 18)));
            }
            public void mouseExited(MouseEvent e) { 
                item.setBackground(Color.WHITE);
                item.setBorder(new CompoundBorder(new RoundBorder(new Color(235, 237, 240), 1, 15), new EmptyBorder(15, 18, 15, 18)));
            }
        });

        return item;
    }

    private JPanel createBaseCard(String titleText) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new RoundBorder(new Color(232, 235, 240), 1, 15),
            new EmptyBorder(30, 35, 30, 35)
        ));

        JLabel label = new JLabel(titleText);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 19));
        label.setForeground(new Color(15, 23, 42));
        label.setBorder(new EmptyBorder(0, 0, 20, 0));
        card.add(label, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        card.add(body, BorderLayout.CENTER);

        return card;
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