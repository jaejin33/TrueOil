package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class VehicleHealthSettingDialog extends JDialog {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_DANGER = new Color(239, 68, 68);

    private JButton saveBtn, cancelBtn;
    private Map<String, JTextField> cycleFieldMap = new HashMap<>();

    public VehicleHealthSettingDialog(Frame parent) {
        super(parent, "권장 주기 설정", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(700, 500);

        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(COLOR_BG_GRAY);
        background.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 2), new EmptyBorder(20, 20, 20, 20)));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(209, 213, 219), 2), new EmptyBorder(16, 24, 24, 24)));

        /* ===== 상단 헤더 ===== */
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_CARD_BG);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
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

        JLabel titleLabel = new JLabel("소모품 권장 교체 주기 설정");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel formWrapper = new JPanel(new GridLayout(2, 2, 30, 30));
        formWrapper.setBackground(COLOR_CARD_BG);
        formWrapper.setOpaque(false);

        /* * [DB POINT 1] 초기 권장 주기 데이터 로드 (SELECT)
         * - 구현 방법: SELECT 결과를 아래 cycleVal 변수에 할당하세요.
         */
        addCycleSection(formWrapper, "엔진 오일", 10000);
        addCycleSection(formWrapper, "타이어", 50000);
        addCycleSection(formWrapper, "브레이크 패드", 30000);
        addCycleSection(formWrapper, "배터리", 60000);

        /* ===== 버튼 영역 ===== */
        saveBtn = new JButton("설정 저장");
        saveBtn.setBackground(COLOR_PRIMARY);
        saveBtn.setForeground(COLOR_CARD_BG);
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(250, 50));
        
        saveBtn.addActionListener(e -> {
            /* * [DB POINT 2] 데이터 업데이트 실행 (UPDATE)
             * - cycleFieldMap.get("엔진 오일").getText() 처럼 값을 추출하여 DB에 저장하세요.
             */
            JOptionPane.showMessageDialog(this, "권장 교체 주기가 저장되었습니다.");
            dispose();
        });

        cancelBtn = new JButton("취소");
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setForeground(Color.GRAY);
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> dispose());

        card.add(header);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(40));
        card.add(formWrapper);
        card.add(Box.createVerticalStrut(40));
        card.add(saveBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(cancelBtn);

        background.add(card, BorderLayout.CENTER);
        add(background);
        setLocationRelativeTo(parent);
    }

    private void addCycleSection(JPanel p, String title, int cycleVal) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBackground(COLOR_CARD_BG);
        sectionPanel.setBorder(new CompoundBorder(new LineBorder(new Color(245, 245, 245), 1), new EmptyBorder(15, 15, 15, 15)));

        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        sectionPanel.add(sectionTitle);

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        inputRow.setOpaque(false);

        JTextField cycleField = new JTextField(String.valueOf(cycleVal), 8);
        cycleField.setHorizontalAlignment(JTextField.CENTER);
        cycleField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        cycleField.setBorder(new MatteBorder(0, 0, 2, 0, COLOR_PRIMARY));
        cycleFieldMap.put(title, cycleField);

        JLabel unitLbl = new JLabel("km");
        unitLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        inputRow.add(cycleField);
        inputRow.add(unitLbl);
        
        sectionPanel.add(inputRow);
        p.add(sectionPanel);
    }
}