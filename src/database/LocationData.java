package database;

/**
 * 앱 전역에서 사용하는 지역별 기준 좌표 데이터
 */
public enum LocationData {
	DONG_EUI("동의대역", 494396, 284051, 35.154176, 129.033014), SEOMYEON("서면역", 496851, 284488, 35.15771, 129.05917),
	BUSAN_STN("부산역", 495302, 279766, 35.11522, 129.04224), HAEUNDAE("해운대역", 505928, 285117, 35.16311, 129.15890),
	SASANG("사상역", 490059, 284988, 35.162361, 128.984621), HADAN("하단역", 488426, 278784, 35.10618, 128.96680);

	private final String name;
	private final double x; // KATECH X (주유소 API용)
	private final double y; // KATECH Y (주유소 API용)
	private final double lat; // 위도 (지도/정비소용)
	private final double lng; // 경도 (지도/정비소용)
	public static LocationData selected = DONG_EUI;

	LocationData(String name, double x, double y, double lat, double lng) {

		this.name = name;
		this.x = x;
		this.y = y;
		this.lat = lat;
		this.lng = lng;
	}

	public String getName() {

		return name;
	}

	public double getX() {

		return x;
	}

	public double getY() {

		return y;
	}

	public double getLat() {

		return lat;
	}

	public double getLng() {

		return lng;
	}

	@Override
	public String toString() {

		return name; // JComboBox에 역 이름이 바로 표시됩니다.
	}
}