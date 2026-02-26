package fuel.dto;

import java.time.LocalDateTime;

/**
 * 주유 기록 정보를 담는 데이터 전송 객체 (DTO)
 */
public class FuelLogDto {
    private int fuelId;          // 주유 기록 고유 ID (PK)
    private int userId;          // 사용자 ID (FK)
    private String fuelDate;     // 주유 날짜 (YYYY-MM-DD)
    private String stationName;  // 주유소 이름
    private int fuelPrice;       // 총 주유 금액 (원)
    private double fuelAmount;   // 주유량 (L)
    private int currentMileage;  // 주유 시점의 누적 주행거리 (km)

    public FuelLogDto() {}

    public int getFuelId() { return fuelId; }
    public void setFuelId(int fuelId) { this.fuelId = fuelId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFuelDate() { return fuelDate; }
    public void setFuelDate(String fuelDate) { this.fuelDate = fuelDate; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public int getFuelPrice() { return fuelPrice; }
    public void setFuelPrice(int fuelPrice) { this.fuelPrice = fuelPrice; }

    public double getFuelAmount() { return fuelAmount; }
    public void setFuelAmount(double fuelAmount) { this.fuelAmount = fuelAmount; }

    public int getCurrentMileage() { return currentMileage; }
    public void setCurrentMileage(int currentMileage) { this.currentMileage = currentMileage; }
}