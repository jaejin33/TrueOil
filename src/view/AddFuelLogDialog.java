package view;

import javax.swing.*;
import javax.swing.border.*;

import fuel.FuelController;
import fuel.dto.FuelLogDto;
import util.SessionManager;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AddFuelLogDialog extends JDialog {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
    private static final Color COLOR_BORDER = new Color(209, 213, 219);
    private static final Color COLOR_DANGER = new Color(220, 38, 38);
    private static final Color COLOR_LABEL = new Color(55, 65, 81);

    private JTextField dateF, stationF, priceF, litersF, mileageF;
    private JButton saveBtn, cancelBtn;
    private boolean isUpdated = false;
    private FuelController fuelController = new FuelController();
    
    private Point initialClick; // 드래그를 위한 포인트 저장

    public AddFuelLogDialog(Frame parent) {
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

        // 드래그 기능 구현
        background.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }
        });
        background.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                setLocation(X, Y);
            }
        });

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 2),
                new EmptyBorder(16, 24, 24, 24)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ===== 헤더 영역  ===== */
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

        addInput(formWrapper, "날짜 (YYYY-MM-DD)", dateF = new JTextField(LocalDate.now().toString()), labelFont, fieldSize);
        addInput(formWrapper, "주유소", stationF = new JTextField(), labelFont, fieldSize);
        addInput(formWrapper, "가격 (원)", priceF = new JTextField(), labelFont, fieldSize);
        addInput(formWrapper, "주유량 (L)", litersF = new JTextField(), labelFont, fieldSize);
        addInput(formWrapper, "누적 주행 거리 (km)", mileageF = new JTextField(), labelFont, fieldSize);

        // [로직] 날짜 입력 시 마일리지 필드 유효성 검사
        dateF.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) { validateMileageField(); }
        });

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
                String date = dateF.getText().trim();
                String station = stationF.getText().trim();
                int price = Integer.parseInt(priceF.getText().trim());
                double liters = Double.parseDouble(litersF.getText().trim());
                
                int mileage = -1;
                if (mileageF.isEnabled()) {
                    if (mileageF.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "누적 주행 거리를 입력해주세요.");
                        return;
                    }
                    mileage = Integer.parseInt(mileageF.getText().trim());
                }

                if (station.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "주유소 이름을 입력해주세요.");
                    return;
                }

                // [DB 연동 로직] FuelController 호출
                FuelLogDto logDto = new FuelLogDto();
                logDto.setUserId(SessionManager.getUserId());
                logDto.setFuelDate(date);
                logDto.setStationName(station);
                logDto.setFuelPrice(price);
                logDto.setFuelAmount(liters);
                logDto.setCurrentMileage(mileage);

                if (fuelController.registerFueling(logDto)) {
                    isUpdated = true;
                    JOptionPane.showMessageDialog(this, "주유 기록이 성공적으로 추가되었습니다.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "저장에 실패했습니다. 주행거리를 확인해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
                }

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

        /* ===== 컴포넌트 조립  ===== */
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

        validateMileageField();
    }

    private void validateMileageField() {
        String inputDateStr = dateF.getText().trim();
        String lastDateStr = fuelController.getLastFuelDate();

        try {
            LocalDate inputDate = LocalDate.parse(inputDateStr);
            LocalDate lastDate = LocalDate.parse(lastDateStr);

            if (inputDate.isBefore(lastDate)) {
                mileageF.setEnabled(false);
                mileageF.setText(""); 
                mileageF.setBackground(COLOR_BG_GRAY);
            } else {
                mileageF.setEnabled(true);
                mileageF.setBackground(Color.WHITE);
            }
        } catch (DateTimeParseException e) {
            mileageF.setEnabled(true);
            mileageF.setBackground(Color.WHITE);
        }
    }

    public boolean isUpdated() { return isUpdated; }
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