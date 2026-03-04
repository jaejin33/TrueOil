package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class FindAccountDialog extends JDialog {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
    private static final Color COLOR_BORDER = new Color(209, 213, 219);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);

    private Point initialClick;
    private static final Dimension FIELD_SIZE = new Dimension(320, 35);
    private static final Dimension BUTTON_SIZE = new Dimension(320, 45);

    public FindAccountDialog(JFrame parent) {
        super(parent, true);
        setUndecorated(true);
        setSize(420, 680); // 비밀번호 이름 필드 제거로 높이 재조정
        setLocationRelativeTo(parent);

        /* ===== 전체 배경 ===== */
        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(COLOR_BG_GRAY);
        background.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 2),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel centerWrapper = new JPanel();
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        centerWrapper.setBackground(COLOR_BG_GRAY);

        /* ===== 카드 패널 ===== */
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 2),
                new EmptyBorder(16, 24, 24, 24)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(360, 640));

        /* ===== 상단 헤더 (오른쪽 ← 버튼) ===== */
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel backLabel = new JLabel("←");
        backLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        backLabel.setForeground(Color.LIGHT_GRAY);
        backLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dispose(); }
            @Override public void mouseEntered(MouseEvent e) { backLabel.setForeground(COLOR_PRIMARY); }
            @Override public void mouseExited(MouseEvent e) { backLabel.setForeground(Color.LIGHT_GRAY); }
        });
        header.add(backLabel, BorderLayout.EAST);

        /* ===== 타이틀 영역 ===== */
        JLabel titleLabel = new JLabel("아이디 / 비밀번호 찾기");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(COLOR_TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 입력 폼 영역 ===== */
        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- 아이디(이메일) 찾기 섹션 (이름 + 차량번호) ---
        formWrapper.add(createFieldLabel("이름"));
        formWrapper.add(Box.createVerticalStrut(6));
        JTextField tfNameForId = createStyledTextField();
        formWrapper.add(tfNameForId);
        formWrapper.add(Box.createVerticalStrut(10));

        formWrapper.add(createFieldLabel("차량 번호"));
        formWrapper.add(Box.createVerticalStrut(6));
        JTextField tfCarForId = createStyledTextField();
        formWrapper.add(tfCarForId);
        formWrapper.add(Box.createVerticalStrut(14));
        
        JButton btnFindId = createStyledButton("아이디 확인");
        formWrapper.add(btnFindId);
        
        JLabel lblIdResult = new JLabel(" ");
        lblIdResult.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblIdResult.setForeground(COLOR_PRIMARY);
        lblIdResult.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblIdResult.setBorder(new EmptyBorder(12, 0, 12, 0)); 
        
        btnFindId.addActionListener(e -> {
            String name = tfNameForId.getText().trim();
            String carNum = tfCarForId.getText().trim();
            if(!name.isEmpty() && !carNum.isEmpty()) {
                // DB 포인트: WHERE name = ? AND car_number = ?
                lblIdResult.setText("등록된 아이디: user****@gmail.com");
            }
        });
        formWrapper.add(lblIdResult);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(320, 1));
        formWrapper.add(sep);
        formWrapper.add(Box.createVerticalStrut(20));

        // --- 비밀번호 찾기 섹션 (아이디 + 차량번호) ---
        formWrapper.add(createFieldLabel("아이디 (이메일)"));
        formWrapper.add(Box.createVerticalStrut(6));
        JTextField tfEmailForPw = createStyledTextField();
        formWrapper.add(tfEmailForPw);
        formWrapper.add(Box.createVerticalStrut(10));

        formWrapper.add(createFieldLabel("차량 번호"));
        formWrapper.add(Box.createVerticalStrut(6));
        JTextField tfCarForPw = createStyledTextField();
        formWrapper.add(tfCarForPw);
        formWrapper.add(Box.createVerticalStrut(14));

        JButton btnFindPw = createStyledButton("비밀번호 확인");
        formWrapper.add(btnFindPw);

        JLabel lblPwResult = new JLabel(" ");
        lblPwResult.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblPwResult.setForeground(COLOR_PRIMARY);
        lblPwResult.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPwResult.setBorder(new EmptyBorder(12, 0, 0, 0));

        btnFindPw.addActionListener(e -> {
            String email = tfEmailForPw.getText().trim();
            String carNum = tfCarForPw.getText().trim();
            if(!email.isEmpty() && !carNum.isEmpty()) {
                // DB 포인트: WHERE email = ? AND car_number = ?
                lblPwResult.setText("비밀번호: 12**56 (임시 발급됨)");
            }
        });
        formWrapper.add(lblPwResult);

        /* ===== 최종 조립 ===== */
        card.add(header);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(formWrapper);

        centerWrapper.add(card);
        background.add(centerWrapper, BorderLayout.CENTER);
        add(background);

        /* ===== 드래그 로직 ===== */
        background.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }
        });
        background.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(getLocation().x + e.getX() - initialClick.x, getLocation().y + e.getY() - initialClick.y);
            }
        });
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        l.setForeground(COLOR_TEXT_DARK);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(320, 20));
        return l;
    }

    private JTextField createStyledTextField() {
        JTextField t = new JTextField();
        t.setMaximumSize(FIELD_SIZE);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        return t;
    }

    private JButton createStyledButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(COLOR_PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(BUTTON_SIZE);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }
}