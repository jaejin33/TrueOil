package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SignupFrame extends JFrame {

    public SignupFrame() {
        setTitle("회원가입");
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(460, 780);
        setLocationRelativeTo(null);
        setResizable(false);

        /* ===== 배경 ===== */
        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(new Color(243, 244, 246));
        background.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 2),
                new EmptyBorder(20, 20, 20, 20) 
        ));

        JPanel centerWrapper = new JPanel();
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        centerWrapper.setBackground(new Color(243, 244, 246));

        /* ===== 카드 (메인 컨테이너) ===== */
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(209, 213, 219), 2),
                new EmptyBorder(24, 24, 24, 24)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(380, 700));

        /* ===== 상단 (제목 왼쪽, 뒤로가기 오른쪽) ===== */
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("회원가입");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));

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

        /* ===== 기본 정보 (필드 생성) ===== */
        JPanel basicPanel = new JPanel();
        basicPanel.setLayout(new BoxLayout(basicPanel, BoxLayout.Y_AXIS));
        basicPanel.setBackground(Color.WHITE);
        basicPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        basicPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(10, 0, 16, 0)
        ));

        JLabel basicTitle = new JLabel("기본 정보");
        basicTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        basicTitle.setForeground(new Color(55, 65, 81));

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField pwField = new JPasswordField();
        JPasswordField pwConfirmField = new JPasswordField();

        basicPanel.add(basicTitle);
        basicPanel.add(Box.createVerticalStrut(12));
        basicPanel.add(makeField("이름 *", nameField));
        basicPanel.add(makeField("이메일 *", emailField));
        basicPanel.add(makeField("비밀번호 *", pwField));
        basicPanel.add(makeField("비밀번호 확인 *", pwConfirmField));

        /* ===== 차량 정보 ===== */
        JPanel carPanel = new JPanel();
        carPanel.setLayout(new BoxLayout(carPanel, BoxLayout.Y_AXIS));
        carPanel.setBackground(Color.WHITE);
        carPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel carTitle = new JLabel("차량 정보");
        carTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        carTitle.setForeground(new Color(55, 65, 81));

        JTextField carNumberField = new JTextField();
        JComboBox<String> fuelTypeBox = new JComboBox<>(new String[]{"휘발유", "경유", "LPG", "전기", "하이브리드"});
        JTextField mileageField = new JTextField();

        JPanel mileageWrapper = new JPanel(new BorderLayout(6, 0));
        mileageWrapper.setBackground(Color.WHITE);
        mileageWrapper.add(mileageField, BorderLayout.CENTER);
        mileageWrapper.add(new JLabel("km"), BorderLayout.EAST);

        carPanel.add(carTitle);
        carPanel.add(Box.createVerticalStrut(12));
        carPanel.add(makeField("차량번호 *", carNumberField));
        carPanel.add(makeField("연료 타입 *", fuelTypeBox));
        carPanel.add(makeField("현재 주행거리 (선택)", mileageWrapper));

        /* ===== 가입 버튼 (TODO 주석 추가) ===== */
        JButton signupButton = new JButton("가입하기");
        signupButton.setBackground(new Color(37, 99, 235));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        signupButton.setFocusPainted(false);
        signupButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        signupButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        signupButton.addActionListener(e -> {
            // TODO 1: 입력 데이터 추출
            // - nameField, emailField, pwField, carNumberField 등 모든 필드 값 가져오기
            
            // TODO 2: 필수 입력값 유효성 검사
            // - 이름, 이메일, 비밀번호, 차량번호 등 '*' 표시된 항목이 비어있는지 체크
            // - 비밀번호와 비밀번호 확인 필드 값이 일치하는지 체크
            
            // TODO 3: 데이터베이스 연동 처리
            // - 이메일 중복 체크 (DB에 동일한 이메일이 있는지 확인)
            // - 회원 정보 INSERT (비밀번호는 반드시 해시 암호화하여 저장)
            
            // TODO 4: 가입 결과 처리
            // - 성공: "회원가입이 완료되었습니다" 안내 후 Login 화면으로 이동
            // - 실패: 실패 원인(중복 이메일 등) 알림창 띄우기

            JOptionPane.showMessageDialog(this, "회원가입 처리 로직 구현 위치입니다.");
        });

        /* ===== 하단 로그인 이동 ===== */
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel loginText = new JLabel("이미 계정이 있으신가요?");
        loginText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        loginText.setForeground(Color.GRAY);

        JLabel loginLink = new JLabel("로그인");
        loginLink.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        loginLink.setForeground(new Color(37, 99, 235));
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new Login().setVisible(true);
                dispose();
            }
        });

        bottomPanel.add(loginText);
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
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (field instanceof JComponent) {
            ((JComponent) field).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        panel.add(Box.createVerticalStrut(10));
        return panel;
    }
}