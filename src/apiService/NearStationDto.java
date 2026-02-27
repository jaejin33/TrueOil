package apiService;

public class NearStationDto {
    private String name;
    private String addr;
    private String price;
    private String dist;

    public NearStationDto(String name, String addr, String price, String dist) {
        this.name = name;
        this.addr = addr;
        this.price = price;
        this.dist = dist;
    }

    public String getName() { return name; }
    public String getAddr() { return addr; }
    public String getPrice() { return price; }
    public String getDist() { return dist; }
}
