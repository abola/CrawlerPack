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

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.PrefixXmlTreeBuilder;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 資料爬蟲包 
 *  
 * 
 * @author Abola Lee <abola921@gmail.com>
 */
public class CrawlerPack {

    static Logger log = LoggerFactory.getLogger(CrawlerPack.class);

    static StandardFileSystemManager fileSystem ;

    static{
        // create a Self-signed Server Certificates
        XTrustProvider.install();

        try {
            fileSystem = new StandardFileSystemManager();
            fileSystem.setCacheStrategy(CacheStrategy.ON_CALL);
            fileSystem.init();
        }catch(FileSystemException fse){
            // ignore
        }
    }

    static CrawlerPack defaultCrawler ;

    /**
     * Create a CrawlerPack instance
     *
     * @return CrawlerPack
     */
    public static CrawlerPack start(){
        if (null == defaultCrawler)
            defaultCrawler = new CrawlerPack();
        return defaultCrawler;
    }

    private List<Cookie> cookies = new ArrayList<Cookie>();


    /**
     * Creates a cookie with the given name and value.
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @return CrawlerPack
     */
    public CrawlerPack addCookie(String name, String value){
        if( null == name ) {
            log.warn("Cookie name null.");
            return this;
        }

        cookies.add( new Cookie("", name, value) );

        return this;
    }

    /**
     * Creates a cookie with the given name, value, domain attribute,
     * path attribute, expiration attribute, and secure attribute
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @param domain  the domain this cookie can be sent to
     * @param path    the path prefix for which this cookie can be sent
     * @param expires the {@link Date} at which this cookie expires,
     *                or <tt>null</tt> if the cookie expires at the end
     *                of the session
     * @param secure if true this cookie can only be sent over secure
     * connections
     *
     */
    public CrawlerPack addCookie(String domain, String name, String value,
                  String path, Date expires, boolean secure) {
        if( null == name ) {
            log.warn("Cookie name null.");
            return this;
        }

        cookies.add(new Cookie(domain, name, value, path, expires, secure));
        return this;
    }

    /**
     * Return a Cookie array
     * and auto importing domain and path when domain was empty.
     *
     * @param uri required Apache Common VFS supported file systems and response JSON format content.
     * @return Cookie[]
     */
    Cookie[] getCookies(String uri){
        if( null == cookies || 0 == cookies.size()) return null;

        for(Cookie cookie: cookies){

            if("".equals(cookie.getDomain())){
                String domain = uri.replaceAll("^.*:\\/\\/([^\\/]+)[\\/]?.*$", "$1");
                System.out.println(domain);
                cookie.setDomain(domain);
                cookie.setPath("/");
                cookie.setExpiryDate(null);
                cookie.setSecure(false);
            }
        }

        return cookies.toArray(new Cookie[cookies.size()]);
    }

    /**
     * Clear all cookies
     */
    void clearCookies(){
        cookies = new ArrayList<Cookie>();
    }


    /**
     * 取得遠端格式為 JSON 的資料
     *
     * @param uri required Apache Common VFS supported file systems and response JSON format content.
     * @return org.jsoup.nodes.Document 
     */
    public org.jsoup.nodes.Document getFromJson(String uri){
        // 取回資料，並轉化為XML格式
        String json = getFromRemote(uri);

        // 將 json 轉化為 xml
        String xml  = jsonToXml(json);

        // 轉化為 Jsoup 物件
        return xmlToJsoupDoc(xml);
    }

    /**
     * 取得遠端格式為 HTML/Html5 的資料
     *
     * @param uri required Apache Common VFS supported file systems and response HTML format content.
     * @return org.jsoup.nodes.Document
     */
    public org.jsoup.nodes.Document getFromHtml(String uri){
        // 取回資料
        String html = getFromRemote(uri);

        // 轉化為 Jsoup 物件
        return htmlToJsoupDoc(html);
    }

    /**
     * 取得遠端格式為 XML 的資料
     *
     * @param uri required Apache Common VFS supported file systems and response XML format content.
     * @return org.jsoup.nodes.Document 
     */
    public org.jsoup.nodes.Document getFromXml(String uri){
        // 取回資料，並轉化為XML格式
        String xml = getFromRemote(uri);

        // 轉化為 Jsoup 物件
        return xmlToJsoupDoc(xml);
    }


