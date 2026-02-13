package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class MainPage extends JFrame {
    private JPanel contentArea;
    private CardLayout cardLayout;
    private JPanel navBar;

    public MainPage() {
        setTitle("TrueOil");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // [1] 상단 헤더 (로고 & 로그아웃)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

        JLabel logoLabel = new JLabel("⛽ TrueOil");
        logoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        logoLabel.setBorder(new EmptyBorder(15, 20, 15, 20));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JButton logoutBtn = new JButton("로그아웃");
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(new EmptyBorder(0, 20, 0, 20));
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.addActionListener(e -> {
            // 1. 확인 다이얼로그 띄우기 (부모를 MainPage.this로 지정)
            int confirm = JOptionPane.showConfirmDialog(
                MainPage.this, "로그아웃 하시겠습니까?", "로그아웃 확인", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                new Login().setVisible(true); 
                MainPage.this.dispose(); 
            }
        });
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        // [2] 네비게이션 탭 바 (각 페이지 전환 컨트롤)
        navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        navBar.setBackground(Color.WHITE);
        addTabButton("🏠 메인", "MAIN", true);
        addTabButton("📍 주유소 찾기", "SEARCH", false);
        addTabButton("🚗 차량 관리", "CAR", false);
        addTabButton("🔧 정비소 예약", "REPAIR", false);
        addTabButton("👤 마이페이지", "MYPAGE", false);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(headerPanel, BorderLayout.NORTH);
        topWrapper.add(navBar, BorderLayout.CENTER);
        add(topWrapper, BorderLayout.NORTH);

        // [3] 중앙 컨텐츠 영역 (CardLayout 적용)
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);

        contentArea.add(new HomePage(), "MAIN");        // 오늘의 유가 시세 정보 표시
        contentArea.add(new StationPage(), "SEARCH");  // 오피넷 전국 주유소 실시간 위치/가격 정보
        contentArea.add(new VehiclePage(), "CAR");     // 회원별 등록 차량 및 주유 이력 관리
        contentArea.add(new RepairPage(), "REPAIR");   // 정비소 목록 및 예약 스케줄 데이터
        contentArea.add(new MyPage(), "MYPAGE");       // 개인정보(PW, 이메일) 수정 기능

        add(contentArea, BorderLayout.CENTER);
    }

    /**
     * [기능] 주유소 상세 페이지 호출
     * @param stationName - [API] 선택된 주유소의 고유 ID 또는 이름을 전달받아 상세 정보 쿼리
     */
    public void showStationDetail(String stationName) {
        contentArea.add(new StationDetailPage(stationName), "DETAIL");
        cardLayout.show(contentArea, "DETAIL");
        clearNavSelection();
    }

    /**
     * [기능] 주유소 리스트로 복귀
     */
    public void showStationList() {
        cardLayout.show(contentArea, "SEARCH");
        highlightNavButton("📍 주유소 찾기");
    }

    /**
     * [기능] 네비게이션 버튼 생성 및 이벤트 설정
     */
    private void addTabButton(String text, String pageName, boolean isDefault) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(160, 50));
        btn.setBackground(Color.WHITE);
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new MatteBorder(0, 0, isDefault ? 3 : 0, 0, new Color(37, 99, 235)));
        btn.setForeground(isDefault ? new Color(37, 99, 235) : Color.GRAY);

        btn.addActionListener(e -> {
            clearNavSelection();
            btn.setForeground(new Color(37, 99, 235));
            btn.setBorder(new MatteBorder(0, 0, 3, 0, new Color(37, 99, 235)));
            cardLayout.show(contentArea, pageName);
        });
        navBar.add(btn);
    }

    // [기능] 탭 선택 해제 시각화 처리
    private void clearNavSelection() {
        for (Component c : navBar.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                b.setForeground(Color.GRAY);
                b.setBorder(null);
            }
        }
    }

    // [기능] 특정 탭 강제 활성화 (상세페이지 등에서 돌아올 때 사용)
    private void highlightNavButton(String btnText) {
        for (Component c : navBar.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                if (b.getText().equals(btnText)) {
                    b.setForeground(new Color(37, 99, 235));
                    b.setBorder(new MatteBorder(0, 0, 3, 0, new Color(37, 99, 235)));
                }
            }
        }
    }

    // 임시 패널 생성 로직 (현재는 실제 페이지 클래스로 대체됨)
    private JPanel createTempPanel(String msg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.add(new JLabel(msg));
        return p;
    }
}