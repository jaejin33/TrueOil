package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MyPage extends JPanel {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
    private static final Color COLOR_BORDER = new Color(209, 213, 219);
    private static final Color COLOR_DIVIDER = new Color(229, 231, 235);
    private static final Color COLOR_ROW_BG = new Color(252, 252, 253);

    public MyPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(COLOR_BG_GRAY); 
        setBorder(new EmptyBorder(30, 60, 30, 60));

        JLabel title = new JLabel("마이페이지");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setForeground(COLOR_TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(25));

        /**
         * [DB 포인트 1: 사용자 정보 로드]
         * - 기능: 세션 유저의 이름, 이메일, 차량번호, 가입일 등 상세 정보 조회
         * - 연결: 아래 createProfileBox 내의 각 데이터 필드에 연결 필요
         */
        add(createProfileBox());
        add(Box.createVerticalStrut(25));
        
        /**
         * [DB 포인트 2: 활동 통계 데이터 집계]
         * - 기능: 주유 기록 건수, 누적 주유 금액 합계, 즐겨찾기 등록 수 조회
         * - 연결: 아래 createActivityBox 내의 통계 항목에 데이터 바인딩
         */
        add(createActivityBox());
    }

    // [섹션 1] 내 정보 박스
    private JPanel createProfileBox() {
        JPanel card = createCardFrame("👤 내 정보");
        
        JPanel profileHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        profileHeader.setBackground(Color.WHITE);
        profileHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // 아바타 영역
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
            /**
             * [DB 포인트 3: 프로필 이미지 수정]
             * - 기능: 새 이미지 경로를 DB(members 테이블)에 UPDATE
             * - 연결: 수정 성공 시 화면 아바타 이미지 새로고침 연동
             */
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            PhotoChangeDialog dialog = new PhotoChangeDialog(parentFrame);
            dialog.setVisible(true);
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

        // 데이터 행
        card.add(createDataRow("✉️ 이메일", "hong@example.com"));
        card.add(Box.createVerticalStrut(10));
        card.add(createDataRow("🚗 차량번호", "12가 3456"));
        card.add(Box.createVerticalStrut(10));
        card.add(createDataRow("📅 가입일", "2025-12-15"));
        card.add(Box.createVerticalStrut(25));

        // 하단 버튼 영역
        JPanel btns = new JPanel(new GridLayout(1, 2, 15, 0));
        btns.setOpaque(false);
        btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton b1 = new JButton("정보 수정"); 
        styleBtn(b1);
        b1.addActionListener(e -> {
            /**
             * [DB 포인트 4: 회원 정보 수정 반영]
             * - 기능: 다이얼로그에서 입력된 정보를 DB에 UPDATE
             * - 연결: 수정 완료 후 현재 페이지의 정보(이름, 차량번호 등) 재조회 및 UI 반영
             */
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
        p.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(25, 25, 25, 25)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));

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

    private JPanel createStatItem(String label, String val) {
        JPanel b = new JPanel(new GridLayout(2, 1, 0, 5));
        b.setBackground(new Color(250, 250, 251));
        b.setBorder(new LineBorder(COLOR_DIVIDER));
        
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setForeground(Color.GRAY);
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        
        JLabel v = new JLabel(val, SwingConstants.CENTER);
        v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        v.setForeground(COLOR_TEXT_DARK);
        
        b.add(l); b.add(v);
        return b;
    }

    private void styleBtn(JButton b) {
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        b.setForeground(COLOR_TEXT_DARK);
        b.setBorder(new LineBorder(COLOR_BORDER));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

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