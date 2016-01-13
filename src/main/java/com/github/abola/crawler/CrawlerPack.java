/**
 * 爬蟲組合包
 *
 * @author Abola Lee <abola921@gmail.com>
 */
package com.github.abola.crawler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.VFS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.MyXmlTreeBuilder;
import org.jsoup.parser.Parser;

import java.nio.charset.StandardCharsets;

public class CrawlerPack {

    /**
     * 取得遠端格式為 JSON 的資料
     *
     * @param url required Apache Common VFS supported file systems
     * @return Jsoup Document
     */
    public static org.jsoup.nodes.Document getFromJson(String url){
        // 取回資料，並轉化為XML格式
        String json = getFromRemote(url);

        // 將 json 轉化為 xml
        String xml  = jsonToXml(json);

        // Custom code here

        // 轉化為 Jsoup 物件
        return xmlToJsoupDoc(xml);
    }

    /**
     * 取得遠端格式為 XML 的資料
     *
     * @param url required Apache Common VFS supported file systems
     * @return Jsoup Document
     */
    public static org.jsoup.nodes.Document getFromXml(String url){
        // 取回資料，並轉化為XML格式
        String xml = getFromRemote(url);

        // Custom code here

        // 轉化為 Jsoup 物件
        return xmlToJsoupDoc(xml);
    }

    /**
     * HTML 與 XML 處理模式相同
     */
    public static org.jsoup.nodes.Document getFromHtml(String url){
        return getFromXml(url);
    }

    /**
     * 將 json 轉為 XML
     * @param json
     * @return
     */
    public static String jsonToXml(String json){
        String xml = "";
        // 處理直接以陣列開頭的JSON，並指定給予 row 的 tag
        if ( "[".equals( json.substring(0,1) ) ){
            xml = XML.toString(new JSONArray(json), "row");
        }else{
            xml = XML.toString(new JSONObject(json));
        }

        return xml;
    }

    /**
     * 透過 Apache Common VFS 套件 取回遠端的資源
     *
     * 能使用的協定參考：
     * @see "https://commons.apache.org/proper/commons-vfs/filesystems.html"
     */
    public static String getFromRemote(String url){
        try {
            // 透過  Apache VFS 取回指定的遠端資料
            return IOUtils.toString(
                    VFS.getManager().resolveFile(url).getContent().getInputStream()
                    , "UTF-8"
            );
        }catch(Exception ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    // 替換字元：一定要是 a-zA-Z 開頭的組合
    final static String prefix = "all-lower-case-prefix";

    /**
     * 將 XML 轉化為 Jsoup Document 物件
     *
     * 如果碰到Tag 名稱首字元非 a-zA-Z 的字元，jsoup 會解析為註解
     * 所以必需用騙的先置入 prefix
     * 再改寫xmlParse 在回傳時移除prefix
     *
     */
    public static org.jsoup.nodes.Document xmlToJsoupDoc(String xml){

        // Tag 首字元非 a-zA-Z 時轉化為註解的問題
        xml = xml.replaceAll("<([^A-Za-z\\/][^\\/>]*)>", "<"+prefix.toLowerCase()+"$1>")
                 .replaceAll("<\\/([^A-Za-z\\/][^\\/>]*)>", "</"+prefix.toLowerCase()+"$1>");

        // 將 xml(html/html5) 轉為 jsoup Document 物件
        Document jsoupDoc = Jsoup.parse(xml, "", new Parser( new MyXmlTreeBuilder(prefix.toLowerCase()) ) );
        jsoupDoc.charset(StandardCharsets.UTF_8);


        return jsoupDoc;
    }
}
