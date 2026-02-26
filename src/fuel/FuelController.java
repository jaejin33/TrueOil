package fuel;

import fuel.FuelService;
import fuel.dto.FuelLogDto;
import java.util.List;

public class FuelController {
    private final FuelService fuelService = new FuelService();

    // 최근 주유 기록 조회
    public List<FuelLogDto> getRecentHistory(int userId) {
        return fuelService.getRecentFuelLogs(userId);
    }

    // 주유 등록 요청 처리 (AddStationDialog에서 호출)
    public boolean registerFueling(FuelLogDto log) {
        return fuelService.registerFueling(log);
    }
}