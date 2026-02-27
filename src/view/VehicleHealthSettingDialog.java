package view;

import javax.swing.*;
import javax.swing.border.*;

import maintenance.MaintenanceController;
import maintenance.dto.MaintenanceStatusDto;
import user.SessionManager;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleHealthSettingDialog extends JDialog {
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_BG_GRAY = new Color(243, 244, 246);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_DANGER = new Color(239, 68, 68);

    private JButton saveBtn, cancelBtn;
    private Map<String, JTextField> cycleFieldMap = new HashMap<>();
    private MaintenanceController maintenanceController = new MaintenanceController();
    
    private Point initialClick; // 드래그를 위한 포인트 저장

    public VehicleHealthSettingDialog(Frame parent) {
        super(parent, "권장 주기 설정", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(700, 500);

        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(COLOR_BG_GRAY);
        background.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 2), new EmptyBorder(20, 20, 20, 20)));

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
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(209, 213, 219), 2), new EmptyBorder(16, 24, 24, 24)));

        /* ===== 상단 헤더 ===== */
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_CARD_BG);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
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

        JLabel titleLabel = new JLabel("소모품 권장 교체 주기 설정");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel formWrapper = new JPanel(new GridLayout(2, 2, 30, 30));
        formWrapper.setBackground(COLOR_CARD_BG);
        formWrapper.setOpaque(false);

        //[DB POINT 1] 초기 권장 주기 데이터 로드 (SELECT)
        loadCurrentSettings(formWrapper);

        /* ===== 버튼 영역 ===== */
        saveBtn = new JButton("설정 저장");
        saveBtn.setBackground(COLOR_PRIMARY);
        saveBtn.setForeground(COLOR_CARD_BG);
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(250, 50));
        
        saveBtn.addActionListener(e -> {
            try {
                /* * [DB POINT 2] 데이터 업데이트 실행 */
                maintenanceController.updateAllCustomCycles(cycleFieldMap);
                
                JOptionPane.showMessageDialog(this, "권장 교체 주기가 저장되었으며 건강도가 재계산되었습니다.");
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "주기는 숫자만 입력 가능합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn = new JButton("취소");
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setForeground(Color.GRAY);
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> dispose());

        card.add(header);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(40));
        card.add(formWrapper);
        card.add(Box.createVerticalStrut(40));
        card.add(saveBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(cancelBtn);

        background.add(card, BorderLayout.CENTER);
        add(background);
        setLocationRelativeTo(parent);
    }

    private void addCycleSection(JPanel p, String title, int cycleVal) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBackground(COLOR_CARD_BG);
        sectionPanel.setBorder(new CompoundBorder(new LineBorder(new Color(245, 245, 245), 1), new EmptyBorder(15, 15, 15, 15)));

        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        sectionPanel.add(sectionTitle);

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        inputRow.setOpaque(false);

        JTextField cycleField = new JTextField(String.valueOf(cycleVal), 8);
        cycleField.setHorizontalAlignment(JTextField.CENTER);
        cycleField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        cycleField.setBorder(new MatteBorder(0, 0, 2, 0, COLOR_PRIMARY));
        cycleFieldMap.put(title, cycleField);

        JLabel unitLbl = new JLabel("km");
        unitLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        inputRow.add(cycleField);
        inputRow.add(unitLbl);
        
        sectionPanel.add(inputRow);
        p.add(sectionPanel);
    }
    
    private void loadCurrentSettings(JPanel formWrapper) {
        int userId = SessionManager.getUserId();
        // 기존 MaintenanceService의 getHealthDashboard 재사용
        List<MaintenanceStatusDto> list = new maintenance.MaintenanceService().getHealthDashboard(userId);
        
        for (MaintenanceStatusDto dto : list) {
            // 커스텀 주기가 설정되어 있으면(-1이 아니면) 그 값을, 없으면 기본 주기(cycleMileage)를 표시
            int displayCycle = (dto.getCustomCycleMileage() == -1) 
                               ? dto.getCycleMileage() 
                               : dto.getCustomCycleMileage();
                               
            addCycleSection(formWrapper, dto.getItemName(), displayCycle);
        }
    }
}