package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;

/**
 * [Add Record Dialog]
 * 1. 기능 설명: 사용자가 주유 날짜, 장소, 가격, 주유량, 주행거리를 입력하여 개인 차량 관리 데이터를 생성합니다.
 * 2. API 포인트: [없음] (현재 수동 입력 방식)
 * 3. DB 포인트: 입력된 정보를 기반으로 fuel_records 테이블에 새로운 행을 INSERT 합니다.
 * 4. 추가해야 되는 기능: 주유소 명칭 입력 시 인근 주유소 리스트를 추천해주는 API 연동 기능.
 */
public class AddStationDialog extends JDialog {
    private JTextField dateF, stationF, priceF, litersF, mileageF; // priceF 추가
    private JButton saveBtn, cancelBtn;

    public AddStationDialog(Frame parent) {
        super(parent, "주유 기록 추가", true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(420, 620);

        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(new Color(243, 244, 246));
        background.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(209, 213, 219), 2),
                new EmptyBorder(16, 24, 24, 24)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 헤더 영역 ===== */
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

        JLabel titleLabel = new JLabel("주유 기록 추가");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 입력 폼 영역 ===== */
        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        formWrapper.setMaximumSize(new Dimension(320, 400));

        Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        Dimension fieldSize = new Dimension(Integer.MAX_VALUE, 35);

        // [기능 설명] 오늘 날짜 자동 세팅
        addInput(formWrapper, "날짜", dateF = new JTextField(LocalDate.now().toString()), labelFont, fieldSize);
        addInput(formWrapper, "주유소", stationF = new JTextField(), labelFont, fieldSize);
        
        // [추가해야 되는 기능] 현재 위치 기준 해당 주유소의 실시간 가격 API를 호출하여 자동으로 채워주는 기능
        addInput(formWrapper, "가격 (원)", priceF = new JTextField(), labelFont, fieldSize);
        
        addInput(formWrapper, "주유량 (L)", litersF = new JTextField(), labelFont, fieldSize);
        addInput(formWrapper, "누적 주행 거리 (km)", mileageF = new JTextField(), labelFont, fieldSize);

        /* ===== 저장 버튼 및 로직 ===== */
        saveBtn = new JButton("추가");
        saveBtn.setBackground(new Color(37, 99, 235));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(320, 45));

        saveBtn.addActionListener(e -> {
            try {
                // [기능 설명] 필드 값 추출 및 숫자 데이터 파싱 검증
                String date = dateF.getText();
                String station = stationF.getText();
                int price = Integer.parseInt(priceF.getText()); // 가격 추출
                double liters = Double.parseDouble(litersF.getText());
                int mileage = Integer.parseInt(mileageF.getText());

                // [DB 포인트] 사용자가 입력한 필드 데이터를 쿼리 파라미터로 매핑하여 DB에 저장
                // SQL: INSERT INTO fuel_records (user_id, log_date, station_name, fuel_price, liters, mileage) 
                //      VALUES (?, ?, ?, ?, ?, ?);

                JOptionPane.showMessageDialog(this, "주유 기록이 성공적으로 추가되었습니다.");
                dispose();
            } catch (NumberFormatException ex) {
                // [추가해야 되는 기능] 어떤 필드가 잘못되었는지 사용자에게 구체적으로 알려주는 포커스 기능
                JOptionPane.showMessageDialog(this, "가격, 리터, 주행거리는 숫자만 입력 가능합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn = new JButton("취소");
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setForeground(Color.GRAY);
        cancelBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> dispose());

        /* ===== 컴포넌트 조립 ===== */
        card.add(header);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(25));
        card.add(formWrapper);
        card.add(Box.createVerticalStrut(15));
        card.add(saveBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(cancelBtn);

        background.add(card, BorderLayout.CENTER);
        add(background);
        setLocationRelativeTo(parent);
    }

    private void addInput(JPanel p, String title, JTextField tf, Font font, Dimension size) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(font);
        lbl.setForeground(new Color(55, 65, 81));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(5));

        tf.setMaximumSize(size);
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setBorder(new CompoundBorder(
            new LineBorder(new Color(209, 213, 219), 1),
            new EmptyBorder(0, 10, 0, 10)
        ));
        p.add(tf);
        p.add(Box.createVerticalStrut(12));
    }
}