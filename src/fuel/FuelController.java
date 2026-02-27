package fuel;

import fuel.FuelService;
import fuel.dto.FuelLogDto;
import fuel.dto.FuelStatsDto;
import user.SessionManager;

import java.util.List;
import java.util.Map;

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
    
    public Map<String, Integer> getFuelStats() {
        int userId = SessionManager.getUserId();
        return fuelService.getMonthlyStats(userId);
    }
    
    public FuelStatsDto getProcessedStats(String[] monthQueries) {
        int userId = SessionManager.getUserId();
        Map<String, Integer> rawData = fuelService.getMonthlyStats(userId);
        
        FuelStatsDto stats = new FuelStatsDto();
        stats.calculate(rawData, monthQueries); // 계산은 여기서 끝냄
        return stats;
    }
    
    public String getLastFuelDate() {
        int userId = SessionManager.getUserId();
        return fuelService.getLastFuelDate(userId);
    }
}