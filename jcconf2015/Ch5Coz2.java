import org.jsoup.nodes.Document;

/**
 * Facebook Graph API 爬蟲範例
 *
 * 範例 Graph API 內容參考
 * https://developers.facebook.com/tools/explorer?method=GET&path=152168711507282&version=v2.0
 *
 * @author Abola Lee <abola921@gmail.com>
 */
public class Ch5Coz2 {

    // FB Graph API access token
    // @see https://developers.facebook.com/docs/facebook-login/access-tokens
    static String token = "<your..token..here>";

    // API url
    static String url = "https://graph.facebook.com"
                        // Graph API version
                        + "/v2.0"
                        // Graph API query
                        + "/152168711507282?fields=id,name,location"
                        // 地區
                        + "&locale=zh_TW"
                        // Graph API Token
                        + "&access_token=" + token
                        ;

    public static void main(String[] args){

        Document original = CrawlerPack.start().getFromJson(url);

        // 檢視 xml 內容
        //System.out.println(original.toString());

        // 抓地點座標
        System.out.println( original.select("location latitude").text() );
        System.out.println( original.select("location longitude").text() );
    }    
}
