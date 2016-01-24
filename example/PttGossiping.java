import com.github.abola.crawler.CrawlerPack;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 範例: 使用爬蟲包取得八卦版最後50篇文章的網址
 */
class PttGossiping {

    final static String gossipMainPage = "https://www.ptt.cc/bbs/Gossiping/index.html";
    final static String gossipIndexPage = "https://www.ptt.cc/bbs/Gossiping/index%s.html";
    // 取得最後幾篇的文章數量
    static Integer loadLastPosts = 50;

    public static void main(String[] argv){

        String prevPage =
            CrawlerPack.start()
                .addCookie("over18","1")                // 八卦版進入需要設定cookie
                .getFromHtml(gossipMainPage)            // 遠端資料格式為 HTML
                .select(".action-bar .pull-right > a")  // 取得右上角『前一頁』的內容
                .get(1).attr("href")
                .replaceAll("/bbs/Gossiping/index([0-9]+).html", "$1");
        // 目前最末頁 index 編號
        Integer lastPage = Integer.valueOf(prevPage)+1;

        List<String> lastPostsLink = new ArrayList<String>();

        while ( loadLastPosts > lastPostsLink.size() ){
            String currPage = String.format(gossipIndexPage, lastPage--);

            Elements links =
                CrawlerPack.start()
                    .addCookie("over18", "1")
                    .getFromHtml(currPage)
                    .select(".title > a");

            for( Element link: links) lastPostsLink.add( link.attr("href") );
        }

        // 檢視結果
        for(String url : lastPostsLink){
            System.out.println(url);
        }
    }
}
