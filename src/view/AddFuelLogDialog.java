package view;

import javax.swing.*;
import javax.swing.border.*;

import fuel.FuelController;
import fuel.dto.FuelLogDto;
import user.SessionManager;

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

    public AddFuelLogDialog(Frame parent) {
        super(parent, "주유 기록 추가", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(420, 620);

        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(COLOR_BG_GRAY);
        background.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 2), new EmptyBorder(20, 20, 20, 20)));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 2), new EmptyBorder(16, 24, 24, 24)));

        /* ===== 헤더 영역 ===== */
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
        });
        header.add(closeLabel, BorderLayout.EAST);

        JLabel titleLabel = new JLabel("주유 기록 추가");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬

        /* ===== 입력 폼 영역 (수정됨) ===== */
        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT); // 부모 컨테이너 중앙 정렬
        formWrapper.setMaximumSize(new Dimension(320, 400));

        Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        // 가로 길이를 고정(320)하여 정렬 축을 고정합니다.
        Dimension fieldSize = new Dimension(320, 35);

        addInput(formWrapper, "날짜 (YYYY-MM-DD)", dateF = new JTextField(LocalDate.now().toString()), labelFont, fieldSize);
        addInput(formWrapper, "주유소", stationF = new JTextField(), labelFont, fieldSize);
        addInput(formWrapper, "가격 (원)", priceF = new JTextField(), labelFont, fieldSize);
        addInput(formWrapper, "주유량 (L)", litersF = new JTextField(), labelFont, fieldSize);
        addInput(formWrapper, "누적 주행 거리 (km)", mileageF = new JTextField(), labelFont, fieldSize);

        dateF.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) { validateMileageField(); }
        });

        /* ===== 저장 버튼 로직 ===== */
        saveBtn = new JButton("추가");
        saveBtn.setBackground(COLOR_PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT); // 버튼 중앙 정렬
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
                JOptionPane.showMessageDialog(this, "숫자 입력 형식이 올바르지 않습니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn = new JButton("취소");
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT); // 버튼 중앙 정렬
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setForeground(Color.GRAY);
        cancelBtn.addActionListener(e -> dispose());

        /* ===== 조립 ===== */
        card.add(header); card.add(Box.createVerticalStrut(10));
        card.add(titleLabel); card.add(Box.createVerticalStrut(25));
        card.add(formWrapper); card.add(Box.createVerticalStrut(15));
        card.add(saveBtn); card.add(Box.createVerticalStrut(12));
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

    /** 중앙 정렬이 적용된 addInput 메소드 */
    private void addInput(JPanel p, String title, JTextField tf, Font font, Dimension size) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(font);
        lbl.setForeground(COLOR_LABEL);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT); // 라벨 중앙 정렬
        p.add(lbl);
        
        p.add(Box.createVerticalStrut(5));
        
        tf.setMaximumSize(size);
        tf.setAlignmentX(Component.CENTER_ALIGNMENT); // 텍스트 필드 중앙 정렬
        tf.setHorizontalAlignment(JTextField.CENTER); // 텍스트 입력값도 중앙 정렬 (선택 사항)
        tf.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(0, 10, 0, 10)));
        p.add(tf);
        
        p.add(Box.createVerticalStrut(12));
    }
}