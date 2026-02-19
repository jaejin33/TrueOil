package maintenance.dto;

public class MaintenanceItemDto {
    private int itemId;
    private String itemName;
    private int cycleMileage; // 권장 교체 주기
    private String description;
    
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public int getCycleMileage() {
		return cycleMileage;
	}
	public void setCycleMileage(int cycleMileage) {
		this.cycleMileage = cycleMileage;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
    
}