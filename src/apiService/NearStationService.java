package apiService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

public class NearStationService {

    public static List<NearStationDto> getNearStations(String areaCode) throws Exception {
        // 오피넷 API URL
        String apiUrl = "https://www.opinet.co.kr/api/lowTop10.do?"
                + "code="+AvgPrice.apiKey
                + "&out=xml"
                + "&prodcd=B027"   // 휘발유
                + "&area=" + areaCode
                + "&cnt=3";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        InputStream is = conn.getInputStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);

        NodeList oilList = doc.getElementsByTagName("OIL");

        List<NearStationDto> stations = new ArrayList<>();
        for (int i = 0; i < oilList.getLength(); i++) {
            Element oil = (Element) oilList.item(i);

            String name = getTagValue(oil, "OS_NM");
            String addr = getTagValue(oil, "NEW_ADR");
            String price = getTagValue(oil, "PRICE");
            String dist = ""; // 좌표로 거리 계산 예정 distance

            stations.add(new NearStationDto(name, addr, price + "원", dist));
        }

        return stations;
    }

    private static String getTagValue(Element e, String tag) {
        NodeList nl = e.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            return nl.item(0).getTextContent();
        }
        return "";
    }
}
