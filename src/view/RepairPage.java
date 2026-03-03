package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import apiService.RepairDao;
import apiService.RepairDto;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
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

	private WebEngine webEngine;

	public RepairPage() {

		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		getVerticalScrollBar().setUnitIncrement(20);
		setBorder(null);

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBackground(COLOR_BG_GRAY);
		container.setBorder(new EmptyBorder(30, 60, 30, 60));

		JLabel title = new JLabel("정비소 예약");
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
		title.setForeground(COLOR_TEXT_DARK);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		container.add(title);
		container.add(Box.createVerticalStrut(25));

		container.add(createMapSection());
		container.add(Box.createVerticalStrut(25));
		container.add(createShopSection());
		container.add(Box.createVerticalStrut(25));
		container.add(createFormSection());

		container.add(Box.createVerticalGlue());
		setViewportView(container);

		// [이벤트] 탭 전환 시 데이터 갱신 리스너
		this.addHierarchyListener(e -> {
			if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
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

		if (shopListPanel == null)
			return;
		shopListPanel.removeAll();

		try {
			RepairDao dao = new RepairDao();
			// 예시: 부산진구 기준, 내 위치 (35.15, 129.03)
			List<RepairDto> shops = dao.getNearestShops(35.1417545, 129.0341064, "부산진구", 10);

			for (RepairDto s : shops) {
				shopListPanel.add(createShopItem(s));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (selectedShopName != null) {
			refreshShopSelection();
		}

		shopListPanel.revalidate();
		shopListPanel.repaint();
	}

	private JPanel createMapSection() {

		JPanel card = createBaseCard("📍 위치 확인");
		JPanel body = (JPanel) card.getComponent(1);

		JFXPanel jfxPanel = new JFXPanel();
		jfxPanel.setPreferredSize(new Dimension(0, 400));

		Platform.runLater(() -> {
			WebView webView = new WebView();
			webEngine = webView.getEngine();

			webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
				if (newState == Worker.State.SUCCEEDED) {
					// 안전한 모드 전환 호출
					String script = "if(typeof setMapMode === 'function') { setMapMode('REPAIR'); }";
					try {
						webEngine.executeScript(script);
					} catch (Exception e) {
						System.err.println("JS 로딩 지연: " + e.getMessage());
					}
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

	private JPanel createShopSection() {

		JPanel card = createBaseCard("📍 근처 정비소");

		// 정비소 목록이 들어갈 컨테이너 초기화
		shopListPanel = new JPanel(new GridLayout(0, 2, 15, 15));
		shopListPanel.setOpaque(false);

		((JPanel) card.getComponent(1)).add(shopListPanel);
		return card;
	}

	private JPanel createFormSection() {

		JPanel card = createBaseCard("🔧 예약 정보 입력");
		JPanel body = (JPanel) card.getComponent(1);
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

		// 1. 선택한 정비소 표시
		shopDisplayField = new JTextField();
		shopDisplayField.setEditable(false);
		shopDisplayField.setPreferredSize(new Dimension(0, 35));
		body.add(createInputGroup("선택한 정비소", shopDisplayField));
		body.add(Box.createVerticalStrut(15));

		// 2. 예약 날짜 및 시간
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

		// 3. 정비 서비스 선택
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

		// 4. 요청사항 입력
		noteArea = new JTextArea(4, 20);
		noteArea.setBorder(new LineBorder(COLOR_DIVIDER));
		JScrollPane noteScroll = new JScrollPane(noteArea);
		noteScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
		body.add(createInputGroup("요청사항", noteScroll));
		body.add(Box.createVerticalStrut(20));

		// 5. 버튼 및 예약 실행 (API/DB 연동)
		JButton submitBtn = new JButton("예약하기");
		submitBtn.setBackground(COLOR_PRIMARY);
		submitBtn.setForeground(Color.WHITE);
		submitBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
		submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

		submitBtn.addActionListener(e -> {
			/** * [예약 실행 로직]
			 * Controller를 통해 UI 입력값을 DB에 저장합니다.
			 */

			// 1) 필수 선택 검증
			if (selectedShopName == null) {
				JOptionPane.showMessageDialog(null, "정비소를 먼저 선택해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (timeCombo.getSelectedIndex() == 0) {
				JOptionPane.showMessageDialog(null, "예약 시간을 선택해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
				return;
			}

			// 2) 체크된 서비스 항목 수집 (콤마로 구분된 문자열 생성)
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

			// 3) 컨트롤러 호출
			boolean success = reservationController.requestReservation(selectedShopName, dateField.getText().trim(),
					timeCombo.getSelectedItem().toString(), sb.toString(), noteArea.getText().trim());

			// 4) 결과 처리
			if (success) {
				JOptionPane.showMessageDialog(null, selectedShopName + "에 예약이 성공적으로 완료되었습니다!", "예약 완료",
						JOptionPane.INFORMATION_MESSAGE);
				resetForm(); // 입력 폼 초기화 (선택 사항)
			} else {
				JOptionPane.showMessageDialog(null, "예약 처리 중 오류가 발생했습니다. 다시 시도해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
			}
		});

		body.add(submitBtn);
		return card;
	}

	/**
	 * 예약 완료 후 입력 폼을 초기 상태로 되돌립니다.
	 */
	private void resetForm() {

		selectedShopName = "";
		dateField.setText("2026-03-03"); // 현재 날짜 등으로 초기화
		timeCombo.setSelectedIndex(0);
		noteArea.setText("");
		for (JCheckBox cb : serviceChecks)
			cb.setSelected(false);
		updateFormVisibility();
		refreshShopSelection();
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


				selectedShopName = s.getName();

				Platform.runLater(() -> {
					try {
						if (webEngine != null) {
							System.out.println("JS 호출 시작: " + selectedShopName); // Java 콘솔에 출력되는지 확인
							webEngine.executeScript("console.log('Java에서 마커 추가 요청됨');");

							webEngine.executeScript("clearMarkers();");

							String nameEscaped = s.getName().replace("'", "\\'");
							String addScript = String.format(java.util.Locale.US, "addRepair(%f, %f, '%s', '%s')",
									s.getX(), s.getY(), nameEscaped, "정비 예약 가능");

							webEngine.executeScript(addScript);
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
				// 텍스트 매칭을 통해 현재 선택된 항목 식별
				if (((JLabel) item.getComponent(0)).getText().contains("<b>" + selectedShopName + "</b>")) {
					item.setBackground(COLOR_SELECTED_BG);
					item.setBorder(new LineBorder(COLOR_PRIMARY, 2));
				}
			}
		}
	}

	private void updateFormVisibility() {

		boolean enabled = (selectedShopName != null);
		shopDisplayField.setText(selectedShopName.isEmpty() ? " 정비소를 먼저 선택해주세요" : " " + selectedShopName);
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