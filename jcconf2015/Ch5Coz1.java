import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 104人力銀行 Job Search API 爬蟲範例
 *
 * api 文件
 * @see "http://www.104.com.tw/i/api_doc/jobsearch/documentation.cfm"
 *
 * @author Abola Lee <abola921@gmail.com>
 */
public class Ch5Coz1 {

    // 104 job search api
    // 目前月薪10萬以上的工作前200筆
    static String url = "http://www.104.com.tw/i/apis/jobsearch.cfm?order=2&fmt=4&cols=JOB,NAME&slmin=100000&sltp=S&pgsz=200";

    public static void main(String[] args){
        Document original = CrawlerPack.start().getFromXml(url);

        // 檢視 xml 內容
        //System.out.println(original.toString());

        // 看職稱跟app有關的有多少
        for(Element elem : original.select("item[job*=app]") ){
            System.out.println( "職稱:" + elem.attr("job") + ", 公司:" + elem.attr("name") );
        }
    }
}
