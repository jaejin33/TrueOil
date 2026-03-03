package fuel.dto;

public class MonthlySummaryDto {
    private int totalCount;
    private int totalAmount;
    private int avgPrice;
    private double diffPercent;

    // Getter, Setter 생성
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public int getAvgPrice() { return avgPrice; }
    public void setAvgPrice(int avgPrice) { this.avgPrice = avgPrice; }
    public double getDiffPercent() { return diffPercent; }
    public void setDiffPercent(double diffPercent) { this.diffPercent = diffPercent; }
}
