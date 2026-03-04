package apiService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.HashMap;
import java.util.Map;

public class AvgPrice {
	public static String apiKey = "F260206147";
    //F260303263
    //F260206147
	
	// 전국 평균 유가 가져오기
	public static Map<String, AvgPriceDto> getAvgPrice() throws Exception {

		String url = "http://www.opinet.co.kr/api/avgAllPrice.do?out=xml&code="+apiKey;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(url);
		doc.getDocumentElement().normalize();

		Map<String, AvgPriceDto> avgPriceMap = new HashMap<>();
		NodeList oilNodes = doc.getElementsByTagName("OIL");

		for (int i = 0; i < oilNodes.getLength(); i++) {
			Node node = oilNodes.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String prodcd = element.getElementsByTagName("PRODCD").item(0).getTextContent();
				String parsedAvgPrice = element.getElementsByTagName("PRICE").item(0).getTextContent();
				String parsedDiffPrice = element.getElementsByTagName("DIFF").item(0).getTextContent();

				avgPriceMap.put(prodcd, new AvgPriceDto(prodcd, parsedAvgPrice, parsedDiffPrice));
			}
		}

		return avgPriceMap;
	}
}