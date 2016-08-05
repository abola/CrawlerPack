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
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 資料爬蟲包 
 *  
 * 
 * @author Abola Lee <abola921@gmail.com>
 */
public class CrawlerPack {

    static SimpleLog log = new SimpleLog("simple.logger.com.github.abola.crawler.CrawlerPack");

    static StandardFileSystemManager fileSystem ;

    static{
        // create a Self-signed Server Certificates
        // for pass SSL
        XTrustProvider.install();

        // Set default logging level "ERROR"
        log.setLevel(SimpleLog.LOG_LEVEL_WARN);

        try {

            fileSystem = new StandardFileSystemManager();

            fileSystem.setCacheStrategy(CacheStrategy.ON_CALL);

            // change default logger to SimpleLog
            fileSystem.setLogger(log);
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

    /**
     * Setting global level logging
     *
     * example:
     *   CrawlerPack.setLoggerLevel( SimpleLog.LOG_LEVEL_INFO );
     *
     * @param level
     */
    public static void setLoggerLevel(int level){
        log.setLevel(level);
        fileSystem.setLogger(log);
    }

    private String userAgent = "Mozilla/5.0 (CrawlerPack; )";

    private List<Cookie> cookies = new ArrayList<>();


    /**
     * Creates a cookie with the given name and value.
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @return CrawlerPack
     */
    public CrawlerPack addCookie(String name, String value){
        if( null == name ) {
            log.warn("addCookie: Cookie name null.");
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
            log.warn("addCookie: Cookie name null.");
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
        log.trace("clearCookies: clear all cookies.");
        cookies = new ArrayList<>();
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

        String remoteContent ;
        String remoteEncoding = "utf-8";

        log.debug("getFromRemote: Loading remote URI=" + uri);
        FileContent fileContent ;

        try {

            FileSystemOptions fsOptions = new FileSystemOptions();
            // set userAgent
            HttpFileSystemConfigBuilder.getInstance().setUserAgent(fsOptions, userAgent);

            // set cookie if cookies set
            if (0 < this.cookies.size()) {
                HttpFileSystemConfigBuilder.getInstance().setCookies(fsOptions, getCookies(uri));
            }

            log.debug("getFromRemote: userAgent=" + userAgent);
            log.debug("getFromRemote: cookieSize=" + cookies.size());
            log.debug("getFromRemote: cookies=" + cookies.toString());

            fileContent = fileSystem.resolveFile(uri, fsOptions).getContent();

            // 2016-03-22 only pure http/https auto detect encoding
            if ("http".equalsIgnoreCase(uri.substring(0, 4))) {
                fileContent.getSize();  // pass a bug {@link https://issues.apache.org/jira/browse/VFS-427}
                remoteEncoding = fileContent.getContentInfo().getContentEncoding();
            }

            log.debug("getFromRemote: remoteEncoding=" + remoteEncoding + "(auto detect) ");

            // 2016-03-21 修正zip file getContentEncoding 為null
            if (null == remoteEncoding) remoteEncoding = "utf-8";

            if (!"utf".equalsIgnoreCase(remoteEncoding.substring(0, 3))) {
                log.debug("getFromRemote: remote content encoding=" + remoteEncoding);

                // force charset encoding if setRemoteEncoding set
                if (!"utf".equalsIgnoreCase(encoding.substring(0, 3))) {
                    remoteEncoding = encoding;
                } else {
                    // auto detecting encoding
                    remoteEncoding = detectCharset(IOUtils.toByteArray(fileContent.getInputStream()));
                    log.debug("getFromRemote: real encoding=" + remoteEncoding);
                }
            }

            // 透過  Apache VFS 取回指定的遠端資料
            // 2016-02-29 fixed
            remoteContent = IOUtils.toString(fileContent.getInputStream(), remoteEncoding);

        } catch(FileSystemException fse){
            log.warn("getFromRemote: FileSystemException=" + fse.getMessage());
            remoteContent =null;
        }catch(IOException ioe){
            // return empty
            log.warn("getFromRemote: IOException=" + ioe.getMessage());
            remoteContent =null;
        }catch(StringIndexOutOfBoundsException stre){
            log.warn("getFromRemote: StringIndexOutOfBoundsException=" + stre.getMessage());
            log.warn("getFromRemote: uri=" + uri );
            log.warn(stre.getMessage());
            remoteContent =null;
        }

        clearCookies();

        log.debug("getFromRemote: remoteContent=\n" + remoteContent);
        // any exception will return "null"
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
     * Jsoup 1.9.1+ supported non-ascii tag
     * -----
     * 如果碰到Tag 名稱首字元非 a-zA-Z 的字元，jsoup 會解析為註解
     * 所以必需用騙的先置入 prefix
     * 再改寫xmlParse 在回傳時移除prefix
     *
     * @param xml XML format string
     * @return org.jsoup.nodes.Document
     */
    public org.jsoup.nodes.Document xmlToJsoupDoc(String xml){

        // Tag 首字元非 a-zA-Z 時轉化為註解的問題
        //xml = xml.replaceAll("<([^A-Za-z\\/! ][^\\/>]*)>", "<"+prefix.toLowerCase()+"$1>")
        //         .replaceAll("<\\/([^A-Za-z\\/ ][^\\/>]*)>", "</"+prefix.toLowerCase()+"$1>");

        // 將 xml 轉為 jsoup Document 物件
        //Document jsoupDoc = Jsoup.parse(xml, "", new Parser( new PrefixXmlTreeBuilder(prefix.toLowerCase()) ) );

        Document jsoupDoc = Jsoup.parse(xml, "", Parser.xmlParser() );
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
        log.debug("setRemoteEncoding: encoding=" + encoding);
        this.encoding = encoding;
        return this;
    }

    private String detectCharset(byte[] content){
        log.debug("detectCharset: ");
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
        log.debug("detectCharset: offset=" + offset);

        // detect failed
        if( offset > content.length ) return null;

        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(content, offset, content.length - offset > detectBuffer ? detectBuffer : content.length - offset);
        detector.dataEnd();

        String detectEncoding = detector.getDetectedCharset();

        return null==detectEncoding?detectCharset(content,offset+detectBuffer):detectEncoding;
    }


    /**
     * set header userAgent
     *
     * @param userAgent
     * @return CrawlerPack
     */
    public CrawlerPack setUserAgent(String userAgent){
        log.debug("setUserAgent: userAgent=\"" + userAgent + "\"");
        this.userAgent = userAgent;
        return this;
    }
}
