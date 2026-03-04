package view;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import util.SessionManager;
import javafx.concurrent.Worker;
import database.LocationData;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import javax.swing.Timer;

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
	private JComboBox<LocationData> locationCombo;

	private WebView webView;
	private WebEngine webEngine;

	private double currentX = 496541;
	private double currentY = 283842;
	private JavaConnector myConnector = new JavaConnector();

	public class JavaConnector {
		private Timer debounceTimer;
		private static final int DEBOUNCE_DELAY = 1500; // 1.5초 대기

		public void searchStations(double x, double y) {

			if (debounceTimer != null && debounceTimer.isRunning()) {
				debounceTimer.stop();
			}
			debounceTimer = new Timer(DEBOUNCE_DELAY, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					System.out.println("✅ 지도 정지 감지! API 호출 시작: X=" + x + ", Y=" + y);
					currentX = x;
					currentY = y;

					SwingUtilities.invokeLater(() -> {
						String keyword = searchInput != null ? searchInput.getText().trim() : "";
						if (keyword.equals("주유소 이름을 입력하세요"))
							keyword = "";
						refreshData(keyword);
					});

					debounceTimer.stop();
				}
			});
			debounceTimer.setRepeats(false);
			debounceTimer.start();
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
				if (locationCombo != null) {
					locationCombo.setSelectedItem(LocationData.selected);
				}
				refreshData();
			}
		});
	}

	public void refreshData() {

		refreshData(null);
	}

	public void refreshData(String keyword) {

		gridContainer.removeAll();
		if (gridContainer != null) {
			try {
				String selectedFuel = fuelTypeCombo != null ? (String) fuelTypeCombo.getSelectedItem() : "휘발유";
				String selectedSort = sortCombo != null ? (String) sortCombo.getSelectedItem() : "가격순";
				String prodCd = "";
				switch (selectedFuel) {
				case "휘발유":
					prodCd = "B027";
					break;
				case "경유":
					prodCd = "D047";
					break;
				case "LPG":
					prodCd = "K015";
					break;
				case "고급휘발유":
					prodCd = "B034";
					break;
				case "등유":
					prodCd = "C004";
					break;
				}
				String sortCode = "1";
				if ("거리순".equals(selectedSort)) {
					sortCode = "2";
				}

				List<apiService.ValueStationDto> stations = apiService.ValueStationService.getStations(currentX,
						currentY, 2000, keyword, prodCd, sortCode);

				// 지도 마커 업데이트
				if (webEngine != null) {
					updateMapMarkers(stations);
				}

				// 리스트 UI 업데이트
				for (apiService.ValueStationDto s : stations) {
					int price = parsePrice(s.getPrice());
					String dist = String.format("%.1fkm", s.getDistance() / 1000.0);
					gridContainer.add(createStationItem(s, price, dist));
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

		JPanel card = createBaseCard("");
		JPanel body = (JPanel) card.getComponent(1);
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setOpaque(false);
		headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

		JLabel titleLabel = new JLabel("🗺️ 주변 지도 확인");
		titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		titleLabel.setForeground(COLOR_LABEL_DARK);
		headerPanel.add(titleLabel, BorderLayout.WEST);

		JPanel rightWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		rightWrapper.setOpaque(false);

		locationCombo = new JComboBox<>(LocationData.values());
		locationCombo.setSelectedItem(LocationData.selected); // 초기값 설정
		locationCombo.setPreferredSize(new Dimension(120, 30));

		locationCombo.addActionListener(e -> {
			LocationData loc = (LocationData) locationCombo.getSelectedItem();
			if (loc != null) {
				LocationData.selected = loc;
				updateLocation(loc);
			}
		});
		rightWrapper.add(new JLabel("기준 지역: "));
		rightWrapper.add(locationCombo);
		headerPanel.add(rightWrapper, BorderLayout.EAST);
		card.add(headerPanel, BorderLayout.NORTH);
		// -------------------------------------------
		JFXPanel jfxPanel = new JFXPanel();
		jfxPanel.setPreferredSize(new Dimension(0, 400));

		Platform.runLater(() -> {
			webView = new WebView();
			webEngine = webView.getEngine();
			webEngine.setUserAgent(
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");

			webEngine.getLoadWorker().stateProperty().addListener((obs, oldVal, newVal) -> {
				if (newVal == Worker.State.SUCCEEDED) {
					try {
						// 지도 모드 설정 (STATION)
						webEngine.executeScript("if(typeof setMapMode === 'function') { setMapMode('STATION'); }");

						// Java-JS 브릿지 설정
						netscape.javascript.JSObject window = (netscape.javascript.JSObject) webEngine
								.executeScript("window");
						window.setMember("javaConnector", myConnector);

						// 초기 중심점 설정
						LocationData currentLoc = LocationData.selected;
						String moveScript = String.format(java.util.Locale.US,
								"if(typeof setCenter === 'function') { setCenter(%f, %f); }", currentLoc.getX(),
								currentLoc.getY());
						webEngine.executeScript(moveScript);

						// 초기 데이터 로드
						refreshData();
						System.out.println("✅ 지도 및 브릿지 초기화 완료");
					} catch (Exception ex) {
						System.err.println("❌ 지도 초기화 스크립트 실행 실패: " + ex.getMessage());
					}
				}
			});
			// HTML 로드
			try {
				String projectRoot = System.getProperty("user.dir");
				File mapFile = new File(projectRoot, "map.html");

				if (mapFile.exists()) {
					webEngine.load(mapFile.toURI().toURL().toExternalForm());
				} else {
					webEngine.loadContent(
							"<html><body><h3 style='color:red;'>map.html 파일을 찾을 수 없습니다.</h3></body></html>");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			jfxPanel.setScene(new Scene(webView));
		});

		body.add(jfxPanel);
		return card;
	}

	private void updateLocation(LocationData loc) {

		this.currentX = loc.getX();
		this.currentY = loc.getY();

		Platform.runLater(() -> {
			if (webEngine != null) {
				String script = String.format(java.util.Locale.US,
						"if(typeof setCenter === 'function') { setCenter(%f, %f); }", loc.getLat(), loc.getLng());
				try {
					webEngine.executeScript(script);
				} catch (Exception e) {
					System.err.println("지도 함수가 아직 준비되지 않았습니다.");
				}
			}
		});

		refreshData();
	}

	private void updateMapMarkers(List<apiService.ValueStationDto> stations) {

		Platform.runLater(() -> {
			if (webEngine == null || stations.isEmpty())
				return;
			try {
				webEngine.executeScript("clearMarkers();");

				int minPrice = Integer.MAX_VALUE;
				for (apiService.ValueStationDto s : stations) {
					int p = parsePrice(s.getPrice());
					if (p < minPrice)
						minPrice = p;
				}

				String currentFuel = (String) fuelTypeCombo.getSelectedItem();

				for (apiService.ValueStationDto s : stations) {
					int price = parsePrice(s.getPrice());
					boolean isCheapest = (price == minPrice && price != Integer.MAX_VALUE);
					String script = String.format(java.util.Locale.US,
							"if(typeof addMarker === 'function') { addMarker(%f, %f, '%s', '%s', '%s', %b); }", s.getX(),
							s.getY(), s.getName().replace("'", "\\'"), String.format("%,d", price), currentFuel, isCheapest);
					webEngine.executeScript(script);
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

		fuelTypeCombo = new JComboBox<>(new String[] { "휘발유", "경유", "LPG", "고급휘발유", "등유" });
		sortCombo = new JComboBox<>(new String[] { "가격순", "거리순" });
		ActionListener filterListener = e -> {
			String keyword = searchInput.getText().trim();
			if (keyword.equals("주유소 이름을 입력하세요")) {
				keyword = "";
			}
			refreshData(keyword);
		};
		fuelTypeCombo.setSelectedItem(SessionManager.getFuelType());
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

	private JPanel createStationItem(apiService.ValueStationDto s, int price, String dist) {

		JPanel item = new JPanel(new BorderLayout(10, 0));
		item.setBackground(Color.WHITE);
		item.setBorder(new CompoundBorder(new LineBorder(COLOR_ITEM_BORDER), new EmptyBorder(15, 15, 15, 15)));
		item.setCursor(new Cursor(Cursor.HAND_CURSOR));

		JPanel info = new JPanel(new GridLayout(2, 1, 0, 5));
		info.setOpaque(false);
		JLabel nameLabel = new JLabel(s.getName());
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

				if (e.getClickCount() == 1) {
					handleStationSelection(s.getX(), s.getY(), s.getName(), String.format("%,d", price),
							(String) fuelTypeCombo.getSelectedItem());

				}

				if (e.getClickCount() == 2) {
					// [더블 클릭] 상세 정보 페이지 표시
					Window win = SwingUtilities.getWindowAncestor(item);
					if (win instanceof MainPage) {
						((MainPage) win).showStationDetail(s.getUniId());
					}
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

	// 주유소 리스트에서 항목을 클릭했을 때 실행할 로직
	public void handleStationSelection(double x, double y, String name, String price, String fuelType) {

		Platform.runLater(() -> {
			if (webEngine == null)
				return;
			try {
				webEngine.executeScript("window.focusStation('" + name + "')");

			} catch (Exception e) {
				webEngine.executeScript(String.format(java.util.Locale.US, "addMarker(%f, %f, '%s', '%s','%s');", x, y,
						name.replace("'", "\\'"), price, fuelType));
			}
		});
	}
}