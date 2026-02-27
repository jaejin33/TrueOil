
package fuel.dto;

import java.util.Map;

public class FuelStatsDto {
    private int[] monthlyExpenses = new int[6];
    private long total;
    private int max;
    private int min;
    private long avg;

    // 계산 로직을 Service나 DTO 내부로 이동
    public void calculate(Map<String, Integer> dbData, String[] monthQueries) {
        this.min = Integer.MAX_VALUE;
        this.total = 0;
        
        for (int i = 0; i < 6; i++) {
            int expense = dbData.getOrDefault(monthQueries[i], 0);
            this.monthlyExpenses[i] = expense;
            
            this.total += expense;
            if (expense > this.max) this.max = expense;
            if (expense > 0 && expense < this.min) this.min = expense;
        }
        if (this.min == Integer.MAX_VALUE) this.min = 0;
        this.avg = this.total / 6;
    }

    public int[] getMonthlyExpenses() { return monthlyExpenses; }
    public long getTotal() { return total; }
    public int getMax() { return max; }
    public int getMin() { return min; }
    public long getAvg() { return avg; }
}
