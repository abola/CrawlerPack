import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Google Maps API 爬蟲範例
 *
 * api 文件
 * @see "http://www.104.com.tw/i/api_doc/jobsearch/documentation.cfm"
 *
 * @author Abola Lee <abola921@gmail.com>
 */
public class Ch5Coz3 {

    static String token = "<your..token..here>";

    // Google place api
    // 找出指定座標方圓500公尺 分類為 food 的店家
    static String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/xml?location=25.06,121.53&radius=500&types=food" +
            "&key="+token;

    public static void main(String[] args){
        Document original = CrawlerPack.start().getFromXml(url);

        // 檢視 xml 內容
        //System.out.println(original.toString());

        // 要求要評價在4以上的店家
        for(Element elem : original.select("rating:matchesOwn(^4)") ){
            Element parentRoot = elem.parent();
            System.out.println( parentRoot.select("name").text() + "(" + elem.text() + ")");
        }
    }
}
