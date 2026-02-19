package TrueOil_View;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * [MyPage]
 * 핵심 역할:
 * 1. DB 조회: 현재 로그인된 사용자의 상세 정보 및 활동 데이터(통계) 출력
 * 2. 화면 연결: 정보 수정 및 비밀번호 변경 다이얼로그 호출
 */
public class MyPage extends JPanel {
    public MyPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(243, 244, 246)); 
        setBorder(new EmptyBorder(30, 60, 30, 60));

        JLabel title = new JLabel("마이페이지");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(25));

        add(createProfileBox());
        add(Box.createVerticalStrut(25));
        add(createActivityBox());
    }

    // [섹션 1] 내 정보 박스 (DB 연동)
    private JPanel createProfileBox() {
        JPanel card = createCardFrame("👤 내 정보");
        
        JPanel profileHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        profileHeader.setBackground(Color.WHITE);
        profileHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel avatar = new JLabel("👤", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(80, 80));
        avatar.setOpaque(false);
        avatar.setBackground(new Color(243, 244, 246));
        avatar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
        
        JPanel nameIdTexts = new JPanel(new GridLayout(2, 1, 0, 5));
        nameIdTexts.setOpaque(false);
        
        // [DB Point] members 테이블에서 현재 세션 유저의 name, user_id 가져오기
        JLabel nameLbl = new JLabel("홍길동"); 
        nameLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        JLabel idLbl = new JLabel("회원 ID: USER12345");
        idLbl.setForeground(Color.GRAY);
        
        nameIdTexts.add(nameLbl);
        nameIdTexts.add(idLbl);
        profileHeader.add(avatar);
        profileHeader.add(nameIdTexts);
        
        card.add(profileHeader);
        card.add(Box.createVerticalStrut(25));

        // 상세 정보 데이터 행
        // [DB Point] members, cars 테이블 조인하여 email, car_num, join_date 로드
        card.add(createDataRow("✉️ 이메일", "hong@example.com"));
        card.add(Box.createVerticalStrut(10));
        card.add(createDataRow("🚗 차량번호", "12가 3456"));
        card.add(Box.createVerticalStrut(10));
        card.add(createDataRow("📅 가입일", "2025-12-15"));
        card.add(Box.createVerticalStrut(25));

        /* ===== 버튼 영역 ===== */
        JPanel btns = new JPanel(new GridLayout(1, 2, 15, 0));
        btns.setOpaque(false);
        btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton b1 = new JButton("정보 수정"); 
        styleBtn(b1);
        b1.addActionListener(e -> {
            // [기능] 다이얼로그에서 수정 완료 시, MyPage의 텍스트들을 갱신(Refresh)하는 로직 필요
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            EditProfileDialog dialog = new EditProfileDialog(parentFrame);
            dialog.setVisible(true);
        });

        JButton b2 = new JButton("비밀번호 변경"); 
        styleBtn(b2);
        b2.addActionListener(e -> {
            // [기능] 비밀번호 변경 전용 다이얼로그 호출
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            PasswordChangeDialog dialog = new PasswordChangeDialog(parentFrame);
            dialog.setVisible(true);
        });
        
        btns.add(b1);
        btns.add(b2);
        card.add(btns);

        return card;
    }

    // [섹션 2] 활동 통계 박스 (DB 연동)
    private JPanel createActivityBox() {
        JPanel card = createCardFrame("내 활동 통계");
        JPanel grid = new JPanel(new GridLayout(1, 3, 15, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // [DB Point] 
        // 1. 주유 기록: SELECT COUNT(*) FROM fuel_records WHERE user_id = ?
        // 2. 누적 주유비: SELECT SUM(total_price) FROM fuel_records WHERE user_id = ?
        // 3. 즐겨찾기: SELECT COUNT(*) FROM favorites WHERE user_id = ?
        grid.add(createStatItem("주유 기록", "32회"));
        grid.add(createStatItem("누적 주유비", "950만원"));
        grid.add(createStatItem("즐겨찾기", "5곳"));
        
        card.add(grid);
        return card;
    }

    /* --- UI 유틸리티 메서드 --- */

    private JPanel createCardFrame(String titleText) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new RoundBorder(new Color(209, 213, 219), 1, 15), new EmptyBorder(25, 25, 25, 25)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));

        JLabel t = new JLabel(titleText);
        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(20));
        return p;
    }

    private JPanel createDataRow(String label, String value) {
        JPanel r = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
            }
        };
        r.setOpaque(false);
        r.setBackground(new Color(252, 252, 253));
        r.setBorder(new CompoundBorder(new RoundBorder(new Color(229, 231, 235), 1, 8), new EmptyBorder(12, 15, 12, 15)));
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        r.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        JLabel v = new JLabel(value); 
        v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        r.add(l, BorderLayout.WEST);
        r.add(v, BorderLayout.EAST);
        return r;
    }

    private JPanel createStatItem(String label, String val) {
        JPanel b = new JPanel(new GridLayout(2, 1, 0, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        b.setOpaque(false);
        b.setBackground(new Color(250, 250, 251));
        b.setBorder(new RoundBorder(new Color(229, 231, 235), 1, 15));
        
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setForeground(Color.GRAY);
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        
        JLabel v = new JLabel(val, SwingConstants.CENTER);
        v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        
        b.add(l); b.add(v);
        return b;
    }

    private void styleBtn(JButton b) {
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        b.setBorder(new RoundBorder(new Color(209, 213, 219), 1, 8));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); 
        b.setOpaque(false);
        b.setContentAreaFilled(false);

        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                b.setBackground(new Color(249, 250, 251));
                b.setContentAreaFilled(true);
            }
            public void mouseExited(MouseEvent e) { 
                b.setBackground(Color.WHITE);
                b.setContentAreaFilled(false);
            }
        });
    }

    // 라운드 보더 클래스
    class RoundBorder implements Border {
        private Color color;
        private int thickness;
        private int radius;

        public RoundBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x + thickness/2f, y + thickness/2f, width - thickness, height - thickness, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}