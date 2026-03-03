package apiService;

public class NearStationDto {
    private String name;
    private String addr;
    private String price;
    private String dist;

    public NearStationDto(String name, String addr, String price) {
        this.name = name;
        this.addr = addr;
        this.price = price;
    }

    public String getName() { return name; }
    public String getAddr() { return addr; }
    public String getPrice() { return price; }
}
