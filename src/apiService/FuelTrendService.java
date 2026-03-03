package apiService;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.net.URL;
import java.util.*;

public class FuelTrendService {
    //F260303263
    //F260206147
    public List<FuelTrendDto> getWeeklyTrend(String prodcd) {
        List<FuelTrendDto> list = new ArrayList<>();
        try {
            // out=xml로 요청 
            String urlStr = "https://www.opinet.co.kr/api/avgRecentPrice.do?out=xml"
                          + "&code=" + AvgPrice.apiKey + "&prodcd=" + prodcd;
            
            Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new URL(urlStr).openStream());
            
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("OIL"); 

            for (int i = 0; i < nList.getLength(); i++) {
                Element el = (Element) nList.item(i);
                // XML 태그명: <DATE>, <PRICE> 
                String date = el.getElementsByTagName("DATE").item(0).getTextContent();
                double price = Double.parseDouble(el.getElementsByTagName("PRICE").item(0).getTextContent());
                
                list.add(new FuelTrendDto(date, price));
            }
            // 날짜순(오름차순) 정렬
            list.sort(Comparator.comparing(FuelTrendDto::getDate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}