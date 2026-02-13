package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EditProfileDialog extends JDialog {
    private JTextField nameF, emailF, carF, mileageF;
    private JRadioButton[] fuelRadios;
    private ButtonGroup fuelGroup;
    private JButton saveBtn, cancelBtn;

    public EditProfileDialog(Frame parent) {
        super(parent, "정보 수정", true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(420, 680);

        /* ===== 전체 배경 ===== */
        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(new Color(243, 244, 246));
        background.setBorder(new EmptyBorder(20, 20, 20, 20));

        /* ===== 카드 패널 ===== */
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(209, 213, 219), 2),
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
            public void mouseEntered(MouseEvent e) { closeLabel.setForeground(Color.RED); }
            @Override
            public void mouseExited(MouseEvent e) { closeLabel.setForeground(Color.LIGHT_GRAY); }
        });
        header.add(closeLabel, BorderLayout.EAST);

        /* ===== [DB 포인트 1] 초기 데이터 불러오기 ===== */
        // 예시 데이터: 실제로는 DB에서 조회한 값을 변수에 할당하세요.
        String currentName = "홍길동";
        String currentEmail = "hong@example.com";
        String currentCar = "12가 3456";
        String currentFuel = "휘발유";
        String currentMileage = "50000";

        /* ===== 제목 섹션 ===== */
        JLabel titleLabel = new JLabel("정보 수정");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
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
        addInput(formWrapper, "이메일", emailF = new JTextField(currentEmail), labelFont, fieldSize);
        addInput(formWrapper, "차량번호", carF = new JTextField(currentCar), labelFont, fieldSize);

        // 연료 타입 섹션
        JLabel fuelLabel = new JLabel("연료 타입");
        fuelLabel.setFont(labelFont);
        fuelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(fuelLabel);
        formWrapper.add(Box.createVerticalStrut(6));
        
        JPanel fuelPanel = createFuelPanel(currentFuel);
        fuelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(fuelPanel);
        formWrapper.add(Box.createVerticalStrut(14));

        // 주행 거리 필드
        addInput(formWrapper, "주행 거리 (km)", mileageF = new JTextField(currentMileage), labelFont, fieldSize);

        /* ===== 하단 버튼  ===== */
        saveBtn = new JButton("저장");
        saveBtn.setBackground(new Color(37, 99, 235));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(320, 45));

        saveBtn.addActionListener(e -> {
            /* ===== [DB 포인트 2] 데이터 업데이트 ===== */
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
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(6));

        tf.setMaximumSize(size);
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
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
            if (fuels[i].equals(selected)) fuelRadios[i].setSelected(true);
            fuelGroup.add(fuelRadios[i]);
            p.add(fuelRadios[i]);
        }
        return p;
    }
}