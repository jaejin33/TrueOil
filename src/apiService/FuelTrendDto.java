package apiService;

public class FuelTrendDto {
    private String date;
    private double price;

    public FuelTrendDto(String date, double price) {
        this.date = date;
        this.price = price;
    }
    public String getDate() { return date; }
    public double getPrice() { return price; }
}