package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;

public class AddStationDialog extends JDialog {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
    private static final Color COLOR_BORDER = new Color(209, 213, 219);
    private static final Color COLOR_DANGER = new Color(220, 38, 38);
    private static final Color COLOR_LABEL = new Color(55, 65, 81);

    private JTextField dateF, stationF, priceF, litersF, mileageF;
    private JButton saveBtn, cancelBtn;
    private boolean isUpdated = false;

    public AddStationDialog(Frame parent) {
        super(parent, "주유 기록 추가", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(420, 620);

        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(COLOR_BG_GRAY);
        background.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 2),
                new EmptyBorder(20, 20, 20, 20) 
        ));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 2),
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
            public void mouseEntered(MouseEvent e) { closeLabel.setForeground(COLOR_DANGER); }
            @Override
            public void mouseExited(MouseEvent e) { closeLabel.setForeground(Color.LIGHT_GRAY); }
        });
        header.add(closeLabel, BorderLayout.EAST);

        JLabel titleLabel = new JLabel("주유 기록 추가");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(COLOR_TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 입력 폼 영역 ===== */
        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        formWrapper.setMaximumSize(new Dimension(320, 400));

        Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        Dimension fieldSize = new Dimension(Integer.MAX_VALUE, 35);

        addInput(formWrapper, "날짜", dateF = new JTextField(LocalDate.now().toString()), labelFont, fieldSize);
        addInput(formWrapper, "주유소", stationF = new JTextField(), labelFont, fieldSize);
        addInput(formWrapper, "가격 (원)", priceF = new JTextField(), labelFont, fieldSize);
        addInput(formWrapper, "주유량 (L)", litersF = new JTextField(), labelFont, fieldSize);
        addInput(formWrapper, "누적 주행 거리 (km)", mileageF = new JTextField(), labelFont, fieldSize);

        /* ===== 저장 버튼 및 로직 ===== */
        saveBtn = new JButton("추가");
        saveBtn.setBackground(COLOR_PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(320, 45));

        saveBtn.addActionListener(e -> {
            try {
                // 1. 입력 데이터 추출
                String date = dateF.getText();          // fuel_date
                String station = stationF.getText();    // station_name
                int price = Integer.parseInt(priceF.getText());    // unit_price
                double liters = Double.parseDouble(litersF.getText()); // liters
                int mileage = Integer.parseInt(mileageF.getText()); // total_mileage
                
                /**
                 * [DB 연동 포인트 1: 데이터 삽입]
                 * SQL: INSERT INTO fuel_logs (user_id, fuel_date, station_name, unit_price, liters, total_mileage) 
                 * VALUES (?, ?, ?, ?, ?, ?)
                 * - 현재 로그인한 사용자(Session)의 ID를 외래키로 반드시 포함해야 함.
                 * - '총 주유 금액'은 (unit_price * liters)로 계산하여 DB 컬럼에 따로 저장하거나, 
                 * 조회 시(SELECT) 계산하도록 설계.
                 */

                /**
                 * [DB 연동 포인트 2: 차량 정보 업데이트]
                 * SQL: UPDATE car_info SET mileage = ? WHERE user_id = ?
                 * - 주유 시 입력한 '누적 주행 거리'가 기존 DB에 저장된 거리보다 크다면 
                 * 차량 테이블의 최신 주행 거리를 함께 업데이트하는 로직이 필요함.
                 */

                isUpdated = true;
                JOptionPane.showMessageDialog(this, "주유 기록이 성공적으로 추가되었습니다.");
                dispose();
            } catch (NumberFormatException ex) {
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

    public boolean isUpdated() {
        return isUpdated;
    }

    private void addInput(JPanel p, String title, JTextField tf, Font font, Dimension size) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(font);
        lbl.setForeground(COLOR_LABEL);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(5));

        tf.setMaximumSize(size);
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1),
            new EmptyBorder(0, 10, 0, 10)
        ));
        p.add(tf);
        p.add(Box.createVerticalStrut(12));
    }
}