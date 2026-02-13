package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 정비소 예약 페이지
 * DB 포인트: 정비소 정보 불러오기(GET), 예약 정보 저장하기(POST)
 */
public class RepairPage extends JScrollPane {
    private String selectedShopId = null;
    private String selectedShopName = "";
    
    private JTextField shopDisplayField, dateField;
    private JComboBox<String> timeCombo;
    private JTextArea noteArea;
    private List<JCheckBox> serviceChecks;
    private JPanel shopListPanel;

    public RepairPage() {
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(20);
        setBorder(null);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(243, 244, 246));
        container.setBorder(new EmptyBorder(30, 60, 30, 60));

        JLabel title = new JLabel("정비소 예약");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(title);
        container.add(Box.createVerticalStrut(25));

        container.add(createShopSection());
        container.add(Box.createVerticalStrut(25));
        container.add(createFormSection());

        container.add(Box.createVerticalGlue());
        setViewportView(container);
        updateFormVisibility();
    }

    private JPanel createInputGroup(String labelText, JComponent component) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        label.setForeground(new Color(107, 114, 128));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 5, 0));

        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));

        group.add(label);
        group.add(component);
        return group;
    }

    private JPanel createFormSection() {
        JPanel card = createBaseCard("🔧 예약 정보 입력");
        JPanel body = (JPanel) card.getComponent(1);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // 1. 선택한 정비소
        shopDisplayField = new JTextField();
        shopDisplayField.setEditable(false);
        shopDisplayField.setPreferredSize(new Dimension(0, 35));
        body.add(createInputGroup("선택한 정비소", shopDisplayField));
        body.add(Box.createVerticalStrut(15));

        // 2. 예약 날짜 및 시간
        JPanel grid = new JPanel(new GridLayout(1, 2, 15, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        dateField = new JTextField("연도-월-일");
        timeCombo = new JComboBox<>(new String[]{"시간 선택", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00"});
        
        grid.add(createInputGroup("📅 예약 날짜", dateField));
        grid.add(createInputGroup("⏰ 예약 시간", timeCombo));
        body.add(grid);
        body.add(Box.createVerticalStrut(15));

        // 3. 정비 서비스
        JPanel serviceGrid = new JPanel(new GridLayout(2, 3, 0, 5));
        serviceGrid.setOpaque(false);
        serviceGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        serviceChecks = new ArrayList<>();
        String[] services = {"엔진 오일 교환", "타이어 교체", "브레이크 점검", "배터리 점검", "종합 점검", "기타"};
        for (String s : services) {
            JCheckBox cb = new JCheckBox(s);
            cb.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            cb.setOpaque(false);
            serviceChecks.add(cb);
            serviceGrid.add(cb);
        }
        body.add(createInputGroup("정비 서비스 (복수 선택 가능)", serviceGrid));
        body.add(Box.createVerticalStrut(15));

        // 4. 요청사항
        noteArea = new JTextArea(4, 20);
        noteArea.setBorder(new LineBorder(new Color(229, 231, 235)));
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(createInputGroup("요청사항", noteScroll));
        body.add(Box.createVerticalStrut(20));

        // 5. 버튼 및 API 연동
        JButton submitBtn = new JButton("예약하기");
        submitBtn.setBackground(new Color(37, 99, 235));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        submitBtn.addActionListener(e -> {
            // [API/DB 포인트]
            // 1. 유효성 검사 (정비소 선택 여부, 날짜 입력 여부 등)
            // 2. 체크된 서비스 리스트 수집
            // 3. DB에 INSERT 쿼리 날리거나 API 서버로 JSON 전송
            // 예: INSERT INTO reservations (shop_id, user_id, date, note) VALUES (...)
            JOptionPane.showMessageDialog(null, selectedShopName + "에 예약이 완료되었습니다.");
        });
        body.add(submitBtn);
        return card;
    }

    private JPanel createShopSection() {
        JPanel card = createBaseCard("📍 근처 정비소");
        
        // [DB 포인트] 실제 구현 시 SELECT * FROM shops 쿼리 결과를 기반으로 생성
        shopListPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        shopListPanel.setOpaque(false);
        String[][] shops = {
            {"1", "정비소 A", "역삼동 123", "1.2km", "4.5"}, 
            {"2", "정비소 B", "논현동 678", "2.1km", "4.8"}, 
            {"3", "정비소 C", "서초동 234", "3.5km", "4.3"}, 
            {"4", "정비소 D", "삼성동 789", "1.8km", "4.6"}
        };
        for (String[] s : shops) shopListPanel.add(createShopItem(s[0], s[1], s[2], s[3], s[4]));
        
        ((JPanel)card.getComponent(1)).add(shopListPanel);
        return card;
    }

    private JPanel createShopItem(String id, String name, String addr, String dist, String rate) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setBorder(new LineBorder(new Color(229, 231, 235), 1));
        item.setPreferredSize(new Dimension(0, 80));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel info = new JLabel("<html><div style='padding:10px;'><b>"+name+"</b><br><font color='gray'>"+addr+"</font></div></html>");
        item.add(info, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { 
                selectedShopId = id; 
                selectedShopName = name; 
                refreshShopSelection();
                updateFormVisibility(); 
            }
        });
        return item;
    }

    private void refreshShopSelection() {
        for (Component c : shopListPanel.getComponents()) {
            JPanel item = (JPanel) c;
            item.setBackground(Color.WHITE);
            item.setBorder(new LineBorder(new Color(229, 231, 235), 1));
            if (((JLabel)item.getComponent(0)).getText().contains(selectedShopName)) {
                item.setBackground(new Color(239, 246, 255));
                item.setBorder(new LineBorder(new Color(37, 99, 235), 2));
            }
        }
    }

    private void updateFormVisibility() {
        boolean enabled = (selectedShopId != null);
        shopDisplayField.setText(selectedShopName.isEmpty() ? " 정비소를 먼저 선택해주세요" : " " + selectedShopName);
        dateField.setEnabled(enabled);
        timeCombo.setEnabled(enabled);
        noteArea.setEnabled(enabled);
        for (JCheckBox cb : serviceChecks) cb.setEnabled(enabled);
    }

    private JPanel createBaseCard(String titleText) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(new Color(229, 231, 235), 2), new EmptyBorder(20, 25, 20, 25)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel t = new JLabel(titleText);
        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        t.setBorder(new EmptyBorder(0, 0, 20, 0));
        p.add(t, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        p.add(body, BorderLayout.CENTER);
        
        return p;
    }
}