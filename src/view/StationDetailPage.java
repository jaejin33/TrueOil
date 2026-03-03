package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.Map;

// JavaFX 연동을 위한 import
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;

public class StationDetailPage extends JScrollPane {
	// 컬러 상수 정의
	private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
	private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
	private static final Color COLOR_BORDER_LIGHT = new Color(225, 228, 232);
	private static final Color COLOR_ITEM_BORDER = new Color(235, 237, 240);
	private static final Color COLOR_MAP_BG = new Color(230, 233, 237);
	private static final Color COLOR_MAP_BORDER = new Color(210, 214, 219);
	private static final Color COLOR_NAVER_GREEN = new Color(0, 199, 60);
	private static final Color COLOR_BLUE_LIGHT = new Color(59, 130, 246);
	private static final Color COLOR_GRAY_BORDER = new Color(209, 213, 219);
	private static final int MAX_CARD_WIDTH = 900;

	// 지도 관련 필드
	private JFXPanel jfxPanel;
	private WebEngine webEngine;
	private String stationX, stationY, stationName, representativePrice;

	public StationDetailPage(String uniId) {

		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setBorder(null);
		getVerticalScrollBar().setUnitIncrement(20);

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBackground(COLOR_BG_GRAY);
		container.setBorder(new EmptyBorder(40, 60, 40, 60));

		try {
			// 1. 전국 평균 유가 정보 로드
			Map<String, apiService.AvgPriceDto> avgPrices = apiService.AvgPrice.getAvgPrice();

			// 2. 오피넷 상세정보 API 호출
			String apiUrl = "https://www.opinet.co.kr/api/detailById.do?code=F260206147&id=" + uniId + "&out=xml";
			java.net.URL url = new java.net.URL(apiUrl);
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			java.io.InputStream is = conn.getInputStream();
			javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
			javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document doc = builder.parse(is);

			// 3. 상세 정보 및 좌표 파싱
			this.stationName = getTagValue(doc, "OS_NM");
			String addr = getTagValue(doc, "NEW_ADR");
			String tel = getTagValue(doc, "TEL");
			this.stationX = getTagValue(doc, "GIS_X_COOR"); // 오피넷 KATECH X
			this.stationY = getTagValue(doc, "GIS_Y_COOR"); // 오피넷 KATECH Y

			org.w3c.dom.NodeList priceList = doc.getElementsByTagName("OIL_PRICE");

			// 대표 가격 추출 (첫 번째 항목)
			if (priceList.getLength() > 0) {
				this.representativePrice = ((org.w3c.dom.Element) priceList.item(0)).getElementsByTagName("PRICE")
						.item(0).getTextContent();
			}

			// UI 구성
			container.add(createHeader(stationName, uniId));
			container.add(Box.createVerticalStrut(30));
			container.add(createBasicInfoCard(stationName, addr, tel));
			container.add(Box.createVerticalStrut(25));
			container.add(createPriceInfoCard(priceList, avgPrices));
			container.add(Box.createVerticalStrut(25));
			container.add(createDistanceCostCard());
			container.add(Box.createVerticalStrut(25));
			container.add(createMapCard(stationName)); // 지도 카드 생성
			container.add(Box.createVerticalStrut(60));

		} catch (Exception e) {
			e.printStackTrace();
			container.add(new JLabel("상세 정보를 불러오는 데 실패했습니다."));
		}

		setViewportView(container);
	}

	private String getTagValue(org.w3c.dom.Document doc, String tag) {

		org.w3c.dom.NodeList nl = doc.getElementsByTagName(tag);
		if (nl.getLength() > 0)
			return nl.item(0).getTextContent();
		return "";
	}

