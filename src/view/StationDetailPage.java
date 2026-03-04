package view;

import javax.swing.*;
import javax.swing.border.*;
import apiService.AvgPrice;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.Map;
import database.LocationData;

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
	public String addr;
	private JLabel distanceValueLabel;
	private JLabel travelCostValueLabel;

	public StationDetailPage(String uniId) {

		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setBorder(null);
		getVerticalScrollBar().setUnitIncrement(20);

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBackground(COLOR_BG_GRAY);
		container.setBorder(new EmptyBorder(40, 60, 40, 60));
		container.setAlignmentX(Component.CENTER_ALIGNMENT);

		try {
			// 1. 전국 평균 유가 정보 로드
			Map<String, apiService.AvgPriceDto> avgPrices = apiService.AvgPrice.getAvgPrice();

			// 2. 오피넷 상세정보 API 호출
			String apiUrl = "https://www.opinet.co.kr/api/detailById.do?code=" + AvgPrice.apiKey + "&id=" + uniId
					+ "&out=xml";
			java.net.URL url = new java.net.URL(apiUrl);
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			java.io.InputStream is = conn.getInputStream();
			javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
			javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document doc = builder.parse(is);

			// 3. 상세 정보 및 좌표 파싱
			this.stationName = getTagValue(doc, "OS_NM");
			addr = getTagValue(doc, "NEW_ADR");
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

		this.addHierarchyListener(e -> {
			if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
				refreshMapIfNeeded();
			}
		});

		SwingUtilities.invokeLater(() -> {
			revalidate();
			repaint();
		});
	}

	private String getTagValue(org.w3c.dom.Document doc, String tag) {

		org.w3c.dom.NodeList nl = doc.getElementsByTagName(tag);
		if (nl.getLength() > 0)
			return nl.item(0).getTextContent();
		return "";
	}

	private void refreshMapIfNeeded() {

		Platform.runLater(() -> {
			if (webEngine != null) {
				String url = webEngine.getLocation();
				// 로드된 URL이 없거나 blank 상태라면 다시 로드
				if (url == null || url.isEmpty() || url.equals("about:blank")) {
					loadMapFile();
				}
			}
		});
	}

	// 맵 파일 로드 로직 분리
	private void loadMapFile() {

		try {
			String projectRoot = System.getProperty("user.dir");
			java.io.File mapFile = new java.io.File(projectRoot, "map.html");
			if (mapFile.exists()) {
				webEngine.load(mapFile.toURI().toURL().toExternalForm());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 지도 카드 생성 (JavaFX WebView 통합)
	private JPanel createMapCard(String name) {

		JPanel card = createBaseCard("📍 주유소 위치");

		// JavaFX 컨테이너 생성
		jfxPanel = new JFXPanel();
		Dimension mapDim = new Dimension(MAX_CARD_WIDTH - 80, 400);
		jfxPanel.setPreferredSize(mapDim);

		// JavaFX 스레드에서 WebView 초기화 실행
		Platform.runLater(() -> {
			WebView webView = new WebView();
			webEngine = webView.getEngine();

			webEngine.setUserAgent(
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");
			webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
				if (newState == Worker.State.SUCCEEDED) {
					if (stationX != null && !stationX.isEmpty()) {
						updateRealTimeData();
						String script = String.format(java.util.Locale.US,
								"if(typeof setCenter === 'function' && typeof addMarker === 'function'){ "
										+ "    setCenter(%s, %s); " + "    addMarker(%s, %s, '%s', '%s'); " + "}",
								stationX, stationY, stationX, stationY, stationName.replace("'", "\\'"),
								representativePrice);
						webEngine.executeScript(script);
					}
				}
			});
			jfxPanel.setScene(new Scene(webView));
			loadMapFile();
		});

		JPanel mapWrapper = new JPanel(new BorderLayout());
		mapWrapper.setBackground(COLOR_MAP_BG);
		mapWrapper.setBorder(new LineBorder(COLOR_MAP_BORDER));
		mapWrapper.add(jfxPanel);
		mapWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
		mapWrapper.setMaximumSize(mapDim);

		// 하단 버튼 구성
		JPanel btnGrid = new JPanel(new GridLayout(1, 2, 15, 0));
		btnGrid.setOpaque(false);
		btnGrid.setMaximumSize(new Dimension(MAX_CARD_WIDTH - 80, 50));
		btnGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

		JButton naviBtn = createStyledButton("네이버 지도에서 보기", COLOR_NAVER_GREEN);
		JButton routeBtn = createStyledButton("길찾기", COLOR_PRIMARY);

		naviBtn.addActionListener(e -> {
			try {
				String urlWithFixedSpace = "https://map.naver.com/v5/search/" + stationName.replace(" ", "%20");
				Desktop.getDesktop().browse(new URI(urlWithFixedSpace));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		// 길찾기
		routeBtn.addActionListener(e -> {
			LocationData startPlace = LocationData.selected;
			double sLat = (startPlace != null) ? startPlace.getLat() : 35.154176;
			double sLng = (startPlace != null) ? startPlace.getLng() : 129.033014;
			Platform.runLater(() -> {
				try {
					Object result = webEngine
							.executeScript(String.format("getLatLngFromKatech(%s, %s)", stationX, stationY));
					if (result != null) {
						String[] latlng = result.toString().split(",");
						double dLat = Double.parseDouble(latlng[0]);
						double dLng = Double.parseDouble(latlng[1]);

						new Thread(() -> {
							String jsonResponse = getRouteData(sLng, sLat, dLng, dLat);

							if (jsonResponse != null && jsonResponse.contains("\"path\"")) {
								javafx.application.Platform.runLater(() -> {
									try {
										// 1. 데이터 추출
										java.util.regex.Pattern pathPattern = java.util.regex.Pattern
												.compile("\"path\":\\s*\\[(\\[.*?\\])\\]");
										java.util.regex.Matcher pathMatcher = pathPattern.matcher(jsonResponse);

										// 2. summary 내의 duration 추출 (보안형)
										java.util.regex.Pattern durPattern = java.util.regex.Pattern
												.compile("\"summary\".*?\"duration\":\\s*(\\d+)");
										java.util.regex.Matcher durMatcher = durPattern.matcher(jsonResponse);

										String pathDataString = pathMatcher.find() ? pathMatcher.group(1) : "";

										// duration 파싱
										long durationMs = 0;
										if (durMatcher.find()) {
											durationMs = Long.parseLong(durMatcher.group(1));
										}

										long totalMinutes = Math.max(1, Math.round(durationMs / 1000.0 / 60.0));
										String finalTimeText;
										if (totalMinutes >= 60) {
											finalTimeText = (totalMinutes / 60) + "시간 " + (totalMinutes % 60) + "분";
										} else {
											finalTimeText = totalMinutes + "분";
										}

										if (pathDataString.length() > 5) {
											String script = String.format(java.util.Locale.US,
													"if(window.drawRoute){ window.drawRoute(`[%s]`, '%s'); }",
													pathDataString, finalTimeText);
											webEngine.executeScript(script);

											System.out.println(">>> 실제 duration(ms): " + durationMs);
											System.out.println(">>> 지도 표시 텍스트: " + finalTimeText);
										}
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								});
							}
						}).start();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});
		});

		btnGrid.add(naviBtn);
		btnGrid.add(routeBtn);

		card.add(mapWrapper);
		card.add(Box.createVerticalStrut(20));
		card.add(btnGrid);
		return card;
	}

	public String getRouteData(double sLng, double sLat, double eLng, double eLat) {

		String clientId = "jkwi5mphw2";
		String clientSecret = "2EOprmGxyCh8FdCDBgTUKUUvAEX5uWv0UzBwG93f";

		try {
			String apiURL = String.format(java.util.Locale.US,
					"https://maps.apigw.ntruss.com/map-direction/v1/driving?start=%f,%f&goal=%f,%f&option=trafast",
					sLng, sLat, eLng, eLat);

			java.net.URL url = new java.net.URL(apiURL);
			java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			con.setRequestProperty("x-ncp-apigw-api-key-id", clientId);
			con.setRequestProperty("x-ncp-apigw-api-key", clientSecret);
			con.setRequestProperty("Accept", "application/json");

			int responseCode = con.getResponseCode();
			System.out.println(">>> Directions 5 최종 시도 응답 코드: " + responseCode);

			java.io.InputStream inputStream = (responseCode == 200) ? con.getInputStream() : con.getErrorStream();
			java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream, "UTF-8"));

			StringBuilder response = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null)
				response.append(line);
			br.close();

			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	// --- 나머지 기존 UI 빌더 메서드 (동일) ---

	private JPanel createHeader(String name, String uniId) {

		JPanel p = new JPanel(new BorderLayout());
		p.setOpaque(false);
		p.setMaximumSize(new Dimension(MAX_CARD_WIDTH, 80));
		p.setAlignmentX(Component.CENTER_ALIGNMENT);
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
			AddFuelLogDialog dialog = new AddFuelLogDialog((Frame) parentWindow, this.stationName);
			dialog.setVisible(true);
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
		grid.setMaximumSize(new Dimension(MAX_CARD_WIDTH - 80, 100));
		grid.setAlignmentX(Component.CENTER_ALIGNMENT);
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
		priceContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

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

	private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {

		double theta = lon1 - lon2;
		double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
		dist = Math.acos(dist);
		dist = Math.toDegrees(dist);
		return dist * 60 * 1.1515 * 1.609344;
	}

	private int parsePrice(String priceStr) {

		if (priceStr == null)
			return 0;
		try {
			return Integer.parseInt(priceStr.replace(",", ""));
		} catch (NumberFormatException e) {
			return 0;
		}
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
		grid.setMaximumSize(new Dimension(MAX_CARD_WIDTH - 80, 100));
		distanceValueLabel = new JLabel("계산 중...");
		travelCostValueLabel = new JLabel("계산 중...");
		grid.setAlignmentX(Component.CENTER_ALIGNMENT);
		grid.add(createSubInfoBox("현재 위치에서 거리", distanceValueLabel));
		grid.add(createSubInfoBox("예상 이동 비용", travelCostValueLabel));
		card.add(grid);
		return card;
	}

	private void updateRealTimeData() {

		Platform.runLater(() -> {
			if (webEngine == null)
				return;
			String checkScript = String.format(java.util.Locale.US,
					"if (typeof getLatLngFromKatech === 'function') { getLatLngFromKatech(%s, %s); } else { null; }",
					stationX, stationY);
			Object result = null;
			try {
				result = webEngine.executeScript(checkScript);
			} catch (Exception e) {
				return;
			}
			if (result instanceof String && !((String) result).isEmpty()) {
				String[] latLng = ((String) result).split(",");
				double sLat = Double.parseDouble(latLng[0]);
				double sLng = Double.parseDouble(latLng[1]);

				// 거리 및 비용 계산 로직
				LocationData current = LocationData.selected;
				double distance = calculateDistance(current.getLat(), current.getLng(), sLat, sLng);
				int price = parsePrice(this.representativePrice);
				double travelCost = (distance / 12.0) * price;

				SwingUtilities.invokeLater(() -> {
					distanceValueLabel.setText(String.format("%.2f km", distance));
					travelCostValueLabel.setText(String.format("약 %,.0f원", travelCost));

					Platform.runLater(() -> {
						String mapUpdateScript = String.format(java.util.Locale.US,
								"if (typeof setCenter === 'function') { " + "    setCenter(%s, %s); "
										+ "    if (typeof addMarker === 'function') { " + "        clearMarkers(); "
										+ "        addMarker(%s, %s, '%s', '%s'); " + "    } " + "}",
								stationX, stationY, stationX, stationY, stationName.replace("'", "\\'"),
								representativePrice);

						webEngine.executeScript(mapUpdateScript);
					});
				});
			}
		});
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

	private JPanel createSubInfoBox(String title, Object valueObj) {

		JPanel p = new JPanel(new GridLayout(2, 1, 0, 5));
		p.setBackground(Color.WHITE);
		p.setBorder(new CompoundBorder(new LineBorder(COLOR_ITEM_BORDER), new EmptyBorder(15, 20, 15, 20)));
		JLabel t = new JLabel(title);
		t.setForeground(Color.GRAY);
		p.add(t);
		if (valueObj instanceof JLabel)
			p.add((JLabel) valueObj);
		else
			p.add(new JLabel(valueObj.toString()));
		return p;
	}

	private JPanel createPriceDetailBox(String type, String price, String compare, Color compareColor) {

		JPanel p = new JPanel(new GridLayout(3, 1, 0, 3));
		p.setBackground(Color.WHITE);
		Dimension boxSize = new Dimension(200, 105);
		p.setPreferredSize(boxSize);
		p.setMinimumSize(boxSize);
		p.setMaximumSize(boxSize);
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