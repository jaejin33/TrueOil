package view;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class StationPage extends JScrollPane {
	private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
	private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
	private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
	private static final Color COLOR_TEXT_GRAY = Color.GRAY;
	private static final Color COLOR_LABEL_DARK = new Color(55, 65, 81);
	private static final Color COLOR_BORDER_LIGHT = new Color(229, 231, 235);
	private static final Color COLOR_ITEM_BORDER = new Color(235, 237, 240);
	private static final Color COLOR_HOVER_BG = new Color(248, 250, 252);

	private JPanel gridContainer;
	private JTextField searchInput;
	private JComboBox<String> fuelTypeCombo;
	private JComboBox<String> sortCombo;

	private WebView webView;
	private WebEngine webEngine;

	private double currentX = 494152;
	private double currentY = 282437;
	private JavaConnector myConnector = new JavaConnector();

	public class JavaConnector {
		public void searchStations(double x, double y) {

			System.out.println("✅ 지도가 이동했습니다! KATECH 좌표: X=" + x + ", Y=" + y);
			currentX = x;
			currentY = y;

			// UI 업데이트는 반드시 Swing 쓰레드에서 처리해야 안전합니다.
			SwingUtilities.invokeLater(() -> {
				String keyword = searchInput != null ? searchInput.getText().trim() : "";
				if (keyword.equals("주유소 이름을 입력하세요"))
					keyword = "";
				refreshData(keyword);
			});
		}
	}

	public StationPage() {

		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		getVerticalScrollBar().setUnitIncrement(20);
		setBorder(null);

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBackground(COLOR_BG_GRAY);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(10, 50, 10, 50);

		JLabel title = new JLabel("주유소 찾기");
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
		gbc.insets = new Insets(30, 50, 20, 50);
		contentPanel.add(title, gbc);

		gbc.insets = new Insets(10, 50, 10, 50);
		contentPanel.add(createMapSection(), gbc);
		contentPanel.add(createSearchFilterSection(), gbc);
		contentPanel.add(createStationListSection(), gbc);

		gbc.weighty = 1.0;
		contentPanel.add(new JPanel() {
			{
				setOpaque(false);
			}
		}, gbc);

		setViewportView(contentPanel);

		this.addHierarchyListener(e -> {
			if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
				refreshData();
			}
		});

		refreshData();
	}

	public void refreshData() {

		refreshData(null);
	}

	public void refreshData(String keyword) {

		if (gridContainer != null) {
			gridContainer.removeAll();
			try {
				String selectedFuel = fuelTypeCombo != null ? (String) fuelTypeCombo.getSelectedItem() : "휘발유";
				String selectedSort = sortCombo != null ? (String) sortCombo.getSelectedItem() : "가격순";
				String prodCd = "B027";
				switch (selectedFuel) {
				case "경유":
					prodCd = "D047";
					break;
				case "LPG":
					prodCd = "K015";
					break;
				case "고급휘발유":
					prodCd = "B034";
					break;
				}
				String sortCode = "1";
				if ("거리순".equals(selectedSort)) {
					sortCode = "2";
				}

				// API 데이터 호출
				List<apiService.ValueStationDto> stations = apiService.ValueStationService.getStations(currentX,
						currentY, 3000, keyword, prodCd, sortCode);

				// 지도 마커 업데이트
				if (webEngine != null) {
					updateMapMarkers(stations);
				}

				// 리스트 UI 업데이트
				for (apiService.ValueStationDto s : stations) {
					int price = parsePrice(s.getPrice());
					String dist = String.format("%.1fkm", s.getDistance() / 1000.0);
					gridContainer.add(createStationItem(s.getUniId(), s.getName(), price, dist));
				}

			} catch (Exception e) {
				e.printStackTrace();
				gridContainer.add(new JLabel("주유소 정보를 불러오는 데 실패했습니다."));
			}

			gridContainer.revalidate();
			gridContainer.repaint();
		}
	}

	private JPanel createMapSection() {

		JPanel card = createBaseCard("🗺️ 주변 지도 확인");
		JPanel body = (JPanel) card.getComponent(1);

		JFXPanel jfxPanel = new JFXPanel();
		jfxPanel.setPreferredSize(new Dimension(0, 400));

		Platform.runLater(() -> {
			webView = new WebView(); 
            webEngine = webView.getEngine();
			// 네이버 지도 로딩을 위한 필수 설정
			webEngine.setUserAgent(
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");

			webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    netscape.javascript.JSObject window = (netscape.javascript.JSObject) webEngine.executeScript("window");
                    
                    // 1. myConnector 연결
                    window.setMember("javaConnector", myConnector);
                    System.out.println("✅ Java-HTML 브릿지 연결 완료!");

                    // 2. 💡 [추가] 연결되자마자 지도(HTML) 화면에 성공 메시지를 띄우고, 현재 중심 좌표를 강제로 한 번 전송시킴!
                    String initScript = 
                        "if (typeof map !== 'undefined') {" +
                        "    var center = map.getCenter();" +
                        "    var tm128 = naver.maps.TransCoord.fromLatLngToTM128(center);" +
                        "    window.javaConnector.searchStations(tm128.x, tm128.y);" +
                        "}";
                    webEngine.executeScript(initScript);
                }
            });
			try {
				// 1. 파일의 실제 URL 경로를 얻어옵니다.
				// resources 폴더 내의 map.html을 찾습니다.
				java.net.URL url = getClass().getResource("/map.html");

				if (url == null) {
					// 리소스에서 못 찾을 경우 실제 물리적 경로 시도
					java.io.File file = new java.io.File("C:\\Java\\TrueOil\\map.html");
					if (file.exists()) {
						url = file.toURI().toURL();
					}
				}

				if (url != null) {
					// 2. loadContent 대신 load를 사용하여 파일 URL을 직접 호출합니다.
					// 이렇게 로드하면 WebView가 로컬 호스트 컨텍스트를 더 잘 인식합니다.
					webView.getEngine().load(url.toExternalForm());
				} else {
					webView.getEngine().loadContent("<html><body><h3>map.html 파일을 찾을 수 없습니다.</h3></body></html>",
							"text/html");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			jfxPanel.setScene(new Scene(webView));
		});

		body.add(jfxPanel);
		return card;
	}

	private void updateMapMarkers(List<apiService.ValueStationDto> stations) {

		Platform.runLater(() -> {
			if (webEngine == null)
				return;

			try {
				Object result = webEngine.executeScript("typeof isMapLoaded !== 'undefined' && isMapLoaded");
				if (result instanceof Boolean && (Boolean) result) {
					webEngine.executeScript("clearMarkers();");
					for (apiService.ValueStationDto s : stations) {
						int price = parsePrice(s.getPrice());
						// Dto에 저장된 KATECH 좌표 가져오기 (ValueStationService에서 파싱한 변수명에 맞게 호출)
						double x = s.getX();
						double y = s.getY();

						String script = String.format(java.util.Locale.US, "addMarker(%f, %f, '%s', '%s')", x, y,
								s.getName().replace("'", "\\'"), String.format("%,d", price));

						webEngine.executeScript(script);
					}
				}
			} catch (Exception e) {
				System.out.println("지도가 아직 완전히 로드되지 않아 마커 업데이트를 대기합니다.");
			}
		});
	}

	private JPanel createSearchFilterSection() {

		JPanel card = createBaseCard("🔍 주유소 검색 및 필터");
		JPanel body = (JPanel) card.getComponent(1);

		JPanel searchBar = new JPanel(new BorderLayout(10, 0));
		searchBar.setOpaque(false);
		searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

		searchInput = new JTextField("주유소 이름을 입력하세요");
		searchInput.setForeground(COLOR_TEXT_GRAY);
		searchInput.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {

				if (searchInput.getText().trim().equals("주유소 이름을 입력하세요")) {
					searchInput.setText("");
					searchInput.setForeground(COLOR_TEXT_DARK);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {

				if (searchInput.getText().trim().isEmpty()) {
					searchInput.setForeground(COLOR_TEXT_GRAY);
					searchInput.setText("주유소 이름을 입력하세요");
				}
			}
		});

		JButton searchBtn = new JButton("검색");
		searchBtn.setPreferredSize(new Dimension(100, 0));
		searchBtn.setBackground(COLOR_PRIMARY);
		searchBtn.setForeground(Color.WHITE);
		searchBtn.setFocusPainted(false);
		searchBtn.setBorderPainted(false);

		searchBtn.addActionListener(e -> {
			String keyword = searchInput.getText().trim();
			if (keyword.equals("주유소 이름을 입력하세요")) {
				keyword = "";
			}
			refreshData(keyword);
		});

		searchBar.add(searchInput, BorderLayout.CENTER);
		searchBar.add(searchBtn, BorderLayout.EAST);

		body.add(searchBar);
		body.add(Box.createVerticalStrut(20));

		JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		filterRow.setOpaque(false);

		fuelTypeCombo = new JComboBox<>(new String[] { "휘발유", "경유", "LPG", "고급휘발유" });
		sortCombo = new JComboBox<>(new String[] { "가격순", "거리순" });
		ActionListener filterListener = e -> {
			String keyword = searchInput.getText().trim();
			if (keyword.equals("주유소 이름을 입력하세요")) {
				keyword = "";
			}
			refreshData(keyword);
		};
		fuelTypeCombo.addActionListener(filterListener);
		sortCombo.addActionListener(filterListener);

		filterRow.add(new JLabel("유종: "));
		filterRow.add(fuelTypeCombo);
		filterRow.add(Box.createHorizontalStrut(15));
		filterRow.add(new JLabel("정렬: "));
		filterRow.add(sortCombo);

		body.add(filterRow);
		return card;
	}

	private JPanel createStationListSection() {

		JPanel card = createBaseCard("📄 실시간 유가 목록");
		JPanel body = (JPanel) card.getComponent(1);
		gridContainer = new JPanel(new GridLayout(0, 2, 15, 15));
		gridContainer.setOpaque(false);
		body.add(gridContainer);
		return card;
	}

	private JPanel createStationItem(String uniId, String name, int price, String dist) {

		JPanel item = new JPanel(new BorderLayout(10, 0));
		item.setBackground(Color.WHITE);
		item.setBorder(new CompoundBorder(new LineBorder(COLOR_ITEM_BORDER), new EmptyBorder(15, 15, 15, 15)));
		item.setCursor(new Cursor(Cursor.HAND_CURSOR));

		JPanel info = new JPanel(new GridLayout(2, 1, 0, 5));
		info.setOpaque(false);
		JLabel nameLabel = new JLabel(name);
		nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		JLabel subLabel = new JLabel("<html>" + "<br>" + dist + "</html>");
		subLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		subLabel.setForeground(COLOR_TEXT_GRAY);
		info.add(nameLabel);
		info.add(subLabel);

		JLabel priceLabel = new JLabel(String.format("%,d원", price), SwingConstants.RIGHT);
		priceLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		priceLabel.setForeground(COLOR_PRIMARY);

		item.add(info, BorderLayout.CENTER);
		item.add(priceLabel, BorderLayout.EAST);

		item.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				Window win = SwingUtilities.getWindowAncestor(item);
				if (win instanceof MainPage) {
					((MainPage) win).showStationDetail(uniId);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {

				item.setBackground(COLOR_HOVER_BG);
				item.setBorder(new CompoundBorder(new LineBorder(COLOR_PRIMARY), new EmptyBorder(15, 15, 15, 15)));
			}

			@Override
			public void mouseExited(MouseEvent e) {

				item.setBackground(Color.WHITE);
				item.setBorder(new CompoundBorder(new LineBorder(COLOR_ITEM_BORDER), new EmptyBorder(15, 15, 15, 15)));
			}
		});

		return item;
	}

	private JPanel createBaseCard(String titleText) {

		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(Color.WHITE);
		card.setBorder(
				new CompoundBorder(new LineBorder(COLOR_BORDER_LIGHT, 1, true), new EmptyBorder(25, 25, 25, 25)));
		JLabel label = new JLabel(titleText);
		label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		label.setForeground(COLOR_LABEL_DARK);
		label.setBorder(new EmptyBorder(0, 0, 20, 0));
		card.add(label, BorderLayout.NORTH);
		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setOpaque(false);
		card.add(body, BorderLayout.CENTER);
		return card;
	}

	private int parsePrice(String priceStr) {

		if (priceStr == null || priceStr.trim().isEmpty())
			return Integer.MAX_VALUE;
		try {
			return Integer.parseInt(priceStr.replace(",", ""));
		} catch (NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}
}