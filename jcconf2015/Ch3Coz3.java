import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 使用組合包的SOP
 *
 * 1. 取得api
 * 2. 設定資料解析格式
 * 3. 用css selector取用資料
 *
 * jsoup support selector
 * @see "http://jsoup.org/apidocs/org/jsoup/select/Selector.html"
 */
public class Ch3Coz3 {

    public static void main(String[] args){

        // 1. 取得API
        // 北捷列車到站資料 API
        // @see http://taipeicity.github.io/traffic_realtime/
        // @see http://data.taipei/opendata/datalist/datasetMeta?oid=6556e1e8-c908-42d5-b984-b3f7337b139b
        String api = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=55ec6d6e-dc5c-4268-a725-d04cc262172b";

        // 2. 設定資料解析格式
        Document jsoupDoc = CrawlerPack.start().getFromJson(api);

        // 3. 用css selector取用資料
        // 目前往南勢角站的列車停靠站點
        System.out.println("目前往南勢角站的列車停靠站點：");
        for( Element elem : jsoupDoc.select("destination:containsOwn(南勢角站)") ){
            System.out.println(elem.parent().select("Station").text() );
        }

    }
}
