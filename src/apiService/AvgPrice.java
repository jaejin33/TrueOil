package apiService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AvgPrice {

	// 전국 평균 유가 가져오기
	public static AvgPriceDto getAvgPrice() throws Exception {

		String url = "http://www.opinet.co.kr/api/avgAllPrice.do?out=xml&code=F260206147";

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(url);
		doc.getDocumentElement().normalize();

		String parsedAvgPrice = "0";
		String parsedDiffPrice = "0";
		NodeList oilNodes = doc.getElementsByTagName("OIL");
		for (int i = 0; i < oilNodes.getLength(); i++) {
			Node node = oilNodes.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String prodcd = element.getElementsByTagName("PRODCD").item(0).getTextContent();

				if ("B027".equals(prodcd)) {
					parsedAvgPrice = element.getElementsByTagName("PRICE").item(0).getTextContent();
					parsedDiffPrice = element.getElementsByTagName("DIFF").item(0).getTextContent();

					break;
				}
			}
		}

		return new AvgPriceDto(parsedAvgPrice, parsedDiffPrice);
	}
}