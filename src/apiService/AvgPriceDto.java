package apiService;

public class AvgPriceDto {

    private String avgPrice;
    private String diffPrice;

    public AvgPriceDto(String avgPrice, String diffPrice) {
        this.avgPrice = avgPrice;
        this.diffPrice = diffPrice;
    }

    public String getAvgPrice() { return avgPrice; }
    public String getDiffPrice() { return diffPrice; }
}