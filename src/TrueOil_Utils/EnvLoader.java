package TrueOil_Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {
    private static final Map<String, String> env = new HashMap<>();

    static {
        try (BufferedReader br = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    env.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (Exception e) {
            System.err.println(".env 파일을 찾을 수 없습니다. 프로젝트 최상위 폴더에 위치시켜주세요.");
        }
    }

    public static String get(String key) {
        return env.getOrDefault(key, "");
    }
}