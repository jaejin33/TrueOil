package TrueOil_View;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * [Password Change Dialog]
 * 핵심 역할:
 * 1. 입력 검증: 현재 PW 확인, 새 PW와 확인 PW의 일치 여부 체크
 * 2. DB 업데이트: 비밀번호 컬럼 UPDATE (암호화 권장)
 */
public class PasswordChangeDialog extends JDialog {
    private JPasswordField currentPwF, newPwF, confirmPwF;
    private JButton saveBtn, cancelBtn;

    public PasswordChangeDialog(Frame parent) {
        super(parent, "비밀번호 변경", true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(420, 520);

        /* ===== 전체 배경 (메인 톤 유지) ===== */
        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(new Color(243, 244, 246));
        background.setBorder(new EmptyBorder(20, 20, 20, 20));

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

        /* ===== 제목 섹션 ===== */
        JLabel titleLabel = new JLabel("비밀번호 변경");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 입력 폼 영역 (JPasswordField) ===== */
        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        formWrapper.setMaximumSize(new Dimension(320, 260));

        Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        Dimension fieldSize = new Dimension(Integer.MAX_VALUE, 35);

        addPasswordInput(formWrapper, "현재 비밀번호", currentPwF = new JPasswordField(), labelFont, fieldSize);
        addPasswordInput(formWrapper, "새 비밀번호", newPwF = new JPasswordField(), labelFont, fieldSize);
        addPasswordInput(formWrapper, "새 비밀번호 확인", confirmPwF = new JPasswordField(), labelFont, fieldSize);

        /* ===== 하단 버튼 및 로직 ===== */
        saveBtn = new JButton("저장") {
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
        saveBtn.setBackground(new Color(37, 99, 235));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        saveBtn.setFocusPainted(false);
        saveBtn.setOpaque(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(320, 45));

        saveBtn.addActionListener(e -> {
            String currentPw = new String(currentPwF.getPassword());
            String newPw = new String(newPwF.getPassword());
            String confirmPw = new String(confirmPwF.getPassword());

            // 1. [검증] 빈 칸 체크
            if (currentPw.isEmpty() || newPw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "비밀번호를 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. [DB Point 1] 현재 비밀번호 대조
            // SELECT user_pw FROM members WHERE user_id = ?
            // DB에서 가져온 비밀번호와 currentPw가 일치하는지 먼저 확인해야 합니다.

            // 3. [검증] 새 비밀번호 일치 여부
            if (!newPw.equals(confirmPw)) {
                JOptionPane.showMessageDialog(this, "새 비밀번호가 일치하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 4. [DB Point 2] 최종 업데이트
            // UPDATE members SET user_pw = ? WHERE user_id = ?
            JOptionPane.showMessageDialog(this, "비밀번호가 성공적으로 변경되었습니다.");
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

    private void addPasswordInput(JPanel p, String title, JPasswordField pf, Font font, Dimension size) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(font);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(6));

        pf.setMaximumSize(size);
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        pf.setBorder(new CompoundBorder(new RoundBorder(new Color(209, 213, 219), 1, 8), new EmptyBorder(0, 10, 0, 10)));
        p.add(pf);
        p.add(Box.createVerticalStrut(14));
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