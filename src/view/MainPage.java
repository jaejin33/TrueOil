package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class MainPage extends JFrame {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
    private static final Color COLOR_BORDER = new Color(209, 213, 219);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);
    private static final Color COLOR_DIVIDER = new Color(229, 231, 235);

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
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_DIVIDER));

        JLabel logoLabel = new JLabel("⛽ TrueOil");
        logoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        logoLabel.setForeground(COLOR_TEXT_DARK);
        logoLabel.setBorder(new EmptyBorder(15, 20, 15, 20));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JPanel btnGroupPanel = new JPanel(new GridBagLayout()); 
        btnGroupPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 5, 0, 5);

        JButton logoutBtn = new JButton("로그아웃");
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setBackground(COLOR_BG_GRAY);
        logoutBtn.setForeground(COLOR_LABEL_DARK()); // 내부 가독성을 위한 처리
        logoutBtn.setFocusPainted(false);
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
        exitBtn.setBackground(COLOR_DANGER);
        exitBtn.setForeground(Color.WHITE); 
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
        gbc.insets = new Insets(0, 5, 0, 20);
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

        contentArea.add(new HomePage(), "MAIN");        
        contentArea.add(new StationPage(), "SEARCH");  
        contentArea.add(new VehiclePage(), "CAR");     
        contentArea.add(new RepairPage(), "REPAIR");   
        contentArea.add(new MyPage(), "MYPAGE");       

        mainBackgroundPanel.add(contentArea, BorderLayout.CENTER);
        setContentPane(mainBackgroundPanel);
    }

    private Color COLOR_LABEL_DARK() { return new Color(75, 85, 99); }

    public void showStationDetail(String stationName) {
        contentArea.add(new StationDetailPage(stationName), "DETAIL");
        cardLayout.show(contentArea, "DETAIL");
        clearNavSelection();
    }

    public void showStationList() {
        cardLayout.show(contentArea, "SEARCH");
        highlightNavButton("📍 주유소 찾기");
    }
    
    private void addTabButton(String text, String pageName, boolean isDefault) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(160, 50));
        btn.setBackground(Color.WHITE);
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new MatteBorder(0, 0, isDefault ? 3 : 0, 0, COLOR_PRIMARY));
        btn.setForeground(isDefault ? COLOR_PRIMARY : Color.GRAY);

        btn.addActionListener(e -> {
            clearNavSelection();
            btn.setForeground(COLOR_PRIMARY);
            btn.setBorder(new MatteBorder(0, 0, 3, 0, COLOR_PRIMARY));
            cardLayout.show(contentArea, pageName);
        });
        navBar.add(btn);
    }

    private void clearNavSelection() {
        for (Component c : navBar.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                b.setForeground(Color.GRAY);
                b.setBorder(null);
            }
        }
    }

    private void highlightNavButton(String btnText) {
        for (Component c : navBar.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                if (b.getText().equals(btnText)) {
                    b.setForeground(COLOR_PRIMARY);
                    b.setBorder(new MatteBorder(0, 0, 3, 0, COLOR_PRIMARY));
                }
            }
        }
    }
}