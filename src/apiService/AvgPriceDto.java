package apiService;

// 평균유가 관리용
public class AvgPriceDto {
    private String prodcd;
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