	// ✅ 지도 카드 생성 (JavaFX WebView 통합)
	private JPanel createMapCard(String name) {

		JPanel card = createBaseCard("📍 주유소 위치");

		// JavaFX 컨테이너 생성
		jfxPanel = new JFXPanel();
		jfxPanel.setPreferredSize(new Dimension(MAX_CARD_WIDTH - 80, 400));
		jfxPanel.setMaximumSize(new Dimension(MAX_CARD_WIDTH - 80, 400));

		// JavaFX 스레드에서 WebView 초기화 실행
		Platform.runLater(() -> {
			WebView webView = new WebView();
			webEngine = webView.getEngine();
			webEngine.setUserAgent(
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");
			try {
				// ✅ 현재 작업 디렉토리(프로젝트 루트)에서 map.html 찾기
				String projectRoot = System.getProperty("user.dir");
				java.io.File mapFile = new java.io.File(projectRoot, "map.html");

				if (mapFile.exists()) {
					String mapUrl = mapFile.toURI().toURL().toExternalForm();
					webEngine.load(mapUrl);

					webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
						if (newState == Worker.State.SUCCEEDED) {
							if (stationX != null && !stationX.isEmpty()) {
								// 소수점 좌표를 위한 Locale.US 설정 포함
								String script = String.format(java.util.Locale.US,
										"if(typeof setCenter === 'function'){ " + "setCenter(%s, %s); "
												+ "addMarker(%s, %s, '%s', '%s'); }",
										stationX, stationY, stationX, stationY, stationName.replace("'", "\\'"),
										representativePrice);

								webEngine.executeScript(script);
							}
						}
					});
				} else {
					// 💡 상위 폴더 경로 디버깅을 위한 출력
					System.err.println("❌ map.html 못 찾음. 실행 위치: " + projectRoot);
					webEngine.loadContent("<html><body><h3 style='color:red;'>map.html missing at: "
							+ mapFile.getAbsolutePath() + "</h3></body></html>");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			jfxPanel.setScene(new Scene(webView));
		});

		JPanel mapWrapper = new JPanel(new BorderLayout());
		mapWrapper.setBackground(COLOR_MAP_BG);
		mapWrapper.setBorder(new LineBorder(COLOR_MAP_BORDER));
		mapWrapper.add(jfxPanel);

		// 하단 버튼 구성
		JPanel btnGrid = new JPanel(new GridLayout(1, 2, 15, 0));
		btnGrid.setOpaque(false);
		btnGrid.setMaximumSize(new Dimension(MAX_CARD_WIDTH, 50));

		JButton naviBtn = createStyledButton("네이버 지도에서 보기", COLOR_NAVER_GREEN);
		JButton routeBtn = createStyledButton("길찾기", COLOR_PRIMARY);

		naviBtn.addActionListener(e -> {
			try {
				Desktop.getDesktop().browse(new URI("https://map.naver.com/v5/search/" + stationName));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});

		btnGrid.add(naviBtn);
		btnGrid.add(routeBtn);

		card.add(mapWrapper);
		card.add(Box.createVerticalStrut(20));
		card.add(btnGrid);
		return card;
	}

	// --- 나머지 기존 UI 빌더 메서드 (동일) ---

	private JPanel createHeader(String name, String uniId) {

		JPanel p = new JPanel(new BorderLayout());
		p.setOpaque(false);
		p.setMaximumSize(new Dimension(MAX_CARD_WIDTH, 80));
		JLabel title = new JLabel("<html><div style='line-height:1.2;'>" + "주유소 상세 정보<br>"
				+ "<span style='font-size:16px; font-weight:bold; color:#444444;'>(" + name + ")</span>"
				+ "</div></html>");
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		btnPanel.setOpaque(false);

		JButton recordBtn = createStyledButton("+ 주유 기록 추가", COLOR_PRIMARY);
		recordBtn.setPreferredSize(new Dimension(160, 40));
		recordBtn.addActionListener(e -> {
			Window parentWindow = SwingUtilities.getWindowAncestor(this);
			new AddFuelLogDialog((Frame) parentWindow).setVisible(true);
		});

		JButton backBtn = new JButton("← 뒤로가기");
		backBtn.setBackground(Color.WHITE);
		backBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		backBtn.setPreferredSize(new Dimension(120, 40));
		backBtn.addActionListener(e -> {
			Window win = SwingUtilities.getWindowAncestor(this);
			if (win instanceof MainPage)
				((MainPage) win).showStationList();
		});

		btnPanel.add(recordBtn);
		btnPanel.add(backBtn);
		p.add(title, BorderLayout.WEST);
		p.add(btnPanel, BorderLayout.EAST);
		return p;
	}

	private JPanel createBasicInfoCard(String name, String addr, String tel) {

		JPanel card = createBaseCard("🔵 기본 정보");
		JLabel stationTitle = new JLabel(name, SwingConstants.CENTER);
		stationTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
		stationTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel addrLabel = new JLabel(addr, SwingConstants.CENTER);
		addrLabel.setForeground(Color.GRAY);
		addrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
		grid.setOpaque(false);
		grid.add(createSubInfoBox("전화번호", tel.isEmpty() ? "정보없음" : tel));
		grid.add(createSubInfoBox("영업시간", "24시간"));

		card.add(stationTitle);
		card.add(Box.createVerticalStrut(10));
		card.add(addrLabel);
		card.add(Box.createVerticalStrut(25));
		card.add(grid);
		return card;
	}

	private JPanel createPriceInfoCard(org.w3c.dom.NodeList priceList, Map<String, apiService.AvgPriceDto> avgPrices) {

		JPanel card = createBaseCard("💲 유가 정보");
		JPanel priceContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
		priceContainer.setOpaque(false);

		for (int i = 0; i < priceList.getLength(); i++) {
			org.w3c.dom.Element oilPrice = (org.w3c.dom.Element) priceList.item(i);
			String prodcd = oilPrice.getElementsByTagName("PRODCD").item(0).getTextContent();
			String price = oilPrice.getElementsByTagName("PRICE").item(0).getTextContent();
			String type = mapProdcd(prodcd);

			String compareText = "평균 정보 없음";
			Color compareColor = Color.GRAY;
			if (avgPrices != null && avgPrices.containsKey(prodcd)) {
				int diff = (int) Math
						.round(Double.parseDouble(price) - Double.parseDouble(avgPrices.get(prodcd).getAvgPrice()));
				if (diff < 0) {
					compareText = String.format("평균보다 %,d원 저렴 ▼", Math.abs(diff));
					compareColor = COLOR_BLUE_LIGHT;
				} else if (diff > 0) {
					compareText = String.format("평균보다 %,d원 비쌈 ▲", diff);
					compareColor = Color.RED;
				} else {
					compareText = "평균가와 동일";
				}
			}
			priceContainer.add(createPriceDetailBox(type, price + "원", compareText, compareColor));
		}
		card.add(priceContainer);
		return card;
	}

	private String mapProdcd(String code) {

		return switch (code) {
		case "B027" -> "휘발유";
		case "D047" -> "경유";
		case "B034" -> "고급휘발유";
		case "C004" -> "등유";
		case "K015" -> "LPG";
		default -> "기타";
		};
	}

	private JPanel createDistanceCostCard() {

		JPanel card = createBaseCard("🚩 거리 / 예상 이동 비용");
		JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
		grid.setOpaque(false);
		grid.add(createSubInfoBox("현재 위치에서 거리", "1.5km"));
		grid.add(createSubInfoBox("예상 이동 비용", "약 300원 (연비 12km/L 기준)"));
		card.add(grid);
		return card;
	}

	private JPanel createBaseCard(String title) {

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBackground(Color.WHITE);
		p.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_LIGHT), new EmptyBorder(30, 40, 30, 40)));
		p.setAlignmentX(Component.CENTER_ALIGNMENT);
		p.setMaximumSize(new Dimension(MAX_CARD_WIDTH, Integer.MAX_VALUE));
		JLabel t = new JLabel(title);
		t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		t.setForeground(COLOR_PRIMARY);
		t.setAlignmentX(Component.CENTER_ALIGNMENT);
		p.add(t);
		p.add(Box.createVerticalStrut(20));
		return p;
	}

	private JPanel createSubInfoBox(String title, String value) {

		JPanel p = new JPanel(new GridLayout(2, 1, 0, 5));
		p.setBackground(Color.WHITE);
		p.setBorder(new CompoundBorder(new LineBorder(COLOR_ITEM_BORDER), new EmptyBorder(15, 20, 15, 20)));
		JLabel t = new JLabel(title);
		t.setForeground(Color.GRAY);
		JLabel v = new JLabel(value);
		v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
		p.add(t);
		p.add(v);
		return p;
	}

	private JPanel createPriceDetailBox(String type, String price, String compare, Color compareColor) {

		JPanel p = new JPanel(new GridLayout(3, 1, 0, 3));
		p.setBackground(Color.WHITE);
		p.setPreferredSize(new Dimension(200, 105));
		p.setBorder(new CompoundBorder(new LineBorder(COLOR_ITEM_BORDER), new EmptyBorder(15, 20, 15, 20)));
		JLabel t = new JLabel(type);
		t.setForeground(Color.GRAY);
		JLabel v = new JLabel(price);
		v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
		v.setForeground(COLOR_PRIMARY);
		JLabel c = new JLabel(compare);
		c.setForeground(compareColor);
		p.add(t);
		p.add(v);
		p.add(c);
		return p;
	}

	private JButton createStyledButton(String text, Color bg) {

		JButton b = new JButton(text);
		b.setBackground(bg);
		b.setForeground(Color.WHITE);
		b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}
}