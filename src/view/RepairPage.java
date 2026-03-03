package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import database.LocationData;

import apiService.RepairDao;
import apiService.RepairDto;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import reservation.RepairReservationController;

/**
 * 정비소 예약 페이지
 * DB 포인트: 정비소 정보 불러오기(GET), 예약 정보 저장하기(POST)
 */
public class RepairPage extends JScrollPane {

	private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
	private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
	private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
	private static final Color COLOR_TEXT_LIGHT = new Color(107, 114, 128);
	private static final Color COLOR_BORDER = new Color(209, 213, 219);
	private static final Color COLOR_DIVIDER = new Color(229, 231, 235);
	private static final Color COLOR_SELECTED_BG = new Color(239, 246, 255);

	private String selectedShopName = "";
	private RepairReservationController reservationController = new RepairReservationController();

	private JTextField shopDisplayField, dateField;
	private JComboBox<String> timeCombo;
	private JTextArea noteArea;
	private List<JCheckBox> serviceChecks;
	private JPanel shopListPanel;
	private JComboBox<LocationData> locationCombo;
	private WebEngine webEngine;

	// JS(지도)에서 Java로 통신하기 위한 커넥터
	public class JavaConnector {
		public void onShopClick(String shopName) {

			SwingUtilities.invokeLater(() -> {
				// ✅ 무한 피드백 루프 방지
				if (shopName != null && shopName.equals(selectedShopName)) {
					return;
				}

				selectedShopName = shopName;
				shopDisplayField.setText(" " + shopName);
				refreshShopSelection(); // 왼쪽 목록 하이라이트
				updateFormVisibility(); // 폼 활성화
			});
		}
	}

