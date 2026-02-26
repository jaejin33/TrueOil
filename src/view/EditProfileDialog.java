package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*; // DB 연동을 위한 임포트

public class EditProfileDialog extends JDialog {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
    private static final Color COLOR_BORDER = new Color(209, 213, 219);
    private static final Color COLOR_DANGER = new Color(220, 38, 38);
    private static final Color COLOR_LABEL = new Color(55, 65, 81);

    private JTextField nameF, emailF, carF, mileageF;
    private JRadioButton[] fuelRadios;
    private ButtonGroup fuelGroup;
    private JButton saveBtn, cancelBtn;

    public EditProfileDialog(Frame parent) {
        super(parent, "정보 수정", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(420, 680);

        /**
         * [DB 포인트 1: 초기 데이터 로드 로직]
         * 1. 세션이나 전역 변수에서 현재 로그인한 사용자의 고유 키(Email)를 가져옵니다.
         * 2. SQL 실행: 
         * SELECT m.name, m.email, c.car_number, c.fuel_type, c.mileage 
         * FROM member m JOIN car_info c ON m.email = c.user_email 
         * WHERE m.email = ?;
         * 3. ResultSet에서 꺼낸 값을 아래 current... 변수들에 할당합니다.
         */
        String currentName = "홍길동";     // rs.getString("name")
        String currentEmail = "hong@example.com"; // rs.getString("email")
        String currentCar = "12가 3456";  // rs.getString("car_number")
        String currentFuel = "휘발유";    // rs.getString("fuel_type")
        String currentMileage = "50000"; // rs.getString("mileage")

        /* ===== 전체 배경 ===== */
        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(COLOR_BG_GRAY);
        background.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 2),
                new EmptyBorder(20, 20, 20, 20) 
        ));

        /* ===== 카드 패널 ===== */
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 2),
                new EmptyBorder(16, 24, 24, 24)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 상단 헤더 (우측 종료 버튼) ===== */
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
            public void mouseClicked(MouseEvent e) { dispose(); }
            @Override
            public void mouseEntered(MouseEvent e) { closeLabel.setForeground(COLOR_DANGER); }
            @Override
            public void mouseExited(MouseEvent e) { closeLabel.setForeground(Color.LIGHT_GRAY); }
        });
        header.add(closeLabel, BorderLayout.EAST);

        /* ===== 제목 섹션 ===== */
        JLabel titleLabel = new JLabel("정보 수정");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(COLOR_TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 입력 폼 영역 ===== */
        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        formWrapper.setMaximumSize(new Dimension(320, 400));

        Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        Dimension fieldSize = new Dimension(Integer.MAX_VALUE, 35);

        addInput(formWrapper, "이름", nameF = new JTextField(currentName), labelFont, fieldSize);
        addInput(formWrapper, "이메일 (수정 불가)", emailF = new JTextField(currentEmail), labelFont, fieldSize);
        emailF.setEditable(false); // PK 수정을 방지하기 위해 비활성화 권장
        addInput(formWrapper, "차량번호", carF = new JTextField(currentCar), labelFont, fieldSize);

        JLabel fuelLabel = new JLabel("연료 타입");
        fuelLabel.setFont(labelFont);
        fuelLabel.setForeground(COLOR_LABEL);
        fuelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(fuelLabel);
        formWrapper.add(Box.createVerticalStrut(6));
        
        JPanel fuelPanel = createFuelPanel(currentFuel);
        fuelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(fuelPanel);
        formWrapper.add(Box.createVerticalStrut(14));

        addInput(formWrapper, "주행 거리 (km)", mileageF = new JTextField(currentMileage), labelFont, fieldSize);

        /* ===== 하단 저장 버튼 및 DB 업데이트 로직 ===== */
        saveBtn = new JButton("저장");
        saveBtn.setBackground(COLOR_PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(320, 45));

        saveBtn.addActionListener(e -> {
            /**
             * [DB 포인트 2: 정보 업데이트 트랜잭션 로직]
             * * 1. 변수 추출:
             * String newName = nameF.getText();
             * String newCarNum = carF.getText();
             * int newMileage = Integer.parseInt(mileageF.getText());
             * String selectedFuel = ""; 
             * for(JRadioButton rb : fuelRadios) { if(rb.isSelected()) selectedFuel = rb.getText(); }
             * * 2. 트랜잭션 시작:
             * Connection conn = DBConnection.getConnection();
             * conn.setAutoCommit(false);
             * * 3. 실행:
             * (1) UPDATE member SET name = ? WHERE email = ?
             * (2) UPDATE car_info SET car_number = ?, fuel_type = ?, mileage = ? WHERE user_email = ?
             * * 4. 마무리:
             * 모두 성공 시 conn.commit(); 
             * 실패 시 conn.rollback(); 알림창 띄우기.
             */
            JOptionPane.showMessageDialog(this, "성공적으로 수정되었습니다.");
            dispose();
        });

        cancelBtn = new JButton("취소");
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setForeground(Color.GRAY);
        cancelBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> dispose());

        /* ===== 카드 조립 ===== */
        card.add(header);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(25));
        card.add(formWrapper);
        card.add(Box.createVerticalStrut(10));
        card.add(saveBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(cancelBtn);

        background.add(card, BorderLayout.CENTER);
        add(background);
        setLocationRelativeTo(parent);
    }

    private void addInput(JPanel p, String title, JTextField tf, Font font, Dimension size) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(font);
        lbl.setForeground(COLOR_LABEL);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(6));

        tf.setMaximumSize(size);
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1),
            new EmptyBorder(0, 10, 0, 10)
        ));
        p.add(tf);
        p.add(Box.createVerticalStrut(14));
    }

    private JPanel createFuelPanel(String selected) {
        JPanel p = new JPanel(new GridLayout(2, 3, 5, 5));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(320, 55));
        String[] fuels = {"휘발유", "경유", "LPG", "전기", "하이브리드", "기타"};
        fuelRadios = new JRadioButton[fuels.length];
        fuelGroup = new ButtonGroup();

        for (int i = 0; i < fuels.length; i++) {
            fuelRadios[i] = new JRadioButton(fuels[i]);
            fuelRadios[i].setBackground(Color.WHITE);
            fuelRadios[i].setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            fuelRadios[i].setForeground(COLOR_LABEL);
            if (fuels[i].equals(selected)) fuelRadios[i].setSelected(true);
            fuelGroup.add(fuelRadios[i]);
            p.add(fuelRadios[i]);
        }
        return p;
    }
}