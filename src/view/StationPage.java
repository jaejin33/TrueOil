package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
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

		refreshData(null); // 검색어 없는 경우 기본 호출
	}

	public void refreshData(String keyword) {

		if (gridContainer != null) {
			gridContainer.removeAll();
			try {
				String selectedFuel = fuelTypeCombo != null ? (String) fuelTypeCombo.getSelectedItem() : "휘발유";
				String selectedSort = sortCombo != null ? (String) sortCombo.getSelectedItem() : "가격순";
				String prodCd = "B027"; // 휘발유 기본
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
				String sortCode = "1"; // 가격순 기본
				if ("거리순".equals(selectedSort)) {
					sortCode = "2";
				}
				/** [API/DB POINT] 실시간 유가 데이터 수집
				 * - 대상: 오피넷(Opinet) 실시간 유가 API
				 * - 로직: 현재 위치(좌표) 혹은 검색된 지역 코드를 파라미터로 전달하여 JSON 데이터 응답 수신
				 * - 연동: 수신된 리스트를 루프 돌며 createStationItem에 값(이름, 주소, 가격, 거리) 전달
				 */
				List<apiService.ValueStationDto> stations = apiService.ValueStationService.getStations(494152, 282437,
						3000, keyword, prodCd, sortCode);

				// 3. 데이터 정렬 (거리순 or 가격순)
				for (apiService.ValueStationDto s : stations) {
					// 가격 파싱 유틸리티(parsePrice)를 사용하거나 Integer.parseInt 사용
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

		/** [API/DB POINT] 지도 연동
		 * - Naver/Kakao Static Map API 사용 시: 현재 위치 좌표를 기반으로 지도 이미지 URL 생성 및 로드
		 * - WebView(JCEF) 사용 시: 지도 API HTML 가이드를 통해 현재 위치 마커 표시
		 */
		JPanel mapBox = new JPanel(new GridBagLayout());
		mapBox.setBackground(COLOR_BORDER_LIGHT);
		mapBox.setPreferredSize(new Dimension(0, 320));
		mapBox.add(new JLabel("📍 지도 데이터 로딩 중..."));

		((JPanel) card.getComponent(1)).add(mapBox);
		return card;
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
					searchInput.setForeground(COLOR_TEXT_DARK); // 입력할 때는 진한 글씨색으로 변경
				}
			}

			@Override
			public void focusLost(FocusEvent e) {

				if (searchInput.getText().trim().isEmpty()) {
					searchInput.setForeground(COLOR_TEXT_GRAY); // 다시 회색으로 변경
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

		/** [기능 포인트] 검색 실행 로직
		 * - ActionListener를 등록하여 검색어(searchInput.getText()) 추출
		 * - 검색어를 기반으로 오피넷 API 재호출 및 refreshData() 실행으로 UI 갱신
		 */
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
			if (keyword.equals("주유소 이름을 입력하세요"))
				keyword = ""; // 힌트 텍스트 무시
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

		/** [기능 포인트] 상세 페이지 이동 및 즐겨찾기 연동
		 * - 클릭 시 해당 주유소의 고유 ID(또는 명칭)를 StationDetail 페이지로 전달
		 * - [DB 연동]: 상세 페이지 진입 시 해당 주유소가 사용자의 '즐겨찾기' 테이블에 있는지 확인 필요
		 */
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
			return Integer.MAX_VALUE; // 가격 정보가 없으면 정렬 시 맨 뒤로 밀리도록 큰 값 반환
		}
	}
}
