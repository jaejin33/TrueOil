package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public class PhotoChangeDialog extends JDialog {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
    private static final Color COLOR_BORDER = new Color(209, 213, 219);
    private static final Color COLOR_DANGER = new Color(220, 38, 38);
    private static final Color COLOR_SUCCESS = new Color(22, 163, 74);
    private static final Color COLOR_DIVIDER = new Color(229, 231, 235);

    private JLabel photoPreview;
    private JButton removeBtn;
    private JButton applyBtn;
    private JPanel actionRow;

    private int mouseX, mouseY;

    public PhotoChangeDialog(Frame parent) {
        super(parent, "프로필 사진 변경", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(420, 480);

        /* ===== 전체 배경 ===== */
        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(COLOR_BG_GRAY);
        background.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 2),
                new EmptyBorder(20, 20, 20, 20) 
        ));

        background.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        background.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();
                setLocation(x - mouseX, y - mouseY);
            }
        });

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
        JLabel titleLabel = new JLabel("프로필 사진 변경");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(COLOR_TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 사진 프리뷰 영역 ===== */
        photoPreview = new JLabel("👤", SwingConstants.CENTER);
        photoPreview.setPreferredSize(new Dimension(140, 140));
        photoPreview.setMaximumSize(new Dimension(140, 140));
        photoPreview.setOpaque(true);
        photoPreview.setBackground(COLOR_BG_GRAY);
        photoPreview.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 65));
        photoPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoPreview.setBorder(new LineBorder(COLOR_DIVIDER, 1));

        photoPreview.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    Transferable t = support.getTransferable();
                    List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (files.size() > 0) {
                        File file = files.get(0);
                        String path = file.getAbsolutePath().toLowerCase();
                        if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".jpeg")) {
                            updatePreview(file.getAbsolutePath());
                            removeBtn.setVisible(true);
                            applyBtn.setVisible(true);
                            revalidate();
                            repaint();
                            return true;
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
                return false;
            }
        });

        /**
         * [DB Point 1: 초기 데이터 로드] 
         * - SELECT profile_path FROM members WHERE user_id = ?
         * - 사용자가 기존에 설정한 이미지가 있다면 updatePreview() 호출 
         * - 기존 이미지가 있다면 removeBtn과 applyBtn을 보이게 설정 가능
         */

        /* ===== 버튼 영역 ===== */
        JButton uploadBtn = new JButton("사진 업로드");
        stylePrimaryBtn(uploadBtn, COLOR_PRIMARY);
        
        actionRow = new JPanel();
        actionRow.setLayout(new BoxLayout(actionRow, BoxLayout.X_AXIS));
        actionRow.setOpaque(false);
        actionRow.setMaximumSize(new Dimension(320, 45));

        removeBtn = new JButton("삭제");
        styleSecondaryBtn(removeBtn, COLOR_DANGER);
        applyBtn = new JButton("적용");
        stylePrimaryBtn(applyBtn, COLOR_SUCCESS);
        
        // 초기에는 숨김 처리
        removeBtn.setVisible(false);
        applyBtn.setVisible(false);

        actionRow.add(removeBtn);
        actionRow.add(Box.createHorizontalStrut(10));
        actionRow.add(applyBtn);

        /* ===== 액션 리스너 ===== */
        uploadBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String selectedPath = fileChooser.getSelectedFile().getAbsolutePath();
                updatePreview(selectedPath);
                removeBtn.setVisible(true);
                applyBtn.setVisible(true);
                revalidate();
                repaint();
            }
        });

        removeBtn.addActionListener(e -> {
            photoPreview.setIcon(null);
            photoPreview.setText("👤");
            removeBtn.setVisible(false);
            applyBtn.setVisible(false);
            revalidate();
            repaint();
        });

        applyBtn.addActionListener(e -> {
            /**
             * [DB Point 2: 최종 데이터 저장] 
             * - UPDATE members SET profile_path = ? WHERE user_id = ?
             * - 현재 photoPreview의 상태를 DB에 저장
             * - 마이페이지 UI 새로고침 메서드 호출
             */
            JOptionPane.showMessageDialog(this, "프로필 사진이 변경되었습니다.");
            dispose();
        });

        /* ===== 카드 조립 ===== */
        card.add(header);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(30));
        card.add(photoPreview);
        card.add(Box.createVerticalStrut(35));
        card.add(uploadBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(actionRow);
        card.add(Box.createVerticalStrut(10)); // 하단 여백

        background.add(card, BorderLayout.CENTER);
        add(background);
        setLocationRelativeTo(parent);
    }

    private void updatePreview(String path) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            photoPreview.setText("");
            photoPreview.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            photoPreview.setText("👤");
            photoPreview.setIcon(null);
        }
    }

    private void stylePrimaryBtn(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(320, 45));
    }

    private void styleSecondaryBtn(JButton b, Color fg) {
        b.setBackground(Color.WHITE);
        b.setForeground(fg);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(fg));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(320, 45));
    }
}