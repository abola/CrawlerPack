import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 使用 CSS selector 取出資料
 *
 * jsoup support selector
 * @see "http://jsoup.org/apidocs/org/jsoup/select/Selector.html"
 */
public class Ch3Coz2 {

    public static void main(String[] args){

        // 北捷列車到站資料 API
        // @see http://taipeicity.github.io/traffic_realtime/
        // @see http://data.taipei/opendata/datalist/datasetMeta?oid=6556e1e8-c908-42d5-b984-b3f7337b139b
        String api = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=55ec6d6e-dc5c-4268-a725-d04cc262172b";

        // call remote api
        String json = CrawlerPack.start().getFromRemote(api);

        // 轉換至xml
        String xml  = CrawlerPack.start().jsonToXml(json);
        // System.out.println( xml );

        // 轉化為 jsoup 物件
        Document jsoupDoc = CrawlerPack.start().xmlToJsoupDoc(xml);

        // 目前往南勢角站的列車停靠站點
        System.out.println("目前往南勢角站的列車停靠站點：");
        for( Element elem : jsoupDoc.select("destination:contains(南勢角站)") ){
            System.out.println(elem.parent().select("Station").text() );
        }

    }
}
