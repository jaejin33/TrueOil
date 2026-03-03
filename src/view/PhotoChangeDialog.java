package view;

import javax.swing.*;
import javax.swing.border.*;

import user.UserController;
import user.dto.UserDto;

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
    private String currentSelectedPath = null; // 현재 선택된 임시 파일 경로
    private UserController userController = new UserController();

    public PhotoChangeDialog(Frame parent) {
        super(parent, "프로필 사진 변경", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(420, 480);

        /* 1. 버튼 객체들 먼저 생성 (순서가 가장 중요합니다) */
        removeBtn = new JButton("삭제");
        styleSecondaryBtn(removeBtn, COLOR_DANGER);
        applyBtn = new JButton("적용");
        stylePrimaryBtn(applyBtn, COLOR_SUCCESS);

        /* 2. 초기에는 버튼 숨김 처리 */
        removeBtn.setVisible(false);
        applyBtn.setVisible(false);

        /* ===== 전체 배경 설정 ===== */
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

        /* ===== 카드 패널 설정 ===== */
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

        /* ===== 사진 프리뷰 영역 및 드래그 앤 드롭 ===== */
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
                            currentSelectedPath = file.getAbsolutePath(); // 경로 저장
                            updatePreview(currentSelectedPath);
                            removeBtn.setVisible(true);
                            applyBtn.setVisible(true);
                            revalidate(); repaint();
                            return true;
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
                return false;
            }
        });

        /* 3. [DB Point 1: 초기 데이터 로드] */
        String initialPath = userController.getProfileImagePath();
        
        updatePreview(initialPath);

        if (initialPath != null && !initialPath.contains("default.png")) {
            removeBtn.setVisible(true);
            applyBtn.setVisible(true);
        } else {
            removeBtn.setVisible(false);
            applyBtn.setVisible(false);
        }
        
        /* ===== 버튼 영역 및 조립 ===== */
        JButton uploadBtn = new JButton("사진 업로드");
        stylePrimaryBtn(uploadBtn, COLOR_PRIMARY);
        
        actionRow = new JPanel();
        actionRow.setLayout(new BoxLayout(actionRow, BoxLayout.X_AXIS));
        actionRow.setOpaque(false);
        actionRow.setMaximumSize(new Dimension(320, 45));

        actionRow.add(removeBtn);
        actionRow.add(Box.createHorizontalStrut(10));
        actionRow.add(applyBtn);

        /* ===== 액션 리스너 설정 ===== */
        uploadBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("이미지 파일", "jpg", "png", "jpeg"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                currentSelectedPath = fileChooser.getSelectedFile().getAbsolutePath();
                updatePreview(currentSelectedPath);
                removeBtn.setVisible(true);
                applyBtn.setVisible(true);
                revalidate(); repaint();
            }
        });

        removeBtn.addActionListener(e -> {

            String defaultPath = "resources/images/profiles/default.png";
            
            // String defaultPath = userController.getProfileDefaultPath(); // (필요시 추가)

            // 프리뷰를 이모지가 아닌 파일 이미지로 업데이트
            updatePreview(defaultPath); 
            
            // 상태 저장 및 버튼 제어
            currentSelectedPath = "DELETE_ACTION"; // 서버에 "삭제됨"을 알릴 신호
            removeBtn.setVisible(false);
            applyBtn.setVisible(true);
            
            revalidate(); 
            repaint();
        });

        applyBtn.addActionListener(e -> {
            if (currentSelectedPath == null) {
                JOptionPane.showMessageDialog(this, "사진을 먼저 선택하거나 드래그해 주세요.");
                return;
            }
            String errorMessage = userController.requestProfileImageChange(currentSelectedPath);
            if (errorMessage == null) {
                JOptionPane.showMessageDialog(this, "프로필 사진이 성공적으로 변경되었습니다.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, errorMessage, "변경 실패", JOptionPane.ERROR_MESSAGE);
            }
        });

        /* ===== 최종 카드 조립 ===== */
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

    private void updatePreview(String path) {
        try {
            File imgFile = new File(path);
            
            if (!imgFile.exists()) {
                path = "resources/images/profiles/default.png";
                if (!new File(path).exists()) {
                    path = "src/resources/images/profiles/default.png";
                }
            }

            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            photoPreview.setText(""); // 이모지 텍스트 지우기
            photoPreview.setIcon(new ImageIcon(img));
            
        } catch (Exception e) {
            // 정말로 파일이 하나도 없을 때만 최후의 수단으로 이모지 표시
            photoPreview.setIcon(null);
            photoPreview.setText("👤");
            System.err.println("❌ 프리뷰 로딩 실패: " + e.getMessage());
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