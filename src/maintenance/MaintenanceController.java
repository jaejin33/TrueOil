package maintenance;

import maintenance.MaintenanceService;
import maintenance.dto.MaintenanceHistoryDto;
import util.SessionManager;

import javax.swing.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 소모품 관리와 관련된 사용자 요청을 제어하는 컨트롤러입니다.
 */
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    public MaintenanceController() {
        this.maintenanceService = new MaintenanceService();
    }

    /**
     * 사용자의 소모품 교체 이력을 등록하고 건강도를 재계산하도록 조율합니다.
     * * @param parentView 다이얼로그나 메시지 박스를 띄울 부모 컴포넌트
     * @param itemId 교체한 소모품의 고유 ID
     * @param itemName 소모품 이름 (메시지 표시용)
     * @param date 교체 날짜 (yyyy-MM-dd)
     * @param mileage 교체 시점의 주행거리
     * @param cost 교체 비용
     * @return 등록 및 업데이트 성공 여부
     */
    public boolean handleMaintenanceReplacement(Component parentView, int itemId, String itemName, String date, int mileage, int cost) {
        // 1. 세션에서 유저 ID 확인
        int userId = SessionManager.getUserId();
        if (userId == -1) {
            JOptionPane.showMessageDialog(parentView, "로그인 세션이 만료되었습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // 2. 비즈니스 유효성 검사 (예: 교체 거리가 0보다 작을 수 없음)
        if (mileage < 0) {
            JOptionPane.showMessageDialog(parentView, "교체 주행거리는 0보다 커야 합니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 3. 데이터 조립 (DTO 생성)
        MaintenanceHistoryDto history = new MaintenanceHistoryDto();
        history.setUserId(userId);
        history.setItemId(itemId);
        history.setReplaceDate(date);
        history.setReplaceMileage(mileage);
        history.setCost(cost);

        // 4. 서비스 레이어 호출 (트랜잭션 처리 포함)
        boolean isSuccess = maintenanceService.registerReplacement(history);

        // 5. 결과에 따른 사용자 알림(View 제어)
        if (isSuccess) {
            JOptionPane.showMessageDialog(parentView, 
                "[" + itemName + "] 교체 이력이 등록되었습니다.\n현재 주행거리 기준으로 건강도가 갱신되었습니다.", 
                "저장 완료", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(parentView, 
                "데이터 저장 중 서버 오류가 발생했습니다.", 
                "저장 실패", JOptionPane.ERROR_MESSAGE);
        }

        return isSuccess;
    }
    
    public void updateAllCustomCycles(Map<String, JTextField> fieldMap) {
        int userId = SessionManager.getUserId();
        MaintenanceDao dao = new MaintenanceDao();
        
        try {
            for (String itemName : fieldMap.keySet()) {
                int newCycle = Integer.parseInt(fieldMap.get(itemName).getText().trim());
                dao.updateCustomCycle(userId, itemName, newCycle);
            }
        } catch (NumberFormatException e) {
            throw e; // 다이얼로그에서 팝업을 띄울 수 있게 던짐
        }
    }
    
    public List<MaintenanceHistoryDto> getHistory(String itemName) {
    	int userId = SessionManager.getUserId();
        if (userId == -1) return new ArrayList<>(); // 세션 만료 시 빈 리스트 반환
        
        // 서비스 호출
        return maintenanceService.getHistoryList(userId, itemName);
    }
}