package view;

import javax.swing.*;
import javax.swing.border.*;

import maintenance.MaintenanceController;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;

public class VehicleHealthDetailDialog extends JDialog {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER_DEFAULT = new Color(225, 228, 232);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);

    private JTextField lastReplaceField;
    private JTextField replaceDateField; // 추가
    private JTextField costField; // 추가
    private JButton saveBtn, cancelBtn;
    private boolean isUpdated = false;
    private int updatedLastKm;
    private int itemId; // 소모품 식별용 ID 추가
    private String itemName;
    private MaintenanceController maintenanceController = new MaintenanceController(); // 컨트롤러 연결

    public VehicleHealthDetailDialog(Frame parent, int itemId, String itemName, int lastKm, int cycle) {
        super(parent, itemName + " 정보 수정", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(420, 520); // 필드 추가로 인해 높이 조절

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
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER_DEFAULT, 2),
                new EmptyBorder(16, 24, 24, 24)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

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

        /* ===== 제목 섹션 ===== */
        JLabel titleLabel = new JLabel(itemName + " 교체 이력 등록");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 입력 폼 영역 ===== */
        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(COLOR_CARD_BG);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        formWrapper.setMaximumSize(new Dimension(320, 280));

        Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        Dimension fieldSize = new Dimension(Integer.MAX_VALUE, 35);

        addInput(formWrapper, "교체 날짜 (예: 2024-01-01)", replaceDateField = new JTextField(LocalDate.now().toString()), labelFont, fieldSize);
        addInput(formWrapper, "교체 시점의 주행거리 (km)", lastReplaceField = new JTextField(String.valueOf(lastKm)), labelFont, fieldSize);
        addInput(formWrapper, "비용 (원, 생략 가능)", costField = new JTextField(), labelFont, fieldSize);

        JLabel infoLabel = new JLabel(itemName + "의 권장 교체 주기는 " + cycle + "km 입니다.");
        infoLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        infoLabel.setForeground(new Color(75, 85, 99)); 
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(infoLabel);

        /* ===== 하단 버튼 ===== */
        saveBtn = new JButton("저장");
        saveBtn.setBackground(COLOR_PRIMARY);
        saveBtn.setForeground(COLOR_CARD_BG);
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(320, 45));

        saveBtn.addActionListener(e -> {
            try {
                // 입력값 추출
                int mileage = Integer.parseInt(lastReplaceField.getText());
                String date = replaceDateField.getText().trim();
                String costText = costField.getText().trim();
                int cost = costText.isEmpty() ? 0 : Integer.parseInt(costText);

                // 유효성 검사 (날짜 포맷 등 간단 체크 가능)
                if (date.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "교체 날짜를 입력해주세요.");
                    return;
                }

                // [DB 연동] 컨트롤러 호출 
                // handleMaintenanceReplacement 안에서 이력 저장 + 건강도 재계산이 일어남
                boolean success = maintenanceController.handleMaintenanceReplacement(
                    this, itemId, itemName, date, mileage, cost
                );

                if (success) {
                    // 성공 시 상태 업데이트 및 창 닫기
                    this.updatedLastKm = mileage;
                    this.isUpdated = true;
                    dispose(); 
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "주행거리와 비용은 숫자만 입력 가능합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn = new JButton("취소");
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setForeground(Color.GRAY);
        cancelBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> dispose());

        /* ===== 카드 조립 (Strut 수치 조정으로 간격 최적화) ===== */
        card.add(header);
        card.add(Box.createVerticalStrut(5));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(formWrapper);
        card.add(Box.createVerticalStrut(15));
        card.add(saveBtn);
        card.add(Box.createVerticalStrut(10));
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
        tf.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER_DEFAULT), new EmptyBorder(0, 10, 0, 10)));
        p.add(tf);
        p.add(Box.createVerticalStrut(12)); 
    }

    public boolean isUpdated() { return isUpdated; }
    public int getLastKm() { return updatedLastKm; }
}