package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MyPage extends JScrollPane {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
    private static final Color COLOR_BORDER = new Color(209, 213, 219);
    private static final Color COLOR_DIVIDER = new Color(229, 231, 235);
    private static final Color COLOR_ROW_BG = new Color(252, 252, 253);
    private static final Color COLOR_DANGER = new Color(220, 38, 38);

    private JPanel contentPanel;
    private JPanel listPanel;

    public MyPage() {
        setBorder(null);
        getVerticalScrollBar().setUnitIncrement(20);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_BG_GRAY); 
        contentPanel.setBorder(new EmptyBorder(40, 60, 40, 60));

        refreshPage();
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                refreshPage();
            }
        });

        setViewportView(contentPanel);
    }

    public void refreshPage() {
        contentPanel.removeAll();

        // 타이틀
        JLabel title = new JLabel("마이페이지");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setForeground(COLOR_TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(title);
        contentPanel.add(Box.createVerticalStrut(30));

        // [섹션 1] 내 정보
        contentPanel.add(createProfileBox());
        contentPanel.add(Box.createVerticalStrut(25));

        // [섹션 2] 예약 현황
        contentPanel.add(createReservationBox());
        contentPanel.add(Box.createVerticalStrut(25));

        // [섹션 4] 회원 탈퇴 (하단 배치)
        contentPanel.add(createWithdrawalPanel());
        contentPanel.add(Box.createVerticalStrut(20));

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createProfileBox() {
        JPanel card = createCardFrame("👤 내 정보");
        
        /** [DB 포인트 1: 사용자 정보 로드] */
        JPanel profileHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        profileHeader.setBackground(Color.WHITE);
        profileHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel avatar = new JLabel("👤", SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(80, 80));
        avatar.setOpaque(true);
        avatar.setBackground(COLOR_BG_GRAY);
        avatar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
        avatar.setBorder(new LineBorder(COLOR_DIVIDER, 1));
        
        JPanel infoAndBtnTexts = new JPanel();
        infoAndBtnTexts.setLayout(new BoxLayout(infoAndBtnTexts, BoxLayout.Y_AXIS));
        infoAndBtnTexts.setOpaque(false);
        
        JLabel nameLbl = new JLabel("홍길동"); 
        nameLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        nameLbl.setForeground(COLOR_TEXT_DARK);
        
        JLabel idLbl = new JLabel("회원 ID: USER12345");
        idLbl.setForeground(Color.GRAY);
        idLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        JButton changePhotoBtn = new JButton("📷 사진 변경");
        changePhotoBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        changePhotoBtn.setBackground(Color.WHITE);
        changePhotoBtn.setFocusPainted(false);
        changePhotoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePhotoBtn.setBorder(new CompoundBorder(new LineBorder(COLOR_DIVIDER), new EmptyBorder(3, 8, 3, 8)));
        
        changePhotoBtn.addActionListener(e -> {
            /** [DB 포인트 2: 프로필 사진 업데이트] */
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            new PhotoChangeDialog(parentFrame).setVisible(true);
        });

        infoAndBtnTexts.add(nameLbl);
        infoAndBtnTexts.add(Box.createVerticalStrut(4));
        infoAndBtnTexts.add(idLbl);
        infoAndBtnTexts.add(Box.createVerticalStrut(8));
        infoAndBtnTexts.add(changePhotoBtn); 

        profileHeader.add(avatar);
        profileHeader.add(infoAndBtnTexts);
        
        card.add(profileHeader);
        card.add(Box.createVerticalStrut(25));

        card.add(createDataRow("✉️ 이메일", "hong@example.com"));
        card.add(Box.createVerticalStrut(10));
        card.add(createDataRow("🚗 차량번호", "12가 3456"));
        card.add(Box.createVerticalStrut(10));
        card.add(createDataRow("📅 가입일", "2025-12-15"));
        card.add(Box.createVerticalStrut(25));

        JPanel btns = new JPanel(new GridLayout(1, 2, 15, 0));
        btns.setOpaque(false);
        btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton b1 = new JButton("정보 수정"); 
        styleBtn(b1);
        b1.addActionListener(e -> {
            /** [DB 포인트 3: 회원 정보 UPDATE] */
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            new EditProfileDialog(parentFrame).setVisible(true);
            refreshPage();
        });

        JButton b2 = new JButton("비밀번호 변경"); 
        styleBtn(b2);
        b2.addActionListener(e -> {
            /** [DB 포인트 4: 비밀번호 UPDATE] */
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            new PasswordChangeDialog(parentFrame).setVisible(true);
            refreshPage();
        });
        
        btns.add(b1);
        btns.add(b2);
        card.add(btns);
        
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private JPanel createReservationBox() {
        JPanel card = createCardFrame("📅 나의 예약 현황");
        
        /** [DB 포인트 5: 예약 데이터 리스트 조회] */
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        listPanel.add(createReservationItem("2026-02-25 14:00", "강남 주유소", "휘발유 50L"));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createReservationItem("2026-03-01 10:30", "서초 정비소", "엔진오일 교체"));
        
        listPanel.add(Box.createVerticalStrut(5));

        card.add(listPanel);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private JPanel createReservationItem(String dateTime, String location, String detail) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setBackground(COLOR_ROW_BG);
        row.setBorder(new CompoundBorder(new LineBorder(COLOR_DIVIDER), new EmptyBorder(12, 18, 12, 18)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setOpaque(false);
        JLabel dateLabel = new JLabel(dateTime);
        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        dateLabel.setForeground(COLOR_PRIMARY);
        JLabel descLabel = new JLabel(location + " | " + detail);
        infoPanel.add(dateLabel);
        infoPanel.add(descLabel);

        JButton cancelBtn = new JButton("예약 취소");
        cancelBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        cancelBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this, 
                "정말로 예약을 취소하시겠습니까?", 
                "예약 취소 확인", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                /** * [DB 포인트 6: 예약 데이터 삭제 또는 상태 변경] */
                listPanel.remove(row); 
                listPanel.revalidate();
                listPanel.repaint();
                
                Component cardFrame = listPanel.getParent();
                if (cardFrame instanceof JPanel) {
                    cardFrame.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardFrame.getPreferredSize().height));
                }
                JOptionPane.showMessageDialog(this, "예약이 정상적으로 취소되었습니다.");
            }
        });

        row.add(infoPanel, BorderLayout.CENTER);
        row.add(cancelBtn, BorderLayout.EAST);
        
        return row;
    }

    private JPanel createWithdrawalPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton withdrawBtn = new JButton("회원 탈퇴");
        withdrawBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        withdrawBtn.setForeground(Color.GRAY);
        withdrawBtn.setBorderPainted(false);
        withdrawBtn.setContentAreaFilled(false);
        withdrawBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        withdrawBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { withdrawBtn.setForeground(COLOR_DANGER); }
            @Override
            public void mouseExited(MouseEvent e) { withdrawBtn.setForeground(Color.GRAY); }
        });

        withdrawBtn.addActionListener(e -> {
            int firstCheck = JOptionPane.showConfirmDialog(
                this,
                "정말로 탈퇴하시겠습니까?",
                "회원 탈퇴",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (firstCheck == JOptionPane.YES_OPTION) {
                int secondCheck = JOptionPane.showConfirmDialog(
                    this,
                    "탈퇴 시 모든 데이터가 삭제되며 다시 복구할 수 없습니다.\n정말로 모든 정보를 삭제하고 탈퇴하시겠습니까?",
                    "회원 탈퇴 - 최종 확인",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE
                );

                if (secondCheck == JOptionPane.YES_OPTION) {
                    /** * [DB 포인트 8: 회원 탈퇴 처리] 
                     * - DELETE FROM members WHERE user_id = ?
                     */
                    JOptionPane.showMessageDialog(this, "탈퇴 처리가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.");
                    Window ancestor = SwingUtilities.getWindowAncestor(this);
                    if (ancestor != null) {
                        ancestor.dispose();
                    }
                    new Login().setVisible(true); 
                }
            }
        });

        p.add(withdrawBtn);
        return p;
    }

    private JPanel createCardFrame(String titleText) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(25, 25, 25, 25)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = new JLabel(titleText);
        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        t.setForeground(COLOR_TEXT_DARK);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(20));
        return p;
    }

    private JPanel createDataRow(String label, String value) {
        JPanel r = new JPanel(new BorderLayout());
        r.setBackground(COLOR_ROW_BG);
        r.setBorder(new CompoundBorder(new LineBorder(COLOR_DIVIDER), new EmptyBorder(12, 15, 12, 15)));
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        r.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setForeground(COLOR_TEXT_DARK);
        JLabel v = new JLabel(value); 
        v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        v.setForeground(COLOR_PRIMARY);
        
        r.add(l, BorderLayout.WEST);
        r.add(v, BorderLayout.EAST);
        return r;
    }

    private void styleBtn(JButton b) {
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        b.setForeground(COLOR_TEXT_DARK);
        b.setBorder(new LineBorder(COLOR_BORDER));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width, 50));

        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                b.setBackground(COLOR_BG_GRAY); 
                b.setForeground(COLOR_PRIMARY);
            }
            public void mouseExited(MouseEvent e) { 
                b.setBackground(Color.WHITE); 
                b.setForeground(COLOR_TEXT_DARK);
            }
        });
    }
}