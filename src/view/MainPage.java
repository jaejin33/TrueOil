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
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 900);
        setLocationRelativeTo(null);
        JPanel mainBackgroundPanel = new JPanel(new BorderLayout());
        mainBackgroundPanel.setBackground(Color.WHITE);
        mainBackgroundPanel.setBorder(new LineBorder(Color.BLACK, 2));

        // [1] 상단 헤더 (로고 & 로그아웃)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

        JLabel logoLabel = new JLabel("⛽ TrueOil");
        logoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        logoLabel.setBorder(new EmptyBorder(15, 20, 15, 20));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JPanel btnGroupPanel = new JPanel(new GridBagLayout()); 
        btnGroupPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 5, 0, 5); // 버튼 사이의 간격 추가
        Color logoutColor = new Color(243, 244, 246);
        Color exitColor = new Color(239, 68, 68);

        JButton logoutBtn = new JButton("로그아웃");
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setBackground(logoutColor);
        logoutBtn.setForeground(Color.DARK_GRAY);
        logoutBtn.setFocusPainted(false); // 클릭 시 테두리 제거
        logoutBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(MainPage.this, "로그아웃 하시겠습니까?", "로그아웃 확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new Login().setVisible(true);
                MainPage.this.dispose();
            }
        });

        JButton exitBtn = new JButton("종료");
        exitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitBtn.setBackground(exitColor);
        exitBtn.setForeground(Color.WHITE); // 빨간 배경엔 흰색 글씨
        exitBtn.setFocusPainted(false);
        exitBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        exitBtn.setBorder(new EmptyBorder(5, 15, 5, 15));
        exitBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(MainPage.this, "종료하시겠습니까?", "시스템 종료", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) System.exit(0);
        });

        gbc.gridx = 0;
        btnGroupPanel.add(logoutBtn, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 5, 0, 20); // 종료 버튼 오른쪽 여백 확보
        btnGroupPanel.add(exitBtn, gbc);
        headerPanel.add(btnGroupPanel, BorderLayout.EAST);

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
        
        mainBackgroundPanel.add(topWrapper, BorderLayout.NORTH);

        // [3] 중앙 컨텐츠 영역 (CardLayout 적용)
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);

        contentArea.add(new HomePage(), "MAIN");        // 오늘의 유가 시세 정보 표시
        contentArea.add(new StationPage(), "SEARCH");  // 오피넷 전국 주유소 실시간 위치/가격 정보
        contentArea.add(new VehiclePage(), "CAR");     // 회원별 등록 차량 및 주유 이력 관리
        contentArea.add(new RepairPage(), "REPAIR");   // 정비소 목록 및 예약 스케줄 데이터
        contentArea.add(new MyPage(), "MYPAGE");       // 개인정보(PW, 이메일) 수정 기능

        mainBackgroundPanel.add(contentArea, BorderLayout.CENTER);
        
        // 최종적으로 프레임에 루트 패널 설정
        setContentPane(mainBackgroundPanel);
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
}