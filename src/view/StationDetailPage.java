package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;

public class StationDetailPage extends JScrollPane {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_BORDER_LIGHT = new Color(225, 228, 232);
    private static final Color COLOR_ITEM_BORDER = new Color(235, 237, 240);
    private static final Color COLOR_MAP_BG = new Color(230, 233, 237);
    private static final Color COLOR_MAP_BORDER = new Color(210, 214, 219);
    private static final Color COLOR_NAVER_GREEN = new Color(0, 199, 60);
    private static final Color COLOR_BLUE_LIGHT = new Color(59, 130, 246);
    private static final Color COLOR_GRAY_BORDER = new Color(209, 213, 219);

	public StationDetailPage(String uniId) {

		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setBorder(null);
		getVerticalScrollBar().setUnitIncrement(20);

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBackground(COLOR_BG_GRAY);
		container.setBorder(new EmptyBorder(40, 100, 40, 100));

		try {
			// ✅ detailById API 호출
			String apiUrl = "https://www.opinet.co.kr/api/detailById.do?" + "code=F260206147" + "&id=" + uniId
					+ "&out=xml";

			java.net.URL url = new java.net.URL(apiUrl);
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			java.io.InputStream is = conn.getInputStream();
			javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
			javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document doc = builder.parse(is);

			// ✅ 상세정보 파싱
			String name = getTagValue(doc, "OS_NM");
			String addr = getTagValue(doc, "NEW_ADR");
			String tel = getTagValue(doc, "TEL");

			// 유가 정보는 OIL_PRICE 태그 반복문으로 파싱 가능
			org.w3c.dom.NodeList priceList = doc.getElementsByTagName("OIL_PRICE");

			container.add(createHeader(name));
			container.add(Box.createVerticalStrut(30));
			container.add(createBasicInfoCard(name, addr, tel));
			container.add(Box.createVerticalStrut(25));
			container.add(createPriceInfoCard(priceList));
			container.add(Box.createVerticalStrut(25));
			container.add(createDistanceCostCard());
			container.add(Box.createVerticalStrut(25));
			container.add(createMapCard(name));
			container.add(Box.createVerticalStrut(60));

		} catch (Exception e) {
			e.printStackTrace();
			container.add(new JLabel("상세 정보를 불러오는 데 실패했습니다."));
		}

		setViewportView(container);
	}

	// XML 태그 값 추출 유틸
	private String getTagValue(org.w3c.dom.Document doc, String tag) {

		org.w3c.dom.NodeList nl = doc.getElementsByTagName(tag);
		if (nl.getLength() > 0) {
			return nl.item(0).getTextContent();
		}
		return "";
	}

	// ✅ 주소/전화 표시용
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
		grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

		grid.add(createSubInfoBox("전화번호", tel));
		grid.add(createSubInfoBox("영업시간", "24시간")); // 필요시 DB 연동

