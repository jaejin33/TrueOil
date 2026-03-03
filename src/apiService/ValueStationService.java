package apiService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class ValueStationService {
	public static List<ValueStationDto> getStations(double x, double y, int radius) throws Exception {

		return getStations(x, y, radius, null, null, null);
	}

	public static List<ValueStationDto> getStations(double x, double y, int radius, String keyword) throws Exception {

		return getStations(x, y, radius, keyword, null, null);
	}

	public static List<ValueStationDto> getStations(double x, double y, int radius, String keyword, String prodCd,
			String sortCode) throws Exception {
		if (prodCd == null || prodCd.trim().isEmpty()) {
			prodCd = "B027";
		}
		if (sortCode == null || sortCode.trim().isEmpty()) {
			sortCode = "1";
		}
		String apiUrl = "https://www.opinet.co.kr/api/aroundAll.do?" + "code="+AvgPrice.apiKey + "&out=xml" + "&x=" + x
				+ "&y=" + y + "&radius=" + radius + "&prodcd=" + prodCd + "&sort=" + sortCode;

		URL url = new URL(apiUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		InputStream is = conn.getInputStream();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);

		NodeList oilList = doc.getElementsByTagName("OIL");
		List<ValueStationDto> stations = new ArrayList<>();

		for (int i = 0; i < oilList.getLength(); i++) {
			Element oil = (Element) oilList.item(i);
			String uniId = getTagValue(oil, "UNI_ID");
			String name = getTagValue(oil, "OS_NM");
			String price = getTagValue(oil, "PRICE");
			double distance = Double.parseDouble(getTagValue(oil, "DISTANCE"));
			double gx = Double.parseDouble(getTagValue(oil, "GIS_X_COOR"));
			double gy = Double.parseDouble(getTagValue(oil, "GIS_Y_COOR"));

			if (keyword == null || keyword.isEmpty() || name.contains(keyword)) {
				stations.add(new ValueStationDto(uniId, name, price, distance, gx, gy));
			}
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
