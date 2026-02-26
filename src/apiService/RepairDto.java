package apiService;

public class RepairDto {
    private String name;
    private String address;
    private double lat;
    private double lng;
    private double distance;

    public RepairDto(String name, String address, double lat, double lng, double distance) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.distance = distance;
    }

    // Getter
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public double getDistance() { return distance; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %.2f km", name, address, distance);
    }
}