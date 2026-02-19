package TrueOil_View;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class Login extends JFrame {

    public Login() {
        setTitle("TrueOil/Carset");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        /* ===== 전체 배경 ===== */
        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(new Color(243, 244, 246));
        background.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel centerWrapper = new JPanel();
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        centerWrapper.setBackground(new Color(243, 244, 246));

        /* ===== 카드 패널 ===== */
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new RoundBorder(new Color(209, 213, 219), 2, 15),
                new EmptyBorder(16, 24, 24, 24)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(360, 520));

        /* ===== 상단 헤더 ===== */
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel closeLabel = new JLabel("✕");
        closeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        closeLabel.setForeground(Color.LIGHT_GRAY);
        closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                closeLabel.setForeground(Color.RED);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeLabel.setForeground(Color.LIGHT_GRAY);
            }
        });
        header.add(closeLabel, BorderLayout.EAST);

        /* ===== 로고 영역 ===== */
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(Color.WHITE);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLabel = new JLabel("⛽");
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("TrueOil/Carset");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("저렴한 주유소를 찾아보세요");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoPanel.add(iconLabel);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(titleLabel);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(subtitleLabel);

        /* ===== 로그인 제목 ===== */
        JLabel loginTitle = new JLabel("로그인");
        loginTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 입력 폼 ===== */
        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        formWrapper.setMaximumSize(new Dimension(320, 160));

        JLabel emailLabel = new JLabel("이메일");
        emailLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passwordLabel = new JLabel("비밀번호");
        passwordLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formWrapper.add(emailLabel);
        formWrapper.add(Box.createVerticalStrut(6));
        formWrapper.add(emailField);
        formWrapper.add(Box.createVerticalStrut(14));
        formWrapper.add(passwordLabel);
        formWrapper.add(Box.createVerticalStrut(6));
        formWrapper.add(passwordField);

        /* ===== 로그인 버튼 및 로직 ===== */
        JButton loginButton = new JButton("로그인") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        loginButton.setBackground(new Color(37, 99, 235));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(320, 45));

        loginButton.addActionListener(e -> {
            // [DB Point] MEMBER 테이블 연동: SELECT pwd FROM member WHERE email = ? 로직 구현 필요
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            // [기능] 입력 검증: emailField 가 공백이거나 password 가 비어있을 시 JOptionPane 경고창 띄우기
            
            // [기능] 인증 성공 시: MainPage 생성자에 사용자 닉네임 또는 고유 ID를 인자로 전달하여 로그인 상태 유지
            new MainPage().setVisible(true);
            this.dispose(); 
        });

        /* ===== 하단 회원가입 ===== */
        JPanel signupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        signupPanel.setBackground(Color.WHITE);
        signupPanel.setMaximumSize(new Dimension(320, 20));

        JLabel signupText = new JLabel("계정이 없으신가요?");
        signupText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        signupText.setForeground(Color.GRAY);

        JLabel signupLink = new JLabel("회원가입");
        signupLink.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        signupLink.setForeground(new Color(37, 99, 235));
        signupLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        signupLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // [기능] 회원가입 완료 후 로그인 페이지로 자동 리다이렉트 되는 구조 확인
                new SignupFrame().setVisible(true);
                dispose();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                signupLink.setText("<html><u>회원가입</u></html>");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                signupLink.setText("회원가입");
            }
        });

        signupPanel.add(signupText);
        signupPanel.add(signupLink);

        /* ===== 최종 조립 ===== */
        card.add(header);
        card.add(Box.createVerticalStrut(10));
        card.add(logoPanel);
        card.add(Box.createVerticalStrut(28));
        card.add(loginTitle);
        card.add(Box.createVerticalStrut(20));
        card.add(formWrapper);
        card.add(Box.createVerticalStrut(26));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(18));
        card.add(signupPanel);

        centerWrapper.add(card);
        background.add(centerWrapper, BorderLayout.CENTER);
        add(background);
    }

    public static void main(String[] args) {
        // [API Point] 오피넷 API 키 및 DB 연결 상태(JDBC 드라이버 로딩 등)를 체크하는 Pre-Check 로직 필요
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
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