		card.add(stationTitle);
		card.add(Box.createVerticalStrut(10));
		card.add(addrLabel);
		card.add(Box.createVerticalStrut(25));
		card.add(grid);
		return card;
	}

	// ✅ 유종별 가격 표시용
	private JPanel createPriceInfoCard(org.w3c.dom.NodeList priceList) {

		JPanel card = createBaseCard("💲 유가 정보");
		JPanel grid = new JPanel(new GridLayout(0, 3, 20, 0));
		grid.setOpaque(false);
		grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

		for (int i = 0; i < priceList.getLength(); i++) {
			org.w3c.dom.Element oilPrice = (org.w3c.dom.Element) priceList.item(i);
			String prodcd = oilPrice.getElementsByTagName("PRODCD").item(0).getTextContent();
			String price = oilPrice.getElementsByTagName("PRICE").item(0).getTextContent();
			String type = mapProdcd(prodcd); // 코드 → 한글 유종명 변환
			grid.add(createPriceDetailBox(type, price + "원", ""));
		}
		card.add(grid);
		return card;
	}

	private String mapProdcd(String code) {

		switch (code) {
		case "B027":
			return "휘발유";
		case "D047":
			return "경유";
		case "B034":
			return "고급휘발유";
		case "C004":
			return "등유";
		case "K015":
			return "LPG";
		default:
			return "기타";
		}

	}
    private JPanel createMapCard(String name) {
        JPanel card = createBaseCard("지도");
        
        JPanel mapArea = new JPanel(new GridBagLayout());
        mapArea.setPreferredSize(new Dimension(0, 300));
        mapArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        mapArea.setBackground(COLOR_MAP_BG);
        mapArea.setBorder(new LineBorder(COLOR_MAP_BORDER));
        
        // [API Point] 네이버 지도 정적/동적 지도 API 로드
        // - DB에서 가져온 주유소의 위도(Lat), 경도(Lng) 값을 기반으로 지도 렌더링
        mapArea.add(new JLabel("네이버 지도 API 연동 영역"));

        JPanel btnGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        btnGrid.setOpaque(false);
        btnGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JButton naviBtn = createStyledButton("네이버 지도에서 보기", COLOR_NAVER_GREEN);
        JButton routeBtn = createStyledButton("길찾기", COLOR_PRIMARY);

        // [API/System Point] 외부 브라우저 호출
        naviBtn.addActionListener(e -> {
            /** * 1. DB에서 조회한 해당 주유소의 주소 또는 좌표 데이터를 URL 파라미터로 구성
             * 2. Desktop.getDesktop().browse(new URI("https://map.naver.com/v5/search/주소...")) 실행
             */
        });

        // [API/System Point] 네이버 지도 길찾기 연동
        routeBtn.addActionListener(e -> {
            /** * 1. 사용자의 현재 위치(API 호출 또는 설정값) + 주유소 목적지 좌표 결합
             * 2. 길찾기 스키마 URL을 생성하여 외부 브라우저 실행
             */
        });

        btnGrid.add(naviBtn); btnGrid.add(routeBtn);
        
        card.add(mapArea);
        card.add(Box.createVerticalStrut(20));
        card.add(btnGrid);
        return card;
    }

    private JPanel createHeader(String name) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel title = new JLabel("주유소 상세 정보 (" + name + ")"); 
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        
        JButton backBtn = new JButton("← 뒤로가기");
        backBtn.setBackground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorder(new CompoundBorder(new LineBorder(COLOR_GRAY_BORDER), new EmptyBorder(8, 15, 8, 15)));
        
        backBtn.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win instanceof MainPage) ((MainPage) win).showStationList();
        });

        p.add(title, BorderLayout.WEST);
        p.add(backBtn, BorderLayout.EAST);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        return p;
    }

    private JPanel createBasicInfoCard(String name) {
        JPanel card = createBaseCard("🔵 기본 정보");
        JLabel stationTitle = new JLabel(name, SwingConstants.CENTER);
        stationTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        stationTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // [DB Point] SELECT address FROM stations WHERE station_name = ?
        JLabel addrLabel = new JLabel("서울시 강남구 역삼동 123-45", SwingConstants.CENTER);
        addrLabel.setForeground(Color.GRAY);
        addrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        
        // [DB Point] stations 테이블의 business_hours, phone_number 컬럼 데이터 매핑
        grid.add(createSubInfoBox("영업시간", "24시간"));
        grid.add(createSubInfoBox("전화번호", "02-1234-5678"));

        card.add(stationTitle);
        card.add(Box.createVerticalStrut(10));
        card.add(addrLabel);
        card.add(Box.createVerticalStrut(25));
        card.add(grid);
        return card;
    }

    private JPanel createPriceInfoCard() {
        JPanel card = createBaseCard("💲 유가 정보");
        JPanel grid = new JPanel(new GridLayout(1, 3, 20, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        
        // [DB Point] prices 테이블에서 유종별 최신 가격 데이터 로드
        // [Logic Point] 전국 평균가(다른 테이블 혹은 API 결과)와 비교 연산 수행 후 'compare' 텍스트 생성
        grid.add(createPriceDetailBox("휘발유", "1,550원", "전국 평균 대비 -30원"));
        grid.add(createPriceDetailBox("경유", "1,450원", "전국 평균 대비 -20원"));
        grid.add(createPriceDetailBox("LPG", "950원", "전국 평균 대비 -10원"));
        grid.add(createPriceDetailBox("전기", "1,860원", "전국 평균 대비 +10원"));
        
        // [DB Point] 데이터 수집/업데이트 로그 시간 표시 (updated_at 컬럼)
        JLabel updateLabel = new JLabel("* 최종 업데이트: 2026-02-13 09:00", SwingConstants.CENTER);
        updateLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        updateLabel.setForeground(Color.LIGHT_GRAY);
        updateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(grid);
        card.add(Box.createVerticalStrut(15));
        card.add(updateLabel);
        return card;
    }

    private JPanel createDistanceCostCard() {
        JPanel card = createBaseCard("🚩 거리 / 예상 이동 비용");
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        
        // [Logic/API Point] 
        // 1. Haversine 공식 등을 사용하여 사용자의 좌표와 주유소 좌표 사이의 직선/이동 거리 계산
        // 2. (거리 / 사용자 차량 연비) * 휘발유 단가 계산하여 예상 비용 도출
        grid.add(createSubInfoBox("현재 위치에서 거리", "1.5km"));
        grid.add(createSubInfoBox("예상 이동 비용", "약 300원 (연비 12km/L 기준)"));
        card.add(grid);
        return card;
    }

    /* ===== 공통 UI 빌더 메서드 ===== */

    private JPanel createBaseCard(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_LIGHT), new EmptyBorder(30, 40, 30, 40)));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        t.setForeground(COLOR_PRIMARY);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(t); p.add(Box.createVerticalStrut(20));
        return p;
    }

    private JPanel createSubInfoBox(String title, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 5));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(COLOR_ITEM_BORDER), new EmptyBorder(15, 20, 15, 20)));
        JLabel t = new JLabel(title);
        t.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        t.setForeground(Color.GRAY);
        JLabel v = new JLabel(value);
        v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        p.add(t); p.add(v);
        return p;
    }

    private JPanel createPriceDetailBox(String type, String price, String compare) {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 3));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(COLOR_ITEM_BORDER), new EmptyBorder(15, 20, 15, 20)));
        JLabel t = new JLabel(type); t.setForeground(Color.GRAY);
        JLabel v = new JLabel(price); v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22)); v.setForeground(COLOR_PRIMARY);
        JLabel c = new JLabel(compare); c.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12)); c.setForeground(COLOR_BLUE_LIGHT);
        p.add(t); p.add(v); p.add(c);
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