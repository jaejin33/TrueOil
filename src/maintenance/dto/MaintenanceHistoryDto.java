package maintenance.dto;

public class MaintenanceHistoryDto {
	private int historyId;
    private int userId;
    private int itemId;
    private String itemName;    // 교체한 항목 명칭
    private String replaceDate; // "2026-02-19" 형태의 문자열
    private int replaceMileage; // 교체 당시 주행거리
    private int cost = -1;      // -1이면 입력 안 함
    private String shopName;    // 정비소 명칭
	public int getHistoryId() {
		return historyId;
	}
	public void setHistoryId(int historyId) {
		this.historyId = historyId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
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
	public String getReplaceDate() {
		return replaceDate;
	}
	public void setReplaceDate(String replaceDate) {
		this.replaceDate = replaceDate;
	}
	public int getReplaceMileage() {
		return replaceMileage;
	}
	public void setReplaceMileage(int replaceMileage) {
		this.replaceMileage = replaceMileage;
	}
	public int getCost() {
		return cost;
	}
	public void setCost(int cost) {
		this.cost = cost;
	}
	public String getShopName() {
		return shopName;
	}
	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
    
    
}
