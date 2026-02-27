package apiService;

//가성비 추천 알고리즘
//알고리즘 작성은 아직 안함
//지도 데이터 통해서 좌표 받아온 이후 작업
import java.util.List;

public class BestValueService {

    public static EfficiencyResult getBestStations(List<ValueStationDto> stations) {
        ValueStationDto cheapest = null;
        ValueStationDto bestValue = null;

        double minPrice = Double.MAX_VALUE;
        double minScore = Double.MAX_VALUE;

        for (ValueStationDto s : stations) {
            double price = Double.parseDouble(s.getPrice());
            double distKm = s.getDistance() / 1000.0;

            double score = price + distKm * 100; // 거리 비용 환산

            if (price < minPrice) {
                minPrice = price;
                cheapest = s;
            }
            if (score < minScore) {
                minScore = score;
                bestValue = s;
            }
        }

        return new EfficiencyResult(cheapest, bestValue);
    }

    public static class EfficiencyResult {
        public ValueStationDto cheapest;
        public ValueStationDto bestValue;
        public EfficiencyResult(ValueStationDto cheapest, ValueStationDto bestValue) {
            this.cheapest = cheapest;
            this.bestValue = bestValue;
        }
    }
}
