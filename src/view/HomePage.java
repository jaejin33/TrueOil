package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import apiService.*;
import fuel.FuelController;
import fuel.dto.MonthlySummaryDto;

import java.util.List;

public class HomePage extends JScrollPane {
	private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
	private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
	private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
	private static final Color COLOR_DANGER = new Color(220, 38, 38);
	private static final Color COLOR_SUCCESS = new Color(22, 163, 74);

	private JPanel container;
	private JPanel recommendPanel;
	private JPanel efficiencyGrid;
	private JLabel totalCountLabel, totalAmountLabel, avgPriceLabel, diffPercentLabel;
	private String selectedProdCd = "B027"; // 기본값 휘발유

	private JLabel briefSummaryLabel; // 한 줄 요약 텍스트
	private JLabel valPremium, diffPremium; // 고급휘발유 (값, 등락)
	private JLabel valGasoline, diffGasoline; // 휘발유
	private JLabel valDiesel, diffDiesel; // 경유
	private JLabel valLpg, diffLpg; // LPG

	private FuelChartPanel chart;
	private FuelController fuelController = new FuelController(); // 컨트롤러 선언

	public HomePage() {

		container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBackground(COLOR_BG_GRAY);
		container.setBorder(new EmptyBorder(30, 60, 30, 60));

		JLabel title = new JLabel("메인");
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		container.add(title);
		container.add(Box.createVerticalStrut(25));

		container.add(createBriefingBox());
		container.add(Box.createVerticalStrut(25));
		container.add(createTrendChartBox());
		container.add(Box.createVerticalStrut(25));
		container.add(createRecommendBox());
		container.add(Box.createVerticalStrut(25));
		container.add(createEfficiencyBox());
		container.add(Box.createVerticalStrut(25));
		container.add(createSummaryBox());

		setViewportView(container);
		setBorder(null);
		getVerticalScrollBar().setUnitIncrement(16);

		// 탭이 전환되어 화면에 보일 때마다 refreshData 호출
		this.addHierarchyListener(e -> {
			if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
				refreshData();
			}
		});

