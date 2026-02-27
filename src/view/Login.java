package view;

import javax.swing.*;
import javax.swing.border.*;

import user.SessionManager;
import user.UserController;
import user.dto.UserSessionDto;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Login extends JFrame {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
    private static final Color COLOR_BORDER = new Color(209, 213, 219);
    private static final Color COLOR_DANGER = new Color(239, 68, 68); 
    
    public Login() {
        setTitle("TrueOil");
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(COLOR_BG_GRAY);
        background.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 2),
                new EmptyBorder(20, 20, 20, 20) 
        ));

        JPanel centerWrapper = new JPanel();
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        centerWrapper.setBackground(COLOR_BG_GRAY);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 2),
                new EmptyBorder(16, 24, 24, 24)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(360, 520));

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
                closeLabel.setForeground(COLOR_DANGER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeLabel.setForeground(Color.LIGHT_GRAY);
            }
        });
        header.add(closeLabel, BorderLayout.EAST);

        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(Color.WHITE);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLabel = new JLabel("⛽");
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("TrueOil");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("저렴한 주유소를 찾아보세요");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.BLACK);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoPanel.add(iconLabel);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(titleLabel);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(subtitleLabel);

        JLabel loginTitle = new JLabel("로그인");
        loginTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        loginTitle.setForeground(COLOR_TEXT_DARK);
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        formWrapper.setMaximumSize(new Dimension(320, 160));

        JLabel emailLabel = new JLabel("이메일");
        emailLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        emailLabel.setForeground(COLOR_TEXT_DARK);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passwordLabel = new JLabel("비밀번호");
        passwordLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        passwordLabel.setForeground(COLOR_TEXT_DARK);
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

        JButton loginButton = new JButton("로그인");
        loginButton.setBackground(COLOR_PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(320, 45));

        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            UserController controller = new UserController();
            UserSessionDto sessionUser = controller.handleLogin(this, email, password);

            if (sessionUser != null) {
                SessionManager.setLoginUser(sessionUser);
                new MainPage().setVisible(true);
                this.dispose(); 
            }
        });

        JPanel signupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        signupPanel.setBackground(Color.WHITE);
        signupPanel.setMaximumSize(new Dimension(320, 20));

        JLabel signupText = new JLabel("계정이 없으신가요?");
        signupText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        signupText.setForeground(Color.GRAY);

        JLabel signupLink = new JLabel("회원가입");
        signupLink.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        signupLink.setForeground(COLOR_PRIMARY);
        signupLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        signupLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new SignupDialog().setVisible(true);
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
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}




