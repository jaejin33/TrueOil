package fuel;

import java.time.LocalDateTime;

public class FuelLogDto {
    private int logId;
    private int userId;
    private String stationId;
    private int totalPrice;
    private int unitPrice;
    private int currentMileage;
    private LocalDateTime fuelDate; // 주유한 시점
	public int getLogId() {
		return logId;
	}
	public void setLogId(int logId) {
		this.logId = logId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public int getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}
	public int getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(int unitPrice) {
		this.unitPrice = unitPrice;
	}
	public int getCurrentMileage() {
		return currentMileage;
	}
	public void setCurrentMileage(int currentMileage) {
		this.currentMileage = currentMileage;
	}
	public LocalDateTime getFuelDate() {
		return fuelDate;
	}
	public void setFuelDate(LocalDateTime fuelDate) {
		this.fuelDate = fuelDate;
	}
    
    
}