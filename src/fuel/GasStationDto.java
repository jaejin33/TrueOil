package fuel;

public class GasStationDto {
	
    private String stationId;
    private String name;
    private String brand;
    private String address;
    private double lat;
    private double lon;
    private boolean hasCarWash;
    private boolean hasRepair;
    
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public boolean isHasCarWash() {
		return hasCarWash;
	}
	public void setHasCarWash(boolean hasCarWash) {
		this.hasCarWash = hasCarWash;
	}
	public boolean isHasRepair() {
		return hasRepair;
	}
	public void setHasRepair(boolean hasRepair) {
		this.hasRepair = hasRepair;
	}
    
    
}