		refreshData();
	}

	// 1
	private String OneLineBriefing(apiService.AvgPriceDto dto) {

		String price = dto.getAvgPrice();
		String diff = dto.getDiffPrice();

		String trendColor = "#3f3f46";
		String trendText = "변동이 없습니다.";

		if (diff != null && !diff.isEmpty() && !diff.equals("0") && !diff.equals("0.0")) {
			if (diff.contains("+")) {
				trendColor = "#DC2626"; // 빨간색 (상승)
				trendText = "▲ " + diff.replace("+", "") + "원 상승했습니다.";
			} else {
				trendColor = "#2563EB"; // 파란색 (하락)
				trendText = "▼ " + diff.replace("-", "") + "원 하락했습니다.";
			}
		}

		// 모던 & 클래식 느낌을 주는 좌측 포인트 라인 디자인 적용
		return "<html><div style='padding:12px 15px; background-color:#fafafa; border-left:4px solid #18181b; font-family:sans-serif; font-size:13px; color:#3f3f46;'>"
				+ "오늘 전국 평균 <b>휘발유</b> 가격은 리터당 <span style='color:#2563eb; font-weight:bold;'>" + price
				+ "원</span>으로 어제보다 " + "<span style='color:" + trendColor + "; font-weight:bold;'>" + trendText
				+ "</span>" + "</div></html>";
	}

	/**
	 * 실시간 데이터 연동 및 UI 갱신 로직
	 */
	public void refreshData() {
		/**
		 * [데이터 연동 순서 가이드]
		 * 1. Service/DAO 객체 호출 (예: GasService gasService = new GasService();)
		 * 2. DB 데이터 조회: 이번 달 총 지출 금액, 주유 횟수 등
		 * 3. API 호출: 오피넷(Opinet) 실시간 전국 평균 유가 정보
		 * 4. UI 업데이트: 조회된 데이터를 각 Label 및 Panel에 mapping
		 */

		// 2
		SwingWorker<java.util.Map<String, apiService.AvgPriceDto>, Void> worker = new SwingWorker<>() {
			@Override
			protected java.util.Map<String, apiService.AvgPriceDto> doInBackground() throws Exception {

				// 백그라운드에서 API 호출 (Map 형태로 모든 유종의 평균가 가져오기)
				return apiService.AvgPrice.getAvgPrice();
			}

			@Override
			protected void done() {

				try {
					// API 호출 결과 받아오기
					java.util.Map<String, apiService.AvgPriceDto> avgPriceMap = get();

					apiService.AvgPriceDto dto = avgPriceMap.get("B027");

					// 한 줄 요약 업데이트 (휘발유 기준)
					apiService.AvgPriceDto gasDto = avgPriceMap.get("B027");
					if (gasDto != null) {
						briefSummaryLabel.setText(OneLineBriefing(gasDto));
					} else {
						briefSummaryLabel
								.setText("<html><font color='#DC2626'><b>휘발유 가격 정보를 찾을 수 없습니다.</b></font></html>");
					}

					updateFuelLabel(avgPriceMap.get("B034"), valPremium, diffPremium); // 고급휘발유
					updateFuelLabel(avgPriceMap.get("B027"), valGasoline, diffGasoline); // 휘발유
					updateFuelLabel(avgPriceMap.get("D047"), valDiesel, diffDiesel); // 경유
					updateFuelLabel(avgPriceMap.get("K015"), valLpg, diffLpg);

				} catch (Exception e) {
					e.printStackTrace();
					// 오류 발생 시 표시할 텍스트
					briefSummaryLabel.setText("<html><font color='#DC2626'><b>유가 정보를 불러오는 데 실패했습니다.</b></font></html>");
				}
			}
		};
		worker.execute();
		// --- 2. 추천 주유소 영역 ---
		recommendPanel.removeAll();
		try {
			database.LocationData currentLoc = database.LocationData.selected;
			double currentX = currentLoc.getX();
			double currentY = currentLoc.getY();
			List<apiService.ValueStationDto> stations = apiService.ValueStationService.getStations(currentX, currentY,
					3000, "", "B027", "1");
			if (stations != null && !stations.isEmpty()) {
				// 3. 상위 3개 주유소만 리스트에 추가 (UI가 너무 길어지지 않도록 방지)
				int limit = Math.min(stations.size(), 3);
				for (int i = 0; i < limit; i++) {
					apiService.ValueStationDto s = stations.get(i);

					String distanceText = String.format("📍 %s 기준 약 %.1fkm", currentLoc.getName(),
							s.getDistance() / 1000.0);

					// 가격 포맷팅 (예: 1650 -> 1,650원)
					int priceVal = Integer.parseInt(s.getPrice());
					String formattedPrice = String.format("%,d원", priceVal);

					recommendPanel.add(createGasRow(s.getName(), distanceText, formattedPrice));
					recommendPanel.add(Box.createVerticalStrut(12));
				}
			} else {
				// 검색된 주유소가 없을 경우
				JLabel emptyLabel = new JLabel("선택된 지역(" + currentLoc.getName() + ") 주변에 추천할 주유소가 없습니다.");
				emptyLabel.setForeground(Color.GRAY);
				recommendPanel.add(emptyLabel);
			}

		} catch (Exception e) {
			e.printStackTrace();
			JLabel errorLabel = new JLabel("추천 주유소 정보를 불러오는 데 실패했습니다.");
			errorLabel.setForeground(COLOR_DANGER);
			recommendPanel.add(errorLabel);
		}
		efficiencyGrid.removeAll();
		try {
			// 1. 전역에서 선택된 지역 정보 사용
			database.LocationData currentLoc = database.LocationData.selected;
			double currentX = currentLoc.getX();
			double currentY = currentLoc.getY();

			// 2. 반경 3km 내 휘발유 주유소 정보 호출
			List<apiService.ValueStationDto> effStations = apiService.ValueStationService.getStations(currentX,
					currentY, 3000, "", "B027", "1");

			if (effStations != null && !effStations.isEmpty()) {
				// 3. 거리+가격 비례 가성비 알고리즘 실행
				apiService.BestValueService.EfficiencyResult result = apiService.BestValueService
						.getBestStations(effStations);

				if (result != null && result.cheapest != null && result.bestValue != null) {

					// 금액 포맷팅 (예: 1650 -> 1,650원)
					int cheapPrice = Integer.parseInt(result.cheapest.getPrice());
					int bestPrice = Integer.parseInt(result.bestValue.getPrice());

					efficiencyGrid.add(createNestedBox("단순 최저가 주유소", result.cheapest.getName(),
							String.format("%,d원/L", cheapPrice), COLOR_PRIMARY));

					efficiencyGrid.add(createNestedBox("거리 비례 가성비 추천", result.bestValue.getName(),
							String.format("%,d원/L (%.1fkm)", bestPrice, result.bestValue.getDistance() / 1000.0),
							COLOR_PRIMARY));
				} else {
					efficiencyGrid.add(new JLabel("추천할 주유소가 충분하지 않습니다."));
				}
			} else {
				efficiencyGrid.add(new JLabel("주변에 가성비를 계산할 주유소가 없습니다."));
			}

		} catch (Exception e) {
			e.printStackTrace();
			JLabel errorLabel = new JLabel("가성비 추천 정보를 불러오는 데 실패했습니다.");
			errorLabel.setForeground(COLOR_DANGER);
			efficiencyGrid.add(errorLabel);
		}

		MonthlySummaryDto summary = fuelController.getMonthlySummary();

		if (summary != null) {
			// 총 주유 횟수
			totalCountLabel.setText(summary.getTotalCount() + "회");

			// 총 주유 금액 (3자리 콤마 포맷: 245,000원)
			totalAmountLabel.setText(String.format("%,d원", summary.getTotalAmount()));

			// 평균 가격
			avgPriceLabel.setText(String.format("%,d원", summary.getAvgPrice()));

			// 지난달 대비 증감률 설정 및 색상 피드백
			double diff = summary.getDiffPercent();
			if (diff > 0) {
				diffPercentLabel.setText(String.format("+%.1f%%", diff));
				diffPercentLabel.setForeground(COLOR_DANGER); // 지출 증가 시 빨간색
			} else if (diff < 0) {
				diffPercentLabel.setText(String.format("%.1f%%", diff));
				diffPercentLabel.setForeground(COLOR_SUCCESS); // 지출 감소 시 초록색
			} else {
				diffPercentLabel.setText("0%");
				diffPercentLabel.setForeground(COLOR_TEXT_DARK);
			}
		}
		new SwingWorker<List<FuelTrendDto>, Void>() {
			@Override
			protected List<FuelTrendDto> doInBackground() {

				// 오피넷 API에서 휘발유(B027) 7일치 데이터를 가져옴 [cite: 101]
				return new FuelTrendService().getWeeklyTrend("B027");
			}

			@Override
			protected void done() {

				try {
					List<FuelTrendDto> result = get();
					if (result != null && !result.isEmpty()) {
						// 아래에서 올라오는 애니메이션과 함께 출력
						chart.setDataWithAnim(result);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}.execute();

		// 갱신 후 화면 다시 그리기
		revalidate();
		repaint();
	}

	private JPanel createBriefingBox() {

		JPanel card = createBaseCard("📈 오늘의 전국 유가 브리핑");

		briefSummaryLabel = new JLabel("<html>데이터를 불러오는 중입니다...⏳</html>");
		briefSummaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.add(briefSummaryLabel);
		card.add(Box.createVerticalStrut(20));

		JPanel grid = new JPanel(new GridLayout(1, 4, 15, 0));
		grid.setOpaque(false);
		grid.setAlignmentX(Component.LEFT_ALIGNMENT);
		grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

		valPremium = new JLabel("-");
		diffPremium = new JLabel("-");
		valGasoline = new JLabel("-");
		diffGasoline = new JLabel("-");
		valDiesel = new JLabel("-");
		diffDiesel = new JLabel("-");
		valLpg = new JLabel("-");
		diffLpg = new JLabel("-");

		grid.add(createFuelCard("휘발유", valGasoline, diffGasoline));
		grid.add(createFuelCard("경유", valDiesel, diffDiesel));
		grid.add(createFuelCard("LPG", valLpg, diffLpg));
		grid.add(createFuelCard("고급휘발유", valPremium, diffPremium));

		card.add(grid);
		return card;
	}

	private JPanel createFuelCard(String name, JLabel priceLbl, JLabel diffLbl) {

		JPanel panel = new JPanel(new GridLayout(3, 1, 0, 5));

		Color bgColor = Color.WHITE;
		Color nameColor = new Color(113, 113, 122);
		Color priceColor = new Color(24, 24, 27);

		panel.setBackground(bgColor);
		panel.setBorder(
				new CompoundBorder(new LineBorder(new Color(228, 228, 231), 1, true), new EmptyBorder(15, 15, 15, 15)));

		JLabel nameLbl = new JLabel(name);
		nameLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		nameLbl.setForeground(nameColor);

		priceLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
		priceLbl.setForeground(priceColor);
		priceLbl.setHorizontalAlignment(SwingConstants.RIGHT);

		diffLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		diffLbl.setHorizontalAlignment(SwingConstants.RIGHT);

		panel.add(nameLbl);
		panel.add(priceLbl);
		panel.add(diffLbl);

		return panel;
	}

	// 📊 API 데이터 라벨 매핑 헬퍼
	private void updateFuelLabel(apiService.AvgPriceDto dto, JLabel priceLbl, JLabel diffLbl) {

		if (dto == null) {
			priceLbl.setText("-");
			diffLbl.setText("-");
			return;
		}
		priceLbl.setText(dto.getAvgPrice());

		String diff = dto.getDiffPrice();
		if (diff == null || diff.isEmpty() || diff.equals("0") || diff.equals("0.0")) {
			diffLbl.setText("- 0.00");
			diffLbl.setForeground(new Color(113, 113, 122)); // 보합 (회색)
		} else if (diff.contains("+")) {
			diffLbl.setText("▲ " + diff.replace("+", ""));
			diffLbl.setForeground(new Color(220, 38, 38)); // 상승 (빨간색)
		} else {
			diffLbl.setText("▼ " + diff.replace("-", ""));
			diffLbl.setForeground(new Color(37, 99, 235)); // 하락 (파란색)
		}
	}

	// 내 지역 추천 주유소 박스
	private JPanel createRecommendBox() {

		JPanel card = createBaseCard("📍 내 지역 추천 주유소");


		recommendPanel = new JPanel();
		recommendPanel.setLayout(new BoxLayout(recommendPanel, BoxLayout.Y_AXIS));
		recommendPanel.setOpaque(false);
		recommendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		card.add(recommendPanel);
		return card;
	}

	// 가성비 추천 박스
	private JPanel createEfficiencyBox() {

		JPanel card = createBaseCard("💰 가성비 추천");
		
		efficiencyGrid = new JPanel(new GridLayout(1, 2, 20, 0));
		efficiencyGrid.setBackground(Color.WHITE);
		efficiencyGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
		efficiencyGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
		
		efficiencyGrid.add(new JLabel("데이터를 불러오는 중입니다...⏳"));
		card.add(efficiencyGrid);
		return card;
	}

	// 주유비 요약 박스
	private JPanel createSummaryBox() {

		JPanel card = createBaseCard("📅 이번 달 주유비 요약");

		JPanel grid = new JPanel(new GridLayout(1, 4, 15, 0));
		grid.setBackground(Color.WHITE);
		grid.setAlignmentX(Component.LEFT_ALIGNMENT);
		grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

		/**
		 * [DB 연동 포인트]
		 * 1. 쿼리: 
		 * SELECT COUNT(*) as count, SUM(fuel_amount * unit_price) as total_price, AVG(unit_price) as avg_price 
		 * FROM fuel_logs 
		 * WHERE user_id = ? AND date_format(fill_date, '%Y-%m') = date_format(NOW(), '%Y-%m')
		 * 2. 로직: 전월 데이터와 비교하여 증감률(%) 계산 후 COLOR_SUCCESS 또는 COLOR_DANGER 적용
		 */
		totalCountLabel = new JLabel("0회", SwingConstants.CENTER);
		totalAmountLabel = new JLabel("0원", SwingConstants.CENTER);
		avgPriceLabel = new JLabel("0원", SwingConstants.CENTER);
		diffPercentLabel = new JLabel("0%", SwingConstants.CENTER);

		grid.add(createStatContainer("총 주유 횟수", totalCountLabel, COLOR_TEXT_DARK));
		grid.add(createStatContainer("총 주유 금액", totalAmountLabel, COLOR_PRIMARY));
		grid.add(createStatContainer("평균 가격", avgPriceLabel, COLOR_TEXT_DARK));
		grid.add(createStatContainer("지난달 대비", diffPercentLabel, COLOR_SUCCESS));

		card.add(grid);
		return card;
	}

	/* --- UI 헬퍼 메서드 (디자인 및 레이아웃 관리) --- */

	private JPanel createBaseCard(String titleText) {

		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(Color.WHITE);
		card.setBorder(
				new CompoundBorder(new LineBorder(new Color(209, 213, 219), 1), new EmptyBorder(25, 25, 25, 25)));
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

		JLabel title = new JLabel(titleText);
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		card.add(title);
		card.add(Box.createVerticalStrut(20));
		return card;
	}

	private JPanel createGasRow(String name, String addr, String price) {

		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(new Color(249, 250, 251));
		row.setBorder(new CompoundBorder(new LineBorder(new Color(229, 231, 235)), new EmptyBorder(15, 20, 15, 20)));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

		JPanel left = new JPanel(new GridLayout(2, 1));
		left.setOpaque(false);
		left.add(new JLabel("<html><b>" + name + "</b></html>"));
		JLabel sub = new JLabel(addr + " | ");
		sub.setForeground(Color.GRAY);
		left.add(sub);

		JLabel p = new JLabel(price);
		p.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
		p.setForeground(COLOR_PRIMARY);

		row.add(left, BorderLayout.WEST);
		row.add(p, BorderLayout.EAST);
		return row;
	}

	private JPanel createNestedBox(String label, String name, String val, Color valCol) {

		JPanel b = new JPanel();
		b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
		b.setBackground(new Color(252, 252, 253));
		b.setBorder(new CompoundBorder(new LineBorder(new Color(229, 231, 235)), new EmptyBorder(15, 15, 15, 15)));

		JLabel l = new JLabel(label);
		l.setForeground(Color.GRAY);
		JLabel n = new JLabel(name);
		n.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		JLabel v = new JLabel(val);
		v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		v.setForeground(valCol);

		b.add(l);
		b.add(Box.createVerticalStrut(5));
		b.add(n);
		b.add(Box.createVerticalStrut(5));
		b.add(v);
		return b;
	}

	private JPanel createStatContainer(String label, JLabel valueLabel, Color valCol) {

		JPanel b = new JPanel(new GridLayout(2, 1, 0, 5));
		b.setBackground(new Color(252, 252, 253));
		b.setBorder(new CompoundBorder(new LineBorder(new Color(229, 231, 235)), new EmptyBorder(15, 10, 15, 10)));

		JLabel l = new JLabel(label, SwingConstants.CENTER);
		l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		l.setForeground(new Color(75, 85, 99));

		valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
		valueLabel.setForeground(valCol);

		b.add(l);
		b.add(valueLabel);
		return b;
	}

	private JPanel createTrendChartBox() {

		JPanel card = createBaseCard("📊 최근 7일 유가 흐름");

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		btnPanel.setOpaque(false);

		String[][] types = { { "B027", "휘발유" }, { "D047", "경유" }, { "B034", "고급" }, { "C004", "등유" },
				{ "K015", "LPG" } };

		chart = new FuelChartPanel();
		chart.setPreferredSize(new Dimension(0, 250));

		for (String[] t : types) {
			JButton btn = new JButton(t[1]);
			btn.putClientProperty("prodCd", t[0]);
			styleSecondaryBtn(btn, COLOR_PRIMARY);

			if (t[0].equals(selectedProdCd)) {
				btn.setBackground(COLOR_PRIMARY);
				btn.setForeground(Color.WHITE);
			}

			btn.addActionListener(e -> {
				selectedProdCd = t[0];
				updateButtonStyles(btnPanel);

				new SwingWorker<List<FuelTrendDto>, Void>() {
					@Override
					protected List<FuelTrendDto> doInBackground() {

						return new FuelTrendService().getWeeklyTrend(t[0]);
					}

					@Override
					protected void done() {

						try {
							chart.setDataWithAnim(get());
						} catch (Exception ex) {
						}
					}
				}.execute();
			});
			btnPanel.add(btn);
		}

		card.add(btnPanel);
		card.add(Box.createVerticalStrut(20));
		card.add(chart);
		return card;
	}

	/**
	 * 보조 버튼 스타일 (흰색 배경 + 유색 테두리)
	 */
	private void styleSecondaryBtn(JButton b, Color fg) {

		b.setBackground(Color.WHITE);
		b.setForeground(fg);
		b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		b.setFocusPainted(false);
		b.setBorder(new LineBorder(fg, 1));
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		b.setPreferredSize(new Dimension(80, 35));
	}

	private void updateButtonStyles(JPanel btnPanel) {

		for (Component c : btnPanel.getComponents()) {
			if (c instanceof JButton) {
				JButton btn = (JButton) c;
				// 버튼의 ActionCommand나 텍스트로 비교 (여기선 텍스트 예시)
				if (btn.getClientProperty("prodCd").equals(selectedProdCd)) {
					btn.setBackground(COLOR_PRIMARY); // 파란색 채우기
					btn.setForeground(Color.WHITE);
				} else {
					styleSecondaryBtn(btn, COLOR_PRIMARY); // 다시 테두리만
				}
			}
		}
	}

}