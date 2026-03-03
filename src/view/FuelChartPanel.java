package view;
import javax.swing.*;

import apiService.FuelTrendDto;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FuelChartPanel extends JPanel {
    private List<FuelTrendDto> data = new ArrayList<>();
    private double animRatio = 0.0; // 애니메이션 비율 (0.0 ~ 1.0)
    private Timer animTimer;

    // 데이터를 설정하고 애니메이션을 시작하는 메서드
    public void setDataWithAnim(List<FuelTrendDto> newData) {
        this.data = newData;
        this.animRatio = 0.0;
        
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        
        animTimer = new Timer(15, e -> {
            animRatio += 0.05;
            if (animRatio >= 1.0) {
                animRatio = 1.0;
                animTimer.stop();
            }
            repaint();
        });
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 여백 및 크기 설정
        int paddingLeft = 70; // 가격 표시를 위해 왼쪽 여백을 넓게 잡음
        int paddingOther = 40;
        int width = getWidth() - paddingLeft - paddingOther;
        int height = getHeight() - (paddingOther * 2);

        // 1. 데이터 분석 (최소/최대 가격)
        double minPrice = data.stream().mapToDouble(FuelTrendDto::getPrice).min().orElse(0);
        double maxPrice = data.stream().mapToDouble(FuelTrendDto::getPrice).max().orElse(2000);
        
        // 그래프가 너무 꽉 차지 않게 상하 여유를 줌
        minPrice -= 5;
        maxPrice += 5;
        double range = maxPrice - minPrice;

        // 2. Y축 눈금 및 가로 가이드라인 그리기 (여기에 넣으시는 겁니다!)
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        for (int i = 0; i <= 4; i++) {
            // 눈금 위치 계산
            int y = height + paddingOther - (i * height / 4);
            double priceLabel = minPrice + (range * i / 4);
            
            // 가로 가이드라인 (연한 회색)
            g2.setColor(new Color(243, 244, 246)); 
            g2.drawLine(paddingLeft, y, paddingLeft + width, y);
            
            // 가격 라벨 (좌측)
            g2.setColor(Color.GRAY);
            g2.drawString(String.format("%,.0f원", priceLabel), 10, y + 5);
        }

        // 3. 꺾은선 그래프 그리기 (애니메이션 적용)
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(new Color(37, 99, 235)); // COLOR_PRIMARY

        for (int i = 0; i < data.size() - 1; i++) {
            int x1 = paddingLeft + (i * width / (data.size() - 1));
            // animRatio를 곱해 아래에서 위로 올라오는 효과
            int y1 = (int) (height - (((data.get(i).getPrice() - minPrice) / range * height) * animRatio)) + paddingOther;
            
            int x2 = paddingLeft + ((i + 1) * width / (data.size() - 1));
            int y2 = (int) (height - (((data.get(i + 1).getPrice() - minPrice) / range * height) * animRatio)) + paddingOther;

            g2.drawLine(x1, y1, x2, y2);
            g2.fillOval(x1 - 4, y1 - 4, 8, 8); // 데이터 점
            if (i == data.size() - 2) g2.fillOval(x2 - 4, y2 - 4, 8, 8);
            
            // X축 날짜 표시 (MMDD)
            g2.setColor(Color.GRAY);
            String dateStr = data.get(i).getDate(); // 예: 20260303
            String label = dateStr.substring(4, 6) + "/" + dateStr.substring(6); 
            g2.drawString(label, x1 - 15, height + paddingOther + 20);
            
            if (i == data.size() - 2) {
                String lastDate = data.get(i+1).getDate();
                String lastLabel = lastDate.substring(4, 6) + "/" + lastDate.substring(6);
                g2.drawString(lastLabel, x2 - 15, height + paddingOther + 20);
            }
            g2.setColor(new Color(37, 99, 235));
        }
    }
}