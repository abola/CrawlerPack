import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 行政院農業委員會資料開放平台 爬蟲範例
 *
 * @author Abola Lee <abola921@gmail.com>
 */
public class Ch5Coz4 {

    // API url
    static String url = "http://m.coa.gov.tw/OpenData/FarmTransData.aspx?$top=10&$skip=0";

    public static void main(String[] args){
        // API 內容轉化為 jsoup Document 物件
        Document original = CrawlerPack.getFromJson(url);

        // 檢視 xml 內容
        //System.out.println( original.toString() );

        for(Element elem : original.select("row") ){
            System.out.print( elem.select("交易日期").text() );
            System.out.print( elem.select("作物名稱").text() );
            System.out.print( elem.select("平均價").text() );
            System.out.print( "\n" );
        }
    }
}
