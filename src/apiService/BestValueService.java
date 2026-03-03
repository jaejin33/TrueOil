package apiService;

import java.util.List;

// 최적 알고리즘
public class BestValueService {

    private static final double DISTANCE_PENALTY_PER_KM = 50.0;

    public static EfficiencyResult getBestStations(List<ValueStationDto> stations) {
        ValueStationDto cheapest = null;
        ValueStationDto bestValue = null;

        double minPrice = Double.MAX_VALUE;
        double minScore = Double.MAX_VALUE;

        for (ValueStationDto s : stations) {
            double price = Double.parseDouble(s.getPrice());
            double distKm = s.getDistance() / 1000.0;

            if (price < minPrice) {
                minPrice = price;
                cheapest = s;
            }

            double score = price + (distKm * DISTANCE_PENALTY_PER_KM);

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