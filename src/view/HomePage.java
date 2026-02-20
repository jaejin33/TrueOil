package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * 메인 홈 화면 클래스
 * [개선 사항] 하드코딩된 색상들을 상수로 관리하여 유지보수성 향상
 */
public class HomePage extends JScrollPane {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);   // 메인 블루
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);  // 배경 회색
    private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);  // 강조 텍스트
    private static final Color COLOR_DANGER = new Color(220, 38, 38);    // 하락/경고 레드
    private static final Color COLOR_SUCCESS = new Color(22, 163, 74);   // 상승/성공 그린

    public HomePage() {
        JPanel container = new JPanel();
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
        container.add(createRecommendBox());
        container.add(Box.createVerticalStrut(25));
        container.add(createEfficiencyBox());
        container.add(Box.createVerticalStrut(25));
        container.add(createSummaryBox());

        setViewportView(container);
        setBorder(null);
        getVerticalScrollBar().setUnitIncrement(16);
    }

 // [섹션 1] 유가 브리핑 박스
    private JPanel createBriefingBox() {
        JPanel card = createBaseCard("📈 오늘의 유가 한 줄 브리핑");
        
        /**
         * [API 연동 및 비즈니스 로직 상세]
         * * 1. API 호출 (Service 계층): 
         * - Opinet(오피넷) '전국 평균 유가(avgAllPrice)' API를 호출합니다.
         * - 호출 파라미터: out=json (결과 형식), code=API_KEY (오피넷 인증키).
         * * 2. 데이터 추출 및 분석:
         * - 현재 유가(price)와 전일 유가(diff)를 JSON 파싱하여 확보합니다.
         * - trend 판별: diff 값이 (+)이면 '상승', (-)이면 '하락', 0이면 '보합'으로 문자열 변환.
         * * 3. 텍스트 강조 처리 (UI):
         * - 상승 시 COLOR_DANGER (Red), 하락 시 COLOR_PRIMARY (Blue)를 동적으로 적용하도록 로직 구성.
         * * 4. 예외 및 네트워크 처리:
         * - API 호출은 별도의 Thread(혹은 SwingWorker)에서 수행하여 UI 프리징을 방지해야 합니다.
         * - 네트워크 장애 발생 시 "유가 정보를 불러올 수 없습니다."라는 기본 메시지 출력 로직이 필요합니다.
         */
        
        // 실제 구현 시 아래 변수들은 API Response 객체에서 매핑되어야 함
        String avgPrice = "1,580원"; // TODO: apiService.getTodayAvgPrice()
        String diffPrice = "20원";   // TODO: apiService.getPriceDifference()
        String trend = "하락";       // TODO: diffPrice가 음수면 "하락", 양수면 "상승"

        // HTML 태그 내 컬러 코드도 로직에 따라 #2563EB(Blue) 또는 #DC2626(Red)로 변환 필요
        JLabel content = new JLabel("<html>오늘 전국 평균 휘발유 가격은 리터당 <font color='#2563EB'><b>" + avgPrice + "</b></font>으로 " +
                                  "지난주 대비 <font color='#DC2626'><b>" + diffPrice + " " + trend + "</b></font>했습니다.</html>");
        content.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(content);
        return card;
    }

    // [섹션 2] 내 지역 추천 주유소 박스
    private JPanel createRecommendBox() {
        JPanel card = createBaseCard("📍 내 지역 추천 주유소");
        
        /**
         * [DB & API 복합 연동 포인트]
         * 1. DB: 현재 로그인된 사용자의 '선호 주소' 혹은 '최근 주유 지역' 정보 가져오기
         * 2. API: 해당 지역(시/군/구) 기반 주유소 가격 순위 리스트 호출
         * 3. 반복문을 통해 createGasRow()를 생성하여 card에 추가
         */
        card.add(createGasRow("TrueOil 강남 주유소", "서울시 강남구 역삼동", "1,550원", "1.1km"));
        card.add(Box.createVerticalStrut(12));
        card.add(createGasRow("Carset 논현 주유소", "서울시 강남구 논현동", "1,560원", "1.5km"));
        
        return card;
    }

    // [섹션 3] 가성비 추천 박스
    private JPanel createEfficiencyBox() {
        JPanel card = createBaseCard("💰 가성비 추천");
        
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setBackground(Color.WHITE);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        /**
         * [API 연동 포인트]
         * 1. 주변 반경 3~5km 이내 주유소 중 최저가 검색 (최저가 주유소)
         * 2. (가격 * 거리 가중치)를 계산하여 가장 효율적인 주유소 선별 (거리 고려 추천)
         */
        grid.add(createNestedBox("최저가 주유소", "주유소명 A", "1,520원/L", COLOR_PRIMARY));
        grid.add(createNestedBox("거리 고려 추천", "주유소명 B", "1,550원/L (500m)", COLOR_PRIMARY));
        
        card.add(grid);
        return card;
    }

    // [섹션 4] 주유비 요약 박스
    private JPanel createSummaryBox() {
        JPanel card = createBaseCard("📅 이번 달 주유비 요약");
        
        JPanel grid = new JPanel(new GridLayout(1, 4, 15, 0));
        grid.setBackground(Color.WHITE);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        /**
         * [DB 연동 포인트]
         * 1. 쿼리: SELECT COUNT(*), SUM(price), AVG(price) FROM fuel_logs 
         * WHERE user_id = ? AND date >= '2026-02-01'
         * 2. 지난달 데이터와 비교: 전월 대비 지출 퍼센트(%) 증감 로직 구현
         * 3. 결과값을 createStatBox 파라미터로 전달
         */
        grid.add(createStatBox("총 주유 횟수", "8회", COLOR_TEXT_DARK));  
        grid.add(createStatBox("총 주유 금액", "320,000원", COLOR_PRIMARY)); 
        grid.add(createStatBox("평균 가격", "1,560원", COLOR_TEXT_DARK));    
        grid.add(createStatBox("지난달 대비", "-5%", COLOR_SUCCESS)); 
        
        card.add(grid);
        return card;
    }

    /* --- UI 헬퍼 메서드 --- */
    
    private JPanel createBaseCard(String titleText) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(209, 213, 219), 1), 
            new EmptyBorder(25, 25, 25, 25)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(title);
        card.add(Box.createVerticalStrut(20));
        return card;
    }

    private JPanel createGasRow(String name, String addr, String price, String dist) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(249, 250, 251));
        row.setBorder(new CompoundBorder(new LineBorder(new Color(229, 231, 235)), new EmptyBorder(15, 20, 15, 20)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(new JLabel("<html><b>" + name + "</b></html>"));
        JLabel sub = new JLabel(addr + " | " + dist);
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
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel l = new JLabel(label); l.setForeground(Color.GRAY);
        JLabel n = new JLabel(name); n.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        JLabel v = new JLabel(val); v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        v.setForeground(valCol);

        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        n.setAlignmentX(Component.LEFT_ALIGNMENT);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);

        b.add(l); b.add(Box.createVerticalStrut(5));
        b.add(n); b.add(Box.createVerticalStrut(5));
        b.add(v);
        return b;
    }

    private JPanel createStatBox(String label, String value, Color valCol) {
        JPanel b = new JPanel(new GridLayout(2, 1, 0, 5));
        b.setBackground(new Color(252, 252, 253));
        b.setBorder(new CompoundBorder(new LineBorder(new Color(229, 231, 235)), new EmptyBorder(15, 10, 15, 10)));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel l = new JLabel(label, SwingConstants.CENTER); 
        l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        l.setForeground(new Color(75, 85, 99));
        
        JLabel v = new JLabel(value, SwingConstants.CENTER); 
        v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        v.setForeground(valCol);

        b.add(l); b.add(v);
        return b;
    }
}