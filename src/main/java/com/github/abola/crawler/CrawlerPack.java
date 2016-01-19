/**
 * Copyright 2015-2016 Abola Lee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.abola.crawler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.VFS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.PrefixXmlTreeBuilder;

import java.nio.charset.StandardCharsets;

/**
 * 資料爬蟲包 
 *  
 * 
 * @author Abola Lee <abola921@gmail.com>
 * @since 0.9
 */
public class CrawlerPack {

    // create a Self-signed Server Certificates 
    static{
        XTrustProvider.install();
    }
    /**
     * 取得遠端格式為 JSON 的資料
     *
     * @param url required Apache Common VFS supported file systems and response JSON format content. 
     * @return org.jsoup.nodes.Document 
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
     * @param url required Apache Common VFS supported file systems and response XML format content. 
     * @return org.jsoup.nodes.Document 
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
     * 
     * @param url required Apache Common VFS supported file systems and response XML format content.
     * @return org.jsoup.nodes.Document 
     */
    public static org.jsoup.nodes.Document getFromHtml(String url){
        return getFromXml(url);
    }

    /**
     * 將 json 轉為 XML
     * 
     * @param a json format string.
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
     * @see <a href="https://commons.apache.org/proper/commons-vfs/filesystems.html">commons-vfs filesystems</a>
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
     * @param XML
     * @return org.jsoup.nodes.Document
     */
    public static org.jsoup.nodes.Document xmlToJsoupDoc(String xml){

        // Tag 首字元非 a-zA-Z 時轉化為註解的問題
        xml = xml.replaceAll("<([^A-Za-z\\/][^\\/>]*)>", "<"+prefix.toLowerCase()+"$1>")
                 .replaceAll("<\\/([^A-Za-z\\/][^\\/>]*)>", "</"+prefix.toLowerCase()+"$1>");

        // 將 xml(html/html5) 轉為 jsoup Document 物件
        Document jsoupDoc = Jsoup.parse(xml, "", new Parser( new PrefixXmlTreeBuilder(prefix.toLowerCase()) ) );
        jsoupDoc.charset(StandardCharsets.UTF_8);


        return jsoupDoc;
    }
}
