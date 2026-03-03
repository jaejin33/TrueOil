package reservation.dto;

/**
 * 정비 예약 정보를 계층 간 전달하기 위한 데이터 전송 객체입니다.
 * repair_reservations 테이블의 구조와 매핑됩니다.
 * * @author Bae Jae-jin
 */
public class RepairReservationDto {
    private int resId;
    private int userId;
    private String shopName;
    private String resDate;
    private String resTime;
    private String services;
    private String note;
    private String status;
    private String createdAt;

    public RepairReservationDto() {}

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

	public String getResDate() {
		return resDate;
	}

	public void setResDate(String resDate) {
		this.resDate = resDate;
	}

	public String getResTime() {
		return resTime;
	}

	public void setResTime(String resTime) {
		this.resTime = resTime;
	}

	public String getServices() {
		return services;
	}

	public void setServices(String services) {
		this.services = services;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

}