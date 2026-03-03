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
	private JLabel briefingContent;
	private JPanel recommendPanel;
	private JLabel totalCountLabel, totalAmountLabel, avgPriceLabel, diffPercentLabel;
	private String selectedProdCd = "B027"; // 기본값 휘발유
	
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

		// [이벤트] 탭이 전환되어 화면에 보일 때마다 refreshData 호출
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

		String trendColor = "#4B5563"; // 기본 회색 (변동 없음)
		String trendText = "변동이 없습니다.";

		if (diff != null && !diff.isEmpty() && !diff.equals("0") && !diff.equals("0.0")) {
			if (diff.contains("+")) {
				trendColor = "#DC2626"; // 빨간색 (상승)
				trendText = diff.replace("+", "") + "원 상승했습니다.";
			} else {
				trendColor = "#2563EB"; // 파란색 (하락)
				trendText = diff.replace("-", "") + "원 하락했습니다.";
			}
		}

		return "<html>오늘 전국 평균 휘발유 가격은 리터당 <font color='#2563EB'><b>" + price + "원</b></font>으로 어제보다 <font color='"
				+ trendColor + "'><b>" + trendText + "</b></font></html>";
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
		briefingContent.setText("<html>오늘 전국 평균 유가를 불러오는 중입니다... ⏳</html>");// UI 멈춤(프리징) 방지를 위한 비동기 처리
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

					if (dto != null) {
						// 헬퍼 메서드를 통해 조립된 HTML 문자열을 받아와서 UI 업데이트
						String htmlText = OneLineBriefing(dto);
						briefingContent.setText(htmlText);
					} else {
						briefingContent.setText("<html><font color='#DC2626'><b>휘발유 가격 정보를 찾을 수 없습니다.</b></font></html>");
					}

				} catch (Exception e) {
					e.printStackTrace();
					// 오류 발생 시 표시할 텍스트
					briefingContent.setText("<html><font color='#DC2626'><b>유가 정보를 불러오는 데 실패했습니다.</b></font></html>");
				}
			}
		};
		worker.execute();
		// --- 2. 추천 주유소 영역 (더미 데이터) ---
		recommendPanel.removeAll();
		try {
			List<apiService.NearStationDto> stations = apiService.NearStationService.getNearStations("1005"); // 예시: 부산
																												// 부산진구
			for (apiService.NearStationDto s : stations) {
				recommendPanel.add(createGasRow(s.getName(), s.getAddr(), s.getPrice()));
				recommendPanel.add(Box.createVerticalStrut(12));
			}
		} catch (Exception e) {
			e.printStackTrace();
			recommendPanel.add(new JLabel("추천 주유소 정보를 불러오는 데 실패했습니다."));
		}
		// --- 3. 주유비 통계 영역 (더미 데이터) ---
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
     // 처음 실행 시 휘발유(B027) 데이터를 자동으로 불러오게 합니다.
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
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();

        // 갱신 후 화면 다시 그리기
        revalidate();
        repaint();
	}

	// [섹션 1] 유가 브리핑 박스
	private JPanel createBriefingBox() {

		JPanel card = createBaseCard("📈 오늘의 유가 한 줄 브리핑");

		/**
		 * [API 연동 및 비즈니스 로직 상세]
		 * 1. API 호출 (Service 계층): 
		 * - Opinet '전국 평균 유가(avgAllPrice)' API 호출
		 * - URL: http://www.opinet.co.kr/api/avgAllPrice.do?out=json&code=API_KEY
		 * 2. 데이터 추출: JSON 파싱하여 'price'(평균가), 'diff'(전일대비) 추출
		 * 3. 비즈니스 로직: diff 값이 0보다 크면 '상승', 작으면 '하락' 텍스트 매칭
		 */

		briefingContent = new JLabel("데이터를 불러오는 중...");
		briefingContent.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		briefingContent.setAlignmentX(Component.LEFT_ALIGNMENT);

		card.add(briefingContent);
		return card;
	}

	// [섹션 2] 내 지역 추천 주유소 박스
	private JPanel createRecommendBox() {

		JPanel card = createBaseCard("📍 내 지역 추천 주유소");

		/**
		 * [DB & API 복합 연동 포인트]
		 * 1. DB: SELECT addr FROM users WHERE id = ? (사용자 선호 지역 정보 취득)
		 * 2. API: 오피넷 '지역별 최저가 주유소' API 호출 (시군구 코드 활용)
		 * 3. UI: 반환된 주유소 리스트를 for문을 통해 createGasRow()로 생성하여 recommendPanel에 추가
		 */

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

		JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
		grid.setBackground(Color.WHITE);
		grid.setAlignmentX(Component.LEFT_ALIGNMENT);
		grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

		/**
		 * [API 연동 포인트]
		 * 1. 주변 반경(3~5km) 내 주유소 정보 호출
		 * 2. 알고리즘: (가격) + (이동 거리 비용)을 계산하여 최적의 주유소 도출
		 */
		try {
			List<apiService.ValueStationDto> stations = apiService.ValueStationService.getStations(494152, 282437,
					3000);
			apiService.BestValueService.EfficiencyResult result = apiService.BestValueService.getBestStations(stations);

			grid.add(createNestedBox("최저가 주유소", result.cheapest.getName(), result.cheapest.getPrice() + "원/L",
					COLOR_PRIMARY));

			grid.add(
					createNestedBox("거리 비례 가성비 추천", result.bestValue.getName(),
							result.bestValue.getPrice() + "원/L ("
									+ String.format("%.1fkm", result.bestValue.getDistance() / 1000.0) + ")",
							COLOR_PRIMARY));

		} catch (Exception e) {
			e.printStackTrace();
			grid.add(new JLabel("추천 정보를 불러오는 데 실패했습니다."));
		}
		card.add(grid);
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
	    
	    String[][] types = {
	        {"B027", "휘발유"}, {"D047", "경유"}, 
	        {"B034", "고급"}, {"C004", "등유"}, {"K015", "LPG"}
	    };

	    chart = new FuelChartPanel();
	    chart.setPreferredSize(new Dimension(0, 250));

	    for (String[] t : types) {
	        JButton btn = new JButton(t[1]);
	        btn.putClientProperty("prodCd", t[0]);
	        styleSecondaryBtn(btn, COLOR_PRIMARY);
	        
	        if(t[0].equals(selectedProdCd)) {
	            btn.setBackground(COLOR_PRIMARY);
	            btn.setForeground(Color.WHITE);
	        }

	        btn.addActionListener(e -> {
	            selectedProdCd = t[0];
	            updateButtonStyles(btnPanel);
	            
	            new SwingWorker<List<FuelTrendDto>, Void>() {
	                @Override protected List<FuelTrendDto> doInBackground() {
	                    return new FuelTrendService().getWeeklyTrend(t[0]);
	                }
	                @Override protected void done() {
	                    try { chart.setDataWithAnim(get()); } catch (Exception ex) {}
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