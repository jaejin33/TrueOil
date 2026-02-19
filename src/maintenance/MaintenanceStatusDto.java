package maintenance;

public class MaintenanceStatusDto {
	
    private int mId;
    private int itemId;
    private int userId;
    private String itemName;
    private int customCycleMileage = -1; // -1이면 사용자 설정 없음(기본 주기 사용)
    private int lastReplaceMileage = 0;  // 마지막 교체 시점의 주행거리
    private int healthScore = 100;      // 0~100 사이의 정수
    private String description;         // 관리 팁
    
	public int getmId() {
		return mId;
	}
	public void setmId(int mId) {
		this.mId = mId;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public int getCustomCycleMileage() {
		return customCycleMileage;
	}
	public void setCustomCycleMileage(int customCycleMileage) {
		this.customCycleMileage = customCycleMileage;
	}
	public int getLastReplaceMileage() {
		return lastReplaceMileage;
	}
	public void setLastReplaceMileage(int lastReplaceMileage) {
		this.lastReplaceMileage = lastReplaceMileage;
	}
	public int getHealthScore() {
		return healthScore;
	}
	public void setHealthScore(int healthScore) {
		this.healthScore = healthScore;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
    
	
    
}