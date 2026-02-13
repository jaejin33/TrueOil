package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * [MyPage]
 * 핵심 역할:
 * 1. DB 조회: 현재 로그인된 사용자의 상세 정보 및 활동 데이터(통계) 출력
 * 2. 화면 연결: 정보 수정, 비밀번호 변경, 프로필 사진 변경 다이얼로그 호출
 */
public class MyPage extends JPanel {
    public MyPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(243, 244, 246)); 
        setBorder(new EmptyBorder(30, 60, 30, 60));

        JLabel title = new JLabel("마이페이지");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(25));

        add(createProfileBox());
        add(Box.createVerticalStrut(25));
        add(createActivityBox());
    }

    // [섹션 1] 내 정보 박스
    private JPanel createProfileBox() {
        JPanel card = createCardFrame("👤 내 정보");
        
        // 사진과 정보 세트를 감싸는 패널 (왼쪽 정렬)
        JPanel profileHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        profileHeader.setBackground(Color.WHITE);
        profileHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // 1. 프로필 이미지 아바타
        JLabel avatar = new JLabel("👤", SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(80, 80));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(243, 244, 246));
        avatar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
        avatar.setBorder(new LineBorder(new Color(229, 231, 235), 1));
        
        // 2. 이름 + ID + 사진 변경 버튼을 담는 수직 패널
        JPanel infoAndBtnTexts = new JPanel();
        infoAndBtnTexts.setLayout(new BoxLayout(infoAndBtnTexts, BoxLayout.Y_AXIS));
        infoAndBtnTexts.setOpaque(false);
        
        JLabel nameLbl = new JLabel("홍길동"); 
        nameLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        
        JLabel idLbl = new JLabel("회원 ID: USER12345");
        idLbl.setForeground(Color.GRAY);
        idLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        // [핵심] 사진 변경 버튼 - 이름/ID 바로 아래 배치
        JButton changePhotoBtn = new JButton("📷 사진 변경");
        changePhotoBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        changePhotoBtn.setBackground(Color.WHITE);
        changePhotoBtn.setFocusPainted(false);
        changePhotoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePhotoBtn.setBorder(new CompoundBorder(
            new LineBorder(new Color(229, 231, 235)), 
            new EmptyBorder(3, 8, 3, 8)
        ));
        
        changePhotoBtn.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            PhotoChangeDialog dialog = new PhotoChangeDialog(parentFrame);
            dialog.setVisible(true);
        });

        // 수직 정렬 조립
        infoAndBtnTexts.add(nameLbl);
        infoAndBtnTexts.add(Box.createVerticalStrut(4));
        infoAndBtnTexts.add(idLbl);
        infoAndBtnTexts.add(Box.createVerticalStrut(8));
        infoAndBtnTexts.add(changePhotoBtn); 

        profileHeader.add(avatar);
        profileHeader.add(infoAndBtnTexts);
        
        card.add(profileHeader);
        card.add(Box.createVerticalStrut(25));

        // 상세 정보 데이터 행 (이메일, 차량번호, 가입일)
        card.add(createDataRow("✉️ 이메일", "hong@example.com"));
        card.add(Box.createVerticalStrut(10));
        card.add(createDataRow("🚗 차량번호", "12가 3456"));
        card.add(Box.createVerticalStrut(10));
        card.add(createDataRow("📅 가입일", "2025-12-15"));
        card.add(Box.createVerticalStrut(25));

        /* ===== 하단 버튼 영역 (정보 수정 / 비밀번호 변경) ===== */
        JPanel btns = new JPanel(new GridLayout(1, 2, 15, 0));
        btns.setOpaque(false);
        btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton b1 = new JButton("정보 수정"); 
        styleBtn(b1);
        b1.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            EditProfileDialog dialog = new EditProfileDialog(parentFrame);
            dialog.setVisible(true);
        });

        JButton b2 = new JButton("비밀번호 변경"); 
        styleBtn(b2);
        b2.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            PasswordChangeDialog dialog = new PasswordChangeDialog(parentFrame);
            dialog.setVisible(true);
        });
        
        btns.add(b1);
        btns.add(b2);
        card.add(btns);

        return card;
    }

    // [섹션 2] 활동 통계 박스
    private JPanel createActivityBox() {
        JPanel card = createCardFrame("내 활동 통계");
        JPanel grid = new JPanel(new GridLayout(1, 3, 15, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        grid.add(createStatItem("주유 기록", "32회"));
        grid.add(createStatItem("누적 주유비", "950만원"));
        grid.add(createStatItem("즐겨찾기", "5곳"));
        
        card.add(grid);
        return card;
    }

    /* --- UI 유틸리티 메서드 --- */

    private JPanel createCardFrame(String titleText) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(new Color(209, 213, 219), 1), new EmptyBorder(25, 25, 25, 25)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));

        JLabel t = new JLabel(titleText);
        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(20));
        return p;
    }

    private JPanel createDataRow(String label, String value) {
        JPanel r = new JPanel(new BorderLayout());
        r.setBackground(new Color(252, 252, 253));
        r.setBorder(new CompoundBorder(new LineBorder(new Color(229, 231, 235)), new EmptyBorder(12, 15, 12, 15)));
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        r.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        JLabel v = new JLabel(value); 
        v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        r.add(l, BorderLayout.WEST);
        r.add(v, BorderLayout.EAST);
        return r;
    }

    private JPanel createStatItem(String label, String val) {
        JPanel b = new JPanel(new GridLayout(2, 1, 0, 5));
        b.setBackground(new Color(250, 250, 251));
        b.setBorder(new LineBorder(new Color(229, 231, 235)));
        
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setForeground(Color.GRAY);
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        
        JLabel v = new JLabel(val, SwingConstants.CENTER);
        v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        
        b.add(l); b.add(v);
        return b;
    }

    private void styleBtn(JButton b) {
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        b.setBorder(new LineBorder(new Color(209, 213, 219)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width, 60));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); 

        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(249, 250, 251)); }
            public void mouseExited(MouseEvent e) { b.setBackground(Color.WHITE); }
        });
    }
}