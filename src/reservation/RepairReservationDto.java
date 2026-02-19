package reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public class RepairReservationDto {
    private int resId;
    private int userId;
    private String shopName;
    private LocalDate resDate; // DATE 타입은 LocalDate가 적합
    private LocalTime resTime; // TIME 타입은 LocalTime이 적합
    private String status;
	public int getResId() {
		return resId;
	}
	public void setResId(int resId) {
		this.resId = resId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getShopName() {
		return shopName;
	}
	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
	public LocalDate getResDate() {
		return resDate;
	}
	public void setResDate(LocalDate resDate) {
		this.resDate = resDate;
	}
	public LocalTime getResTime() {
		return resTime;
	}
	public void setResTime(LocalTime resTime) {
		this.resTime = resTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
    
    
}