import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.nio.charset.StandardCharsets;

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
        Document original = CrawlerPack.start().getFromJson(url);

        // 檢視 xml 內容
        //System.out.println( original.toString() );

        for(Element elem : original.select("row") ){
            // 印出交易商品資料
            System.out.print( elem.select("交易日期").text() );
            System.out.print( elem.select("作物名稱").text() );
            System.out.print( elem.select("平均價").text() );
            System.out.print( "\n" );
        }

        // 原始的 XmlParser 結果試試看下面這行
        // normalXmlParse();
    }

    // 如果使用預設的 xmlParser會發生什麼？ 試試看
    public static void normalXmlParse(){
        String json = CrawlerPack.getFromRemote(url);
        String xml = CrawlerPack.jsonToXml(json);

        // 原始 json 轉為 xml 的結果
        System.out.println( "原始XML" ) ;
        System.out.println( xml );

        Document jsoupDoc = Jsoup.parse(xml, "", Parser.xmlParser());
        jsoupDoc.charset(StandardCharsets.UTF_8);

        // 發生了什麼事？
        System.out.println( "轉換後XML" ) ;
        System.out.println(jsoupDoc.toString());


    }
}
