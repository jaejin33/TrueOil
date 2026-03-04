package reservation;

import java.util.List;

import reservation.dto.RepairReservationDto;
import util.SessionManager;

/**
 * 정비 예약 관련 화면(View)과 비즈니스 로직(Service)을 연결하는 컨트롤러 클래스입니다.
 * 사용자의 요청을 수신하여 적절한 서비스 메소드를 호출하고 결과를 반환합니다.
 * * @author Bae Jae-jin
 */
public class RepairReservationController {
    private final RepairReservationService reservationService;

    /**
     * RepairReservationController 객체를 초기화하고 Service 인스턴스를 생성합니다.
     */
    public RepairReservationController() {
        this.reservationService = new RepairReservationService();
    }

    /**
     * 정비소 예약 신청을 처리합니다.
     * * @param shopName 선택한 정비소 이름
     * @param date 예약 날짜 (YYYY-MM-DD)
     * @param time 예약 시간 (HH:mm)
     * @param services 선택된 정비 서비스 목록 (문자열 결합 형태)
     * @param note 사용자 요청사항
     * @return 예약 성공 여부
     */
    public boolean requestReservation(String shopName, String date, String time, String services, String note) {
        // 1. DTO 객체 생성 및 데이터 바인딩
        RepairReservationDto dto = new RepairReservationDto();
        dto.setUserId(SessionManager.getUserId()); // 세션에서 현재 사용자 ID 획득
        dto.setShopName(shopName);
        dto.setResDate(date);
        dto.setResTime(time);
        dto.setServices(services);
        dto.setNote(note);

        // 2. 서비스를 호출하여 결과 반환
        return reservationService.makeReservation(dto);
    }

    /**
     * 현재 로그인한 사용자의 모든 예약 내역을 조회합니다.
     * 마이페이지의 예약 현황 섹션 갱신 시 사용됩니다.
     * * @return 사용자의 예약 정보 리스트
     */
    public List<RepairReservationDto> fetchMyReservations() {
        return reservationService.getMyReservations();
    }

    /**
     * 사용자가 요청한 예약을 취소합니다.
     * * @param resId 취소할 예약의 고유 번호
     * @return 취소 성공 여부
     */
    public boolean requestCancel(int resId) {
        return reservationService.cancelReservation(resId);
    }
}