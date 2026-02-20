package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class VehicleHealthDetailDialog extends JDialog {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER_DEFAULT = new Color(225, 228, 232);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);

    private JTextField lastReplaceField;
    private JButton saveBtn, cancelBtn;
    private boolean isUpdated = false;
    private int updatedLastKm;

    public VehicleHealthDetailDialog(Frame parent, String itemName, int lastKm, int cycle) {
        super(parent, itemName + " 정보 수정", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(420, 380);

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

        /* ===== 상단 헤더 (✕ 버튼) ===== */
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
        JLabel titleLabel = new JLabel(itemName + " 정보 수정");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 입력 폼 영역 ===== */
        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(COLOR_CARD_BG);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        formWrapper.setMaximumSize(new Dimension(320, 100));

        Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        Dimension fieldSize = new Dimension(Integer.MAX_VALUE, 35);

        addInput(formWrapper, "마지막 교체 시점 (km)", lastReplaceField = new JTextField(String.valueOf(lastKm)), labelFont, fieldSize);
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
                updatedLastKm = Integer.parseInt(lastReplaceField.getText());

                /* [DB Point] cycle은 기존값을 그대로 사용하거나 필요한 쿼리에 적용
                 * String sql = "UPDATE maintenance SET last_replace_km = ? WHERE item_name = ?";
                 * try (Connection conn = DBUtil.getConnection(); 
                 * PreparedStatement pstmt = conn.prepareStatement(sql)) {
                 * pstmt.setInt(1, updatedLastKm);
                 * pstmt.setString(2, itemName);
                 * pstmt.executeUpdate();
                 * } catch (SQLException ex) { ex.printStackTrace(); }
                 */

                JOptionPane.showMessageDialog(this, itemName + " 정보가 정상적으로 저장되었습니다.", "저장 완료", JOptionPane.INFORMATION_MESSAGE);
                isUpdated = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "숫자만 입력 가능합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
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