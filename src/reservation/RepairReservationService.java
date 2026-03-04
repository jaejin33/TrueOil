package reservation;

import java.util.List;

import reservation.dto.RepairReservationDto;
import util.SessionManager;

/**
 * 정비 예약과 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 데이터 검증 및 DAO 호출을 담당하며, 트랜잭션 단위의 업무를 수행합니다.
 * * @author Bae Jae-jin
 * 
 */
public class RepairReservationService {
    private final RepairReservationDao reservationDao;

    /**
     * RepairReservationService 객체를 초기화하고 필요한 DAO 인스턴스를 생성합니다.
     */
    public RepairReservationService() {
        this.reservationDao = new RepairReservationDao();
    }

    /**
     * 사용자의 예약 요청을 검증하고 최종적으로 등록합니다.
     * * @param dto 화면에서 전달받은 예약 정보 객체
     * @return 예약 성공 여부
     */
    public boolean makeReservation(RepairReservationDto dto) {
        // 사용자 ID가 유효한지 최종 확인
        if (dto.getUserId() <= 0) {
            dto.setUserId(SessionManager.getUserId());
        }
        
        // 날짜/시간 포맷 검증 등 추가적인 비즈니스 룰을 여기에 작성
        if (dto.getShopName() == null || dto.getShopName().isEmpty()) {
            return false;
        }

        return reservationDao.insertReservation(dto);
    }

    /**
     * 현재 로그인한 사용자의 모든 예약 목록을 가져옵니다.
     * * @return 해당 사용자의 예약 리스트
     */
    public List<RepairReservationDto> getMyReservations() {
        int userId = SessionManager.getUserId();
        if (userId == -1) {
            return null;
        }
        return reservationDao.getReservationsByUserId(userId);
    }

    /**
     * 특정 예약을 취소합니다.
     * * @param resId 취소할 예약의 고유 번호
     * @return 취소 성공 여부
     */
    public boolean cancelReservation(int resId) {
        // 취소 전 상태 확인 로직(예: 이미 완료된 정비는 취소 불가) 등을 추가할 수 있습니다.
        return reservationDao.deleteReservation(resId);
    }
}