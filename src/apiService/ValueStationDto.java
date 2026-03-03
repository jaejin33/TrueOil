package apiService;

public class ValueStationDto {
	private String uniId;
	private String name;
	private String price;
	private String fuelType;
	private double distance;
	private double x;
	private double y;

	public ValueStationDto(String uniId, String name, String price, String fuelType, double distance, double x,
			double y) {

		this.uniId = uniId;
		this.name = name;
		this.price = price;
		this.fuelType = fuelType;
		this.distance = distance;
		this.x = x;
		this.y = y;
	}

	public String getUniId() {

		return uniId;
	}

	public String getFuelType() {

		return fuelType;
	}

	public String getName() {

		return name;
	}

	public String getPrice() {

		return price;
	}

	public double getDistance() {

		return distance;
	}

	public double getX() {

		return x;
	}

	public double getY() {

		return y;
	}
}
