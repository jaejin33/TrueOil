module TrueOil {
	requires transitive java.desktop; // 이미지 8의 'Frame' 관련 에러 해결
	requires transitive javafx.controls;
	requires transitive javafx.swing;
	requires javafx.fxml;
	requires javafx.web; // WebView 에러 해결
	requires transitive java.sql;
	requires jdk.jsobject; // 이미지 4의 sql 접근 에러 해결
	// 2. 다른 모듈(JavaFX 등)이 내 코드를 읽을 수 있도록 허용
	// FXML 파일이 있다면 해당 패키지를 열어줘야 합니다.

	opens view to javafx.fxml;
	opens user to javafx.fxml;

	exports apiService;
	exports database;
	exports fuel;
	exports fuel.dto;
	exports maintenance;
	exports maintenance.dto;
	exports reservation.dto;
	exports user;
	exports user.dto;
	exports view;
}