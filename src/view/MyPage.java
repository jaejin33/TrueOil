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

    private JPanel contentPanel;
    private JPanel listPanel;

    public MyPage() {
        // 스크롤판 설정
        setBorder(null);
        getVerticalScrollBar().setUnitIncrement(20);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_BG_GRAY); 
        contentPanel.setBorder(new EmptyBorder(40, 60, 40, 60));

        // 초기 데이터 로드 및 UI 생성
        refreshPage();

        /**
         * [탭 전환 시 자동 갱신]
         * 다른 탭에 있다가 마이페이지로 돌아올 때 최신 DB 정보를 다시 불러옵니다.
         */
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                refreshPage();
            }
        });

        setViewportView(contentPanel);
    }

    // 화면의 모든 내용을 새로고침하는 메서드
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
        
        // [섹션 3] 활동 통계
        contentPanel.add(createActivityBox());
        contentPanel.add(Box.createVerticalStrut(40));

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
        btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton b1 = new JButton("정보 수정"); 
        styleBtn(b1);
        b1.addActionListener(e -> {
            /** [DB 포인트 3: 회원 정보 UPDATE] */
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            new EditProfileDialog(parentFrame).setVisible(true);
        });

        JButton b2 = new JButton("비밀번호 변경"); 
        styleBtn(b2);
        b2.addActionListener(e -> {
            /** [DB 포인트 4: 비밀번호 UPDATE] */
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            new PasswordChangeDialog(parentFrame).setVisible(true);
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

        // 테스트 데이터 (실제 연동 시 반복문 사용)
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
            // 취소 확인 팝업
            int result = JOptionPane.showConfirmDialog(
                this, 
                "정말로 예약을 취소하시겠습니까?", 
                "예약 취소 확인", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                /** * [DB 포인트 6: 예약 데이터 삭제 또는 상태 변경] 
                 * - 쿼리: DELETE FROM reservations WHERE reservation_id = ?
                 * - 또는: UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?
                 */
                
                // [UI 즉시 삭제 로직]
                // 1. 해당 예약 항목(row)을 리스트 패널에서 즉시 제거
                listPanel.remove(row); 
                
                // 2. UI 갱신: 컴포넌트가 삭제되었음을 레이아웃 매니저에 알림
                listPanel.revalidate();
                listPanel.repaint();
                
                /**
                 * [DB 포인트 7: 통계 데이터 갱신 (선택 사항)]
                 * - 예약 취소 후 섹션 3의 '활동 통계' 숫자를 다시 계산해야 한다면
                 * - refreshPage()를 호출하거나 해당 라벨만 다시 SELECT 하여 텍스트 수정
                 */
                
                // 3. 부모 카드 높이 재계산 (항목이 사라진 만큼 카드가 줄어들도록 설정)
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

    private JPanel createActivityBox() {
        JPanel card = createCardFrame("내 활동 통계");
        /** [DB 포인트 7: 통계 데이터 집계] */
        JPanel grid = new JPanel(new GridLayout(1, 3, 15, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        grid.add(createStatItem("주유 기록", "32회"));
        grid.add(createStatItem("누적 주유비", "950만원"));
        grid.add(createStatItem("즐겨찾기", "5곳"));
        
        card.add(grid);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    /* --- UI 유틸리티 메서드 --- */

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