	public RepairPage() {

		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		getVerticalScrollBar().setUnitIncrement(20);
		setBorder(null);

		// ✅ StationPage 방식의 GridBagLayout 적용
		JPanel container = new JPanel(new GridBagLayout());
		container.setBackground(COLOR_BG_GRAY);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(10, 60, 10, 60);

		JLabel title = new JLabel("정비소 예약");
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
		gbc.insets = new Insets(30, 60, 20, 60);
		container.add(title, gbc);

		gbc.insets = new Insets(10, 60, 10, 60);
		container.add(createMapSection(), gbc);
		container.add(createShopSection(), gbc);
		container.add(createFormSection(), gbc);

		// 하단 여백 채우기
		gbc.weighty = 1.0;
		container.add(new JPanel() {
			{
				setOpaque(false);
			}
		}, gbc);

		setViewportView(container);

		// [이벤트] 탭 전환 시 데이터 갱신 리스너
		this.addHierarchyListener(e -> {
			if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
				if (locationCombo != null) {
					locationCombo.setSelectedItem(LocationData.selected);
				}
				refreshData();
			}
		});
		refreshData();
		updateFormVisibility();
	}

	/**
	 * 페이지 데이터 갱신 로직
	 */
	public void refreshData() {

		LocationData loc = LocationData.selected;

		if (shopListPanel == null)
			return;
		shopListPanel.removeAll();

		try {
			RepairDao dao = new RepairDao();
			// 예시: 부산진구 기준, 내 위치 (35.15, 129.03)
			List<RepairDto> shops = dao.getNearestShops(loc.getLat(), loc.getLng(), "부산", 10);
			StringBuilder jsonBuilder = new StringBuilder("[");

			for (int i = 0; i < shops.size(); i++) {
				RepairDto s = shops.get(i);
				shopListPanel.add(createShopItem(s));

				// JS 안전을 위해 따옴표와 특수문자 제거
				String safeName = s.getName().replaceAll("['\"\\\\]", "");
				jsonBuilder.append(String.format(java.util.Locale.US, "{\"name\":\"%s\", \"x\":%f, \"y\":%f}", safeName,
						s.getX(), s.getY()));
				if (i < shops.size() - 1)
					jsonBuilder.append(",");
			}
			jsonBuilder.append("]");
			String finalJson = jsonBuilder.toString();

			Platform.runLater(() -> {
				if (webEngine != null) {
					String escapedJson = finalJson.replace("\\", "\\\\").replace("'", "\\'");
					String script = String.format(
				            "if(typeof setRepairMarkers === 'function') { setRepairMarkers('%s'); }", 
				            escapedJson);
					try {
						webEngine.executeScript(script);
					} catch (Exception e) {
						System.err.println("마커 전송 실패: " + e.getMessage());
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (selectedShopName != null && !selectedShopName.isEmpty()) {
			refreshShopSelection();
		}

		shopListPanel.revalidate();
		shopListPanel.repaint();
	}

	private JPanel createMapSection() {

		// 1. 기본 카드 생성 (타이틀은 수동으로 넣기 위해 빈 값 전달)
		JPanel card = createBaseCard("");
		JPanel body = (JPanel) card.getComponent(1);

		// 2. 상단 타이틀 + 콤보박스 영역 구성 (StationPage와 동일한 레이아웃)
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		titlePanel.setOpaque(false);
		titlePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
		JLabel titleLabel = new JLabel("🛠️ 주변 정비소 위치");
		titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		titleLabel.setForeground(COLOR_TEXT_DARK);

		// 콤보박스 생성 및 설정
		locationCombo = new JComboBox<>(LocationData.values());
		locationCombo.setSelectedItem(LocationData.selected); // 전역 값으로 초기화
		locationCombo.setPreferredSize(new Dimension(120, 30));

		// 리스너: 여기서 바꿔도 전역 변수가 바뀌고 데이터가 갱신됨
		locationCombo.addActionListener(e -> {
			LocationData loc = (LocationData) locationCombo.getSelectedItem();
			if (loc != null) {
				LocationData.selected = loc;
				updateLocation(loc);
			}
		});

		titlePanel.add(locationCombo);
		card.add(titlePanel, BorderLayout.NORTH);
		JFXPanel jfxPanel = new JFXPanel();
		jfxPanel.setPreferredSize(new Dimension(0, 400));

		Platform.runLater(() -> {
			WebView webView = new WebView();
			webEngine = webView.getEngine();
			webEngine.setUserAgent(
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");

			webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
				if (newState == Worker.State.SUCCEEDED) {
					JSObject window = (JSObject) webEngine.executeScript("window");
					window.setMember("javaConnector", new JavaConnector());

					Platform.runLater(() -> {
						LocationData loc = LocationData.selected;

						String script = String.format(java.util.Locale.US,
								"if (typeof setMapMode === 'function') { setMapMode('REPAIR'); }"
										+ "if (typeof setCenter === 'function') { setCenter(%.6f, %.6f); }",
								loc.getX(), loc.getY());

						try {
							webEngine.executeScript(script);
						} catch (Exception e) {
							System.err.println("지도가 아직 완전히 준비되지 않았습니다: " + e.getMessage());
						}

						refreshData();
					});
				}
			});

			String projectRoot = System.getProperty("user.dir");
			File mapFile = new File(projectRoot, "map.html");
			if (mapFile.exists()) {
				try {
					webEngine.load(mapFile.toURI().toURL().toExternalForm());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			jfxPanel.setScene(new Scene(webView));
		});

		body.add(jfxPanel);
		return card;

	}

	private void updateLocation(LocationData loc) {

		Platform.runLater(() -> {
	        if (webEngine != null) {
	            String script = String.format(java.util.Locale.US, 
	                "if(typeof setCenter === 'function') { setCenter(%f, %f); }", 
	                loc.getLat(), loc.getLng());
	            try {
	                webEngine.executeScript(script);
	            } catch (Exception e) {
	                System.err.println("RepairPage: 지도 이동 함수 없음");
	            }
	        }
	    });
		refreshData();
	}

	private JPanel createShopSection() {

		JPanel card = createBaseCard("📍 근처 정비소");
		shopListPanel = new JPanel(new GridLayout(0, 2, 15, 15));
		shopListPanel.setOpaque(false);
		((JPanel) card.getComponent(1)).add(shopListPanel);
		return card;
	}

	private JPanel createFormSection() {

		JPanel card = createBaseCard("🔧 예약 정보 입력");
		JPanel body = (JPanel) card.getComponent(1);
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

		shopDisplayField = new JTextField();
		shopDisplayField.setEditable(false);
		shopDisplayField.setPreferredSize(new Dimension(0, 35));
		body.add(createInputGroup("선택한 정비소", shopDisplayField));
		body.add(Box.createVerticalStrut(15));

		JPanel grid = new JPanel(new GridLayout(1, 2, 15, 0));
		grid.setOpaque(false);
		grid.setAlignmentX(Component.LEFT_ALIGNMENT);
		String today = java.time.LocalDate.now().toString();
		dateField = new JTextField(today);
		timeCombo = new JComboBox<>(
				new String[] { "시간 선택", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00" });

		grid.add(createInputGroup("📅 예약 날짜", dateField));
		grid.add(createInputGroup("⏰ 예약 시간", timeCombo));
		body.add(grid);
		body.add(Box.createVerticalStrut(15));

		JPanel serviceGrid = new JPanel(new GridLayout(2, 3, 0, 5));
		serviceGrid.setOpaque(false);
		serviceChecks = new ArrayList<>();
		String[] services = { "엔진 오일 교환", "타이어 교체", "브레이크 점검", "배터리 점검", "종합 점검", "기타" };
		for (String s : services) {
			JCheckBox cb = new JCheckBox(s);
			cb.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
			cb.setOpaque(false);
			serviceChecks.add(cb);
			serviceGrid.add(cb);
		}
		body.add(createInputGroup("정비 서비스 (복수 선택 가능)", serviceGrid));
		body.add(Box.createVerticalStrut(15));

		noteArea = new JTextArea(4, 20);
		noteArea.setBorder(new LineBorder(COLOR_DIVIDER));
		JScrollPane noteScroll = new JScrollPane(noteArea);
		noteScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
		body.add(createInputGroup("요청사항", noteScroll));
		body.add(Box.createVerticalStrut(20));

		JButton submitBtn = new JButton("예약하기");
		submitBtn.setBackground(COLOR_PRIMARY);
		submitBtn.setForeground(Color.WHITE);
		submitBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
		submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

		submitBtn.addActionListener(e -> {
			if (selectedShopName == null || selectedShopName.isEmpty()) {
				JOptionPane.showMessageDialog(null, "정비소를 먼저 선택해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (timeCombo.getSelectedIndex() == 0) {
				JOptionPane.showMessageDialog(null, "예약 시간을 선택해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
				return;
			}

			StringBuilder sb = new StringBuilder();
			for (JCheckBox cb : serviceChecks) {
				if (cb.isSelected()) {
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(cb.getText());
				}
			}
			if (sb.length() == 0) {
				JOptionPane.showMessageDialog(null, "최소 하나 이상의 정비 서비스를 선택해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
				return;
			}

			boolean success = reservationController.requestReservation(selectedShopName, dateField.getText().trim(),
					timeCombo.getSelectedItem().toString(), sb.toString(), noteArea.getText().trim());

			if (success) {
				JOptionPane.showMessageDialog(null, selectedShopName + "에 예약이 성공적으로 완료되었습니다!", "예약 완료",
						JOptionPane.INFORMATION_MESSAGE);
				resetForm();
			} else {
				JOptionPane.showMessageDialog(null, "예약 처리 중 오류가 발생했습니다. 다시 시도해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
			}
		});

		body.add(submitBtn);
		return card;
	}

	/**
	 * 예약 완료 후 입력 폼과 지도 마커를 초기 상태로 되돌립니다.
	 */
	private void resetForm() {

		selectedShopName = "";
		dateField.setText(java.time.LocalDate.now().toString());
		timeCombo.setSelectedIndex(0);
		noteArea.setText("");
		for (JCheckBox cb : serviceChecks)
			cb.setSelected(false);

		updateFormVisibility();
		refreshShopSelection();

		// ✅ 예약 완료 후 지도 위의 강조된 이름표 마커를 모두 없애고 초기 상태로 되돌림
		Platform.runLater(() -> {
			try {
				if (webEngine != null) {
					// focusRepair에 빈 문자열이나 존재하지 않는 이름을 주면 모두 원상복구(점)됨
					webEngine.executeScript("if(typeof focusRepair === 'function') { focusRepair(''); }");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private JPanel createShopItem(RepairDto s) {

		JPanel item = new JPanel(new BorderLayout());
		item.setBackground(Color.WHITE);
		item.setBorder(new LineBorder(COLOR_DIVIDER, 1));
		item.setPreferredSize(new Dimension(0, 80));
		item.setCursor(new Cursor(Cursor.HAND_CURSOR));

		String distStr = String.format("%.2f km", s.getDistance());
		JLabel info = new JLabel("<html><div style='padding:10px;'><b>" + s.getName() + "</b><br>"
				+ "<font color='gray' size='3'>" + s.getAddress() + " · " + distStr + "</font></div></html>");
		item.add(info, BorderLayout.CENTER);

		item.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				// ✅ 무한 피드백 루프 방지
				if (s.getName().equals(selectedShopName)) {
					return;
				}

				selectedShopName = s.getName();
				Platform.runLater(() -> {
					try {
						if (webEngine != null) {
							String nameEscaped = s.getName().replace("'", "\\'");
							webEngine.executeScript(String.format("focusRepair('%s');", nameEscaped));
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				});

				refreshShopSelection();
				updateFormVisibility();
			}
		});

		return item;
	}

	private void refreshShopSelection() {

		for (Component c : shopListPanel.getComponents()) {
			if (c instanceof JPanel) {
				JPanel item = (JPanel) c;
				item.setBackground(Color.WHITE);
				item.setBorder(new LineBorder(COLOR_DIVIDER, 1));

				if (!selectedShopName.isEmpty()
						&& ((JLabel) item.getComponent(0)).getText().contains("<b>" + selectedShopName + "</b>")) {
					item.setBackground(COLOR_SELECTED_BG);
					item.setBorder(new LineBorder(COLOR_PRIMARY, 2));
				}
			}
		}
	}

	private void updateFormVisibility() {

		// ✅ 빈 문자열 검사를 추가하여 처음 로드 시 폼이 비활성화되도록 수정
		boolean enabled = (selectedShopName != null && !selectedShopName.isEmpty());

		shopDisplayField.setText(!enabled ? " 정비소를 먼저 선택해주세요" : " " + selectedShopName);
		dateField.setEnabled(enabled);
		timeCombo.setEnabled(enabled);
		noteArea.setEnabled(enabled);
		for (JCheckBox cb : serviceChecks)
			cb.setEnabled(enabled);
	}

	private JPanel createInputGroup(String labelText, JComponent component) {

		JPanel group = new JPanel();
		group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
		group.setOpaque(false);
		group.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel label = new JLabel(labelText);
		label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		label.setForeground(COLOR_TEXT_LIGHT);
		label.setBorder(new EmptyBorder(0, 0, 5, 0));

		group.add(label);
		group.add(component);
		return group;
	}

	private JPanel createBaseCard(String titleText) {

		JPanel p = new JPanel(new BorderLayout());
		p.setBackground(Color.WHITE);
		p.setBorder(new CompoundBorder(new LineBorder(COLOR_DIVIDER, 1), new EmptyBorder(20, 25, 20, 25)));
		p.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel t = new JLabel(titleText);
		t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		t.setForeground(COLOR_TEXT_DARK);
		t.setBorder(new EmptyBorder(0, 0, 20, 0));

		p.add(t, BorderLayout.NORTH);

		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setOpaque(false);
		p.add(body, BorderLayout.CENTER);

		return p;
	}
}