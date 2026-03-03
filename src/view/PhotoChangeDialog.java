package view;

import javax.swing.*;
import javax.swing.border.*;
import user.UserController;
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
    private String currentSelectedPath = null;
    private UserController userController = new UserController();

    public PhotoChangeDialog(Frame parent) {
        super(parent, "프로필 사진 변경", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(420, 480);

        /* 1. 버튼 객체 생성 */
        removeBtn = new JButton("삭제");
        styleSecondaryBtn(removeBtn, COLOR_DANGER);
        applyBtn = new JButton("적용");
        stylePrimaryBtn(applyBtn, COLOR_SUCCESS);

        /* 2. 초기 숨김 처리 */
        removeBtn.setVisible(false);
        applyBtn.setVisible(false);

        /* 배경 패널 */
        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(COLOR_BG_GRAY);
        background.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 2),
                new EmptyBorder(20, 20, 20, 20) 
        ));

        // 창 드래그 이동
        background.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX(); mouseY = e.getY();
            }
        });
        background.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
            }
        });

        /* 카드 패널 */
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 2),
                new EmptyBorder(16, 24, 24, 24)
        ));

        /* 헤더 (종료 버튼) */
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel closeLabel = new JLabel("✕");
        closeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        closeLabel.setForeground(Color.LIGHT_GRAY);
        closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); }
        });
        header.add(closeLabel, BorderLayout.EAST);

        JLabel titleLabel = new JLabel("프로필 사진 변경");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* 사진 프리뷰 */
        photoPreview = new JLabel("👤", SwingConstants.CENTER);
        photoPreview.setPreferredSize(new Dimension(140, 140));
        photoPreview.setMaximumSize(new Dimension(140, 140));
        photoPreview.setOpaque(true);
        photoPreview.setBackground(COLOR_BG_GRAY);
        photoPreview.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 65));
        photoPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoPreview.setBorder(new LineBorder(COLOR_DIVIDER, 1));

        /* 드래그 앤 드롭 */
        photoPreview.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }
            public boolean importData(TransferSupport support) {
                try {
                    List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        currentSelectedPath = files.get(0).getAbsolutePath();
                        updatePreview(currentSelectedPath);
                        updateActionLayout(true); // 버튼 2개(삭제/적용) 모드
                        return true;
                    }
                } catch (Exception e) { e.printStackTrace(); }
                return false;
            }
        });

        /* 초기 데이터 로드 */
        String initialPath = userController.getProfileImagePath();
        updatePreview(initialPath);

        /* 하단 버튼 영역 (여기가 핵심!) */
        actionRow = new JPanel();
        actionRow.setOpaque(false);
        actionRow.setMaximumSize(new Dimension(320, 45));
        
        if (initialPath != null && !initialPath.contains("default.png")) {
            updateActionLayout(true);
        } else {
            updateActionLayout(false);
            applyBtn.setVisible(false);
        }

        JButton uploadBtn = new JButton("사진 업로드");
        stylePrimaryBtn(uploadBtn, COLOR_PRIMARY);

        /* 리스너 */
        uploadBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                currentSelectedPath = fileChooser.getSelectedFile().getAbsolutePath();
                updatePreview(currentSelectedPath);
                updateActionLayout(true);
            }
        });

        removeBtn.addActionListener(e -> {
            updatePreview("resources/images/profiles/default.png"); 
            currentSelectedPath = "DELETE_ACTION";
            updateActionLayout(false); // 적용 버튼 1개만 꽉 차게 배치
        });

        applyBtn.addActionListener(e -> {
            if (currentSelectedPath == null) return;
            String result = userController.requestProfileImageChange(currentSelectedPath);
            if (result == null) {
                JOptionPane.showMessageDialog(this, "성공적으로 변경되었습니다.");
                
                // [원래 로직 유지] MainPage 찾아서 갱신
                Window ancestor = SwingUtilities.getWindowAncestor(this);
                if (ancestor instanceof MainPage) {
                    ((MainPage) ancestor).updateProfileUI(); 
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, result, "에러", JOptionPane.ERROR_MESSAGE);
            }
        });

        /* 최종 조립 */
        card.add(header);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(30));
        card.add(photoPreview);
        card.add(Box.createVerticalStrut(35));
        card.add(uploadBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(actionRow);
        card.add(Box.createVerticalStrut(10));

        background.add(card, BorderLayout.CENTER);
        add(background);
        setLocationRelativeTo(parent);
    }

    // [버튼 너비 맞춤 핵심 메서드]
    private void updateActionLayout(boolean showDelete) {
        actionRow.removeAll();
        if (showDelete) {
            actionRow.setLayout(new GridLayout(1, 2, 10, 0)); // 2개 버튼 동일 너비
            actionRow.add(removeBtn);
            actionRow.add(applyBtn);
            removeBtn.setVisible(true);
        } else {
            actionRow.setLayout(new GridLayout(1, 1)); // 1개 버튼 전체 너비
            actionRow.add(applyBtn);
            removeBtn.setVisible(false);
        }
        applyBtn.setVisible(true);
        actionRow.revalidate();
        actionRow.repaint();
    }

    private void updatePreview(String path) {
        try {
            File imgFile = new File(path);
            if (!imgFile.exists()) path = "src/resources/images/profiles/default.png";
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            photoPreview.setText(""); 
            photoPreview.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            photoPreview.setIcon(null);
            photoPreview.setText("👤");
        }
    }

    private void stylePrimaryBtn(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(320, 45));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void styleSecondaryBtn(JButton b, Color fg) {
        b.setBackground(Color.WHITE);
        b.setForeground(fg);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(fg));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(320, 45));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}