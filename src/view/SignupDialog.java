package view;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import user.UserController;
import user.dto.UserDto;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SignupDialog extends JFrame {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
    private static final Color COLOR_BORDER = new Color(209, 213, 219);
    private static final Color COLOR_DANGER = new Color(220, 38, 38);
    private static final Color COLOR_DIVIDER = new Color(229, 231, 235);

    private Point initialClick;

    public SignupDialog() {
        setTitle("회원가입");
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(460, 800);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(COLOR_BG_GRAY);
        background.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 2), new EmptyBorder(20, 20, 20, 20)));

        background.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });
        background.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });

        JPanel centerWrapper = new JPanel();
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        centerWrapper.setBackground(COLOR_BG_GRAY);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 2), new EmptyBorder(24, 24, 24, 24)));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(380, 750));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("회원가입");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(COLOR_TEXT_DARK);

        JLabel backLabel = new JLabel("←");
        backLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        backLabel.setForeground(Color.GRAY);
        backLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new Login().setVisible(true);
                dispose();
            }
        });

        header.add(title, BorderLayout.WEST);
        header.add(backLabel, BorderLayout.EAST);

        /* ----- 1. 기본 정보 영역 ----- */
        JPanel basicPanel = new JPanel();
        basicPanel.setLayout(new BoxLayout(basicPanel, BoxLayout.Y_AXIS));
        basicPanel.setBackground(Color.WHITE);
        basicPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        basicPanel.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, COLOR_DIVIDER), new EmptyBorder(10, 0, 16, 0)));

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField pwField = new JPasswordField();
        JPasswordField pwConfirmField = new JPasswordField();

        JLabel pwStatusLabel = new JLabel(" ");
        pwStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        pwStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        DocumentListener pwListener = new DocumentListener() {
            public void check() {
                String pw = new String(pwField.getPassword());
                String confirm = new String(pwConfirmField.getPassword());
                if (pw.isEmpty() || confirm.isEmpty()) { pwStatusLabel.setText(" "); } 
                else if (pw.equals(confirm)) {
                    pwStatusLabel.setText("✓ 비밀번호가 서로 일치합니다.");
                    pwStatusLabel.setForeground(COLOR_PRIMARY);
                } else {
                    pwStatusLabel.setText("✕ 비밀번호가 서로 일치하지 않습니다.");
                    pwStatusLabel.setForeground(COLOR_DANGER);
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { check(); }
            @Override public void removeUpdate(DocumentEvent e) { check(); }
            @Override public void changedUpdate(DocumentEvent e) { check(); }
        };
        pwField.getDocument().addDocumentListener(pwListener);
        pwConfirmField.getDocument().addDocumentListener(pwListener);

        basicPanel.add(new JLabel("기본 정보") {{ setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14)); setForeground(COLOR_TEXT_DARK); }});
        basicPanel.add(Box.createVerticalStrut(12));
        basicPanel.add(makeField("이름 *", nameField));
        basicPanel.add(makeField("이메일 *", emailField));
        basicPanel.add(makeField("비밀번호 *", pwField));
        basicPanel.add(makeField("비밀번호 확인 *", pwConfirmField));
        basicPanel.add(pwStatusLabel);

        /* ----- 2. 차량 정보 영역 ----- */
        JPanel carPanel = new JPanel();
        carPanel.setLayout(new BoxLayout(carPanel, BoxLayout.Y_AXIS));
        carPanel.setBackground(Color.WHITE);
        carPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField carNumberField = new JTextField();
        JComboBox<String> fuelTypeBox = new JComboBox<>(new String[]{"휘발유", "경유", "LPG", "고급휘발유", "등유"});
        JTextField mileageField = new JTextField();
        JPanel mileageWrapper = new JPanel(new BorderLayout(6, 0));
        mileageWrapper.setBackground(Color.WHITE);
        mileageWrapper.add(mileageField, BorderLayout.CENTER);
        mileageWrapper.add(new JLabel("km") {{ setForeground(Color.GRAY); }}, BorderLayout.EAST);

        carPanel.add(new JLabel("차량 정보") {{ setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14)); setForeground(COLOR_TEXT_DARK); }});
        carPanel.add(Box.createVerticalStrut(12));
        carPanel.add(makeField("차량번호 *", carNumberField));
        carPanel.add(makeField("연료 타입 *", fuelTypeBox));
        carPanel.add(makeField("현재 주행거리 (선택)", mileageWrapper));

        /* ----- 3. 가입 버튼 (DB 연동 핵심 포인트) ----- */
        JButton signupButton = new JButton("가입하기");
        signupButton.setBackground(COLOR_PRIMARY);
        signupButton.setForeground(Color.WHITE);
        signupButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        signupButton.setFocusPainted(false);
        signupButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        signupButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        signupButton.addActionListener(e -> {
            // 1. 데이터 수집 (View의 역할)
            UserDto userDto = new UserDto();
            userDto.setName(nameField.getText().trim());
            userDto.setEmail(emailField.getText().trim());
            userDto.setPassword(new String(pwField.getPassword()));
            userDto.setCarNumber(carNumberField.getText().trim());
            userDto.setFuelType((String) fuelTypeBox.getSelectedItem());
            
            // 주행거리 변환 로직 생략 (유효성 검사는 여기서 하거나 유틸로 분리)
            int mileage = mileageField.getText().isEmpty() ? 0 : Integer.parseInt(mileageField.getText());
            userDto.setCurrentMileage(mileage);

            // 2. Controller에게 처리를 맡김 (View는 결과만 받음)
            UserController controller = new UserController();
            if (controller.handleSignup(this, userDto)) {
                JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다!");
                new Login().setVisible(true);
                dispose();
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel loginLink = new JLabel("로그인");
        loginLink.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        loginLink.setForeground(COLOR_PRIMARY);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new Login().setVisible(true);
                dispose();
            }
        });

        bottomPanel.add(new JLabel("이미 계정이 있으신가요?") {{ setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12)); setForeground(Color.GRAY); }});
        bottomPanel.add(loginLink);

        card.add(header);
        card.add(Box.createVerticalStrut(20));
        card.add(basicPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(carPanel);
        card.add(Box.createVerticalStrut(30));
        card.add(signupButton);
        card.add(Box.createVerticalStrut(15));
        card.add(bottomPanel);

        centerWrapper.add(card);
        background.add(centerWrapper, BorderLayout.CENTER);
        add(background);
    }

    private JPanel makeField(String labelText, Component field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setForeground(Color.GRAY);
        if (field instanceof JComponent) { ((JComponent) field).setAlignmentX(Component.LEFT_ALIGNMENT); }
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        panel.add(Box.createVerticalStrut(10));
        return panel;
    }
}