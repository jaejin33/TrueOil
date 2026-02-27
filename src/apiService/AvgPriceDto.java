package apiService;

public class AvgPriceDto {
    private String prodcd; // 유종 코드 추가
    private String avgPrice;
    private String diffPrice;

    public AvgPriceDto(String prodcd, String avgPrice, String diffPrice) {
        this.prodcd = prodcd;
        this.avgPrice = avgPrice;
        this.diffPrice = diffPrice;
    }

    public String getProdcd() { return prodcd; }
    public String getAvgPrice() { return avgPrice; }
    public String getDiffPrice() { return diffPrice; }
}