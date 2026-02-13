package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PhotoChangeDialog extends JDialog {
    private JLabel photoPreview;
    private JButton removeBtn;
    private JButton applyBtn;
    private JPanel btnGroup;

    public PhotoChangeDialog(Frame parent) {
        super(parent, "프로필 사진 변경", true);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(24, 24, 24, 24));

        // 1. 헤더
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        JLabel titleLbl = new JLabel("프로필 사진 변경");
        titleLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
        JButton closeBtn = new JButton("X");
        closeBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        closeBtn.setForeground(new Color(156, 163, 175));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());

        header.add(titleLbl, BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);

        // 2. 사진 프리뷰
        photoPreview = new JLabel("👤", SwingConstants.CENTER);
        photoPreview.setPreferredSize(new Dimension(120, 120));
        photoPreview.setMaximumSize(new Dimension(120, 120));
        photoPreview.setOpaque(true);
        photoPreview.setBackground(new Color(243, 244, 246));
        photoPreview.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 55));
        photoPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoPreview.setBorder(new LineBorder(new Color(229, 231, 235), 1));

        /* [DB 포인트 1: 초기 데이터 로드] 
           - 사용자가 기존에 설정한 이미지가 있다면 updatePreview() 호출 
           - 기존 이미지가 있다면 removeBtn과 applyBtn을 보이게 설정 가능
        */

        // 3. 버튼 그룹
        btnGroup = new JPanel();
        btnGroup.setLayout(new BoxLayout(btnGroup, BoxLayout.Y_AXIS));
        btnGroup.setOpaque(false);
        btnGroup.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton uploadBtn = createStyledBtn("사진 업로드", new Color(37, 99, 235), Color.WHITE, 280);

        JPanel actionRow = new JPanel();
        actionRow.setLayout(new BoxLayout(actionRow, BoxLayout.X_AXIS));
        actionRow.setOpaque(false);
        actionRow.setMaximumSize(new Dimension(280, 42));

        removeBtn = createStyledBtn("삭제", Color.WHITE, new Color(220, 38, 38), 135);
        removeBtn.setBorder(new LineBorder(new Color(252, 165, 165)));
        applyBtn = createStyledBtn("적용", new Color(22, 163, 74), Color.WHITE, 135);
        
        // 초기에는 숨김 처리
        removeBtn.setVisible(false);
        applyBtn.setVisible(false);

        actionRow.add(removeBtn);
        actionRow.add(Box.createHorizontalStrut(10));
        actionRow.add(applyBtn);

        JButton cancelBtn = createStyledBtn("취소", Color.WHITE, new Color(55, 65, 81), 280);
        cancelBtn.setBorder(new LineBorder(new Color(209, 213, 219)));

        btnGroup.add(uploadBtn);
        btnGroup.add(Box.createVerticalStrut(10));
        btnGroup.add(actionRow);
        btnGroup.add(Box.createVerticalStrut(10));
        btnGroup.add(cancelBtn);

        // [ACTION] 사진 업로드
        uploadBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String selectedPath = fileChooser.getSelectedFile().getAbsolutePath();
                updatePreview(selectedPath);
                
                // 업로드 성공 시 삭제/적용 버튼 노출 및 크기 재조절
                removeBtn.setVisible(true);
                applyBtn.setVisible(true);
                revalidate();
                pack();
                setLocationRelativeTo(parent);
            }
        });

        // [ACTION] 사진 삭제
        removeBtn.addActionListener(e -> {
            photoPreview.setIcon(null);
            photoPreview.setText("👤");
            
            // 삭제 시 액션 버튼 행을 통째로 숨기고 크기를 다시 줄임
            removeBtn.setVisible(false);
            applyBtn.setVisible(false);
            
            revalidate();
            pack(); // 이 부분이 다시 실행되어 창 크기가 작아집니다.
            setLocationRelativeTo(parent);
        });

        // [ACTION] 최종 적용
        applyBtn.addActionListener(e -> {
            /* [DB 포인트 2: 최종 데이터 저장] 
               - 현재 photoPreview의 상태를 DB에 저장
               - 마이페이지 UI 새로고침 메서드 호출
            */
            JOptionPane.showMessageDialog(this, "프로필 사진이 변경되었습니다.");
            dispose();
        });

        cancelBtn.addActionListener(e -> dispose());

        container.add(header);
        container.add(Box.createVerticalStrut(20));
        container.add(photoPreview);
        container.add(Box.createVerticalStrut(24));
        container.add(btnGroup);

        add(container);
        pack();
        setLocationRelativeTo(parent);
    }

    private void updatePreview(String path) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            photoPreview.setText("");
            photoPreview.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            photoPreview.setText("👤");
            photoPreview.setIcon(null);
        }
    }

    private JButton createStyledBtn(String text, Color bg, Color fg, int width) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(width, 42));
        b.setPreferredSize(new Dimension(width, 42));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (bg != Color.WHITE) {
            b.setBorderPainted(false);
        }
        return b;
    }
}