    /**
     * 將 json 轉為 XML
     *
     * @param json a json format string.
     * @return XML format string
     */
    public String jsonToXml(String json){
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
    public String getFromRemote(String uri){

        // clear cache
        fileSystem.getFilesCache().close();

        FileSystemOptions fsOptions = new FileSystemOptions();
        HttpFileSystemConfigBuilder.getInstance().setCookies(fsOptions, getCookies(uri) );

        String remoteContent = "";

        try {
            log.debug("Loading remote URI:" + uri);

            FileContent fileContent = fileSystem.resolveFile(uri, fsOptions).getContent();
            fileContent.getSize();  // pass a bug {@link https://issues.apache.org/jira/browse/VFS-427}

            String remoteEncoding = fileContent.getContentInfo().getContentEncoding();

            if (! "utf".equalsIgnoreCase(remoteEncoding.substring(0,3)) ){
                log.debug("remote content encoding: " + remoteEncoding);

                // force charset encoding if setRemoteEncoding set
                if (! "utf".equalsIgnoreCase(encoding.substring(0, 3)) ){
                    remoteEncoding = encoding;
                }else{
                    // auto detecting encoding
                    remoteEncoding = detectCharset(IOUtils.toByteArray( fileContent.getInputStream() ) );
                    log.info("real encoding: " + remoteEncoding);
                }
            }

            // 透過  Apache VFS 取回指定的遠端資料
            // 2016-02-29 fixed
            remoteContent = IOUtils.toString( fileContent.getInputStream(), remoteEncoding);

        }catch(IOException ioe){
            // return empty
            log.warn(ioe.getMessage());
        }

        clearCookies();

        return remoteContent;
    }

    /**
     * 將 HTML 轉化為 Jsoup Document 物件
     *
     * HTML的內容就使用Jsoup原生的 HTML Parser
     *
     * @param html Html document
     * @return org.jsoup.nodes.Document
     */
    public org.jsoup.nodes.Document htmlToJsoupDoc(String html){

        // 將 html(html/html5) 轉為 jsoup Document 物件
        Document jsoupDoc = Jsoup.parse(html, "UTF-8", Parser.htmlParser() );
        jsoupDoc.charset(StandardCharsets.UTF_8);

        return jsoupDoc;
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
     * @param xml XML format string
     * @return org.jsoup.nodes.Document
     */
    public org.jsoup.nodes.Document xmlToJsoupDoc(String xml){

        // Tag 首字元非 a-zA-Z 時轉化為註解的問題
        xml = xml.replaceAll("<([^A-Za-z\\/! ][^\\/>]*)>", "<"+prefix.toLowerCase()+"$1>")
                 .replaceAll("<\\/([^A-Za-z\\/ ][^\\/>]*)>", "</"+prefix.toLowerCase()+"$1>");

        // 將 xml 轉為 jsoup Document 物件
        Document jsoupDoc = Jsoup.parse(xml, "", new Parser( new PrefixXmlTreeBuilder(prefix.toLowerCase()) ) );
        jsoupDoc.charset(StandardCharsets.UTF_8);

        return jsoupDoc;
    }

    private String encoding = "utf-8";

    /**
     * 指定來源資料的編碼格式
     * 必需要在 get 前設定
     *
     * @return CrawlerPack
     */
    public CrawlerPack setRemoteEncoding(String encoding){
        this.encoding = encoding;
        return this;
    }

    private String detectCharset(byte[] content){
        return detectCharset(content, 0);
    }

    final Integer detectBuffer = 1000;

    /**
     * Detecting real content encoding
     * @param content
     * @param offset
     * @return real charset encoding
     */
    private String detectCharset(byte[] content, Integer offset){
        log.debug("offset: " + offset);

        // detect failed
        if( offset > content.length ) return null;

        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(content, offset, content.length - offset > detectBuffer ? detectBuffer : content.length - offset);
        detector.dataEnd();

        String detectEncoding = detector.getDetectedCharset();

        return null==detectEncoding?detectCharset(content,offset+detectBuffer):detectEncoding;
    }

}
