package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class VehicleHealthSettingDialog extends JDialog {
    private JButton saveBtn, cancelBtn;
    // DB 저장 시 값을 편하게 가져오기 위해 맵에 슬라이더를 담아둡니다.
    private Map<String, JSlider[]> sliderMap = new HashMap<>();

    public VehicleHealthSettingDialog(Frame parent) {
        super(parent, "알림 기준 설정", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(460, 820);

        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(new Color(243, 244, 246));
        background.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 2), new EmptyBorder(20, 20, 20, 20)));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(209, 213, 219), 2), new EmptyBorder(16, 24, 24, 24)));

        /* ===== 상단 헤더 ===== */
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

        JLabel titleLabel = new JLabel("알림 발생 구간 설정");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* * [DB POINT 1] 초기 임계값 데이터 로드 (SELECT)
         * - 구현 방법: SELECT 결과를 아래 redVal, yellowVal 변수에 할당하세요.
         */
        addThresholdSection(formWrapper, "엔진 오일", 20, 50);
        addThresholdSection(formWrapper, "타이어", 45, 70);
        addThresholdSection(formWrapper, "브레이크 패드", 30, 75);
        addThresholdSection(formWrapper, "배터리", 15, 45);

        /* ===== 버튼 영역 ===== */
        saveBtn = new JButton("설정 저장");
        saveBtn.setBackground(new Color(37, 99, 235));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(380, 50));
        
        saveBtn.addActionListener(e -> {
            /* * [DB POINT 2] 데이터 업데이트 실행 (UPDATE)
             * - sliderMap.get("엔진 오일")[0].getValue() 처럼 값을 추출하여 DB에 저장하세요.
             */
            JOptionPane.showMessageDialog(this, "설정이 저장되었습니다. 메인 화면의 색상 기준이 즉시 적용됩니다.");
            
            // 창을 닫으면 VehiclePage의 dialog.setVisible(true) 다음 줄이 실행되며 UI가 갱신됩니다.
            dispose();
        });

        cancelBtn = new JButton("취소");
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setForeground(Color.GRAY);
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> dispose());

        card.add(header);
        card.add(Box.createVerticalStrut(5));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(30));
        card.add(formWrapper);
        card.add(Box.createVerticalStrut(10));
        card.add(saveBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(cancelBtn);

        background.add(card, BorderLayout.CENTER);
        add(background);
        setLocationRelativeTo(parent);
    }

    private void addThresholdSection(JPanel p, String title, int redVal, int yellowVal) {
        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT); 
        sectionTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
        p.add(sectionTitle);

        JSlider rSlider = new JSlider(0, 100, redVal);
        JSlider ySlider = new JSlider(0, 100, yellowVal);
        
        // 나중에 값을 꺼내기 위해 맵에 보관
        sliderMap.put(title, new JSlider[]{rSlider, ySlider});

        JTextField rField = new JTextField(String.valueOf(redVal), 3);
        JTextField yField = new JTextField(String.valueOf(yellowVal), 3);

        JPanel redRow = createThresholdRow("위험", rSlider, rField, new Color(239, 68, 68));
        JPanel yellowRow = createThresholdRow("주의", ySlider, yField, new Color(234, 179, 8));
        
        redRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        yellowRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(redRow);
        p.add(yellowRow);

        rSlider.addChangeListener(e -> {
            rField.setText(String.valueOf(rSlider.getValue()));
            if (rSlider.getValue() >= ySlider.getValue()) {
                ySlider.setValue(rSlider.getValue() + 1);
            }
        });

        ySlider.addChangeListener(e -> {
            yField.setText(String.valueOf(ySlider.getValue()));
            if (ySlider.getValue() <= rSlider.getValue()) {
                rSlider.setValue(ySlider.getValue() - 1);
            }
        });

        p.add(Box.createVerticalStrut(15));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(380, 1));
        p.add(sep);
        p.add(Box.createVerticalStrut(10));
    }

    private JPanel createThresholdRow(String labelText, JSlider slider, JTextField tf, Color themeColor) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(380, 70));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setMaximumSize(new Dimension(380, 25));
        
        JLabel nameLbl = new JLabel(labelText);
        nameLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        nameLbl.setForeground(themeColor); 

        JPanel inputGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        inputGroup.setOpaque(false);
        tf.setHorizontalAlignment(JTextField.RIGHT);
        tf.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        tf.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        tf.setForeground(themeColor);
        
        JLabel unitLbl = new JLabel(" %");
        unitLbl.setForeground(themeColor);
        
        inputGroup.add(tf);
        inputGroup.add(unitLbl);

        top.add(nameLbl, BorderLayout.WEST);
        top.add(inputGroup, BorderLayout.EAST);

        slider.setBackground(Color.WHITE);
        slider.setMaximumSize(new Dimension(380, 35));
        
        tf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    int v = Integer.parseInt(tf.getText());
                    if (v >= 0 && v <= 100) slider.setValue(v);
                } catch (Exception ex) {}
            }
        });

        row.add(top);
        row.add(slider);
        return row;
    }
}