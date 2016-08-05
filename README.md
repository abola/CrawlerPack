# Java 網路資料爬蟲包
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.abola/crawler/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.abola/crawler)
[![Travis-ci build status](https://travis-ci.org/abola/CrawlerPack.svg)](https://travis-ci.org/abola/CrawlerPack)

本套件為網路上常見的資料協定、格式，提供了簡易且方便(easy-to-use)的操作接口。套件主要以Jsoup為核心擴展，整合Apache Commons-VFS後，提供更多種協定的操作，也可支援壓縮格式處理。

Requires JDK 1.7 or higher

To add a dependency on CrawlerPack using Maven, use the following:
```xml
<dependency>
    <groupId>com.github.abola</groupId>
    <artifactId>crawler</artifactId>
    <version>1.0.3-1</version>
</dependency>
```
To add a dependency using Gradle:
```gradle
dependencies {
    compile 'com.github.abola:crawler:1.0.3-1'
}
```

And easy-to-use example
```java
// URI format source
String uri = "https://raw.githubusercontent.com/abola/CrawlerPack/master/test.json";
    
CrawlerPack.start()
    .getFromJson(uri)
    .select("results name").text() ;
```

## 爬蟲包特色
### 支援常見網路協定
使用 Apache Commons-VFS 所支援所有協定，常見網路協定如http/https,samba(cifs),ftp,sftp等…詳細列表請參考 https://commons.apache.org/proper/commons-vfs/filesystems.html

### 支援常見資料格式
* JSON
* XML
* HTML 

### 支援 中文XML標籤 / 中文JSON欄位
爬蟲包套件可正常的處理使用中文命名的XML或JSON

XML
```xml
<集合>
    <元素>元素名稱1</元素>
    <元素>元素名稱2</元素>
</集合>
```

JSON
```json
{"集合":[
    {"元素":"元素名稱1"}
    , {"元素":"元素名稱2"}
]}
```
### 自動偵測遠端資料編碼
爬蟲包建議使用 UTF-8 操作資料。針對非 UTF-8 編碼的遠端資料，爬蟲包預設會啟動自動偵測編碼，並將其轉換為 UTF-8 編碼。

注意，預設啟用的自動編碼，效能會明顯的不如直接指定編碼，平均測試較直接指定編碼的目標多出300ms以上耗費時間。如果遠端資料非 UTF-8 編碼，大量資料擷取時，直接指定遠端編碼，可有效減少你作業整體耗時。

```java
// TWSE 2015'三大法人買賣金額統計表
String uri = "http://www.twse.com.tw/ch/trading/fund/BFI82U/BFI82U_print.php"
            +"?begin_date=20150101&end_date=20151231&report_type=month";

CrawlerPack.start()
    .setRemoteEncoding("big5")  // 直接指定遠端編碼
    .getFromHtml(uri)
    .select("table.board_trad > tbody > tr:nth-child(7) > td:nth-child(4)").text()
```

## 一般使用範例

#### JSON format example
```java
// 即時PM2.5資料
String uri = "http://opendata2.epa.gov.tw/AQX.json";

CrawlerPack.start()
    .getFromJson(uri)
    .getElementsByTag("pm2.5").text();
```

#### XML format example
```java    
// 104司人力銀行上 10萬月薪以上的工作資料
String uri = "http://www.104.com.tw/i/apis/jobsearch.cfm?order=2&fmt=4&cols=JOB,NAME&slmin=100000&sltp=S&pgsz=20";
    
CrawlerPack.start()
    .getFromXml(uri)
    .select("item").get(0).attr("job") ;
```
#### Html format example
```java
// ptt 笨版最新文章列表
String uri = "https://www.ptt.cc/bbs/StupidClown/index.html";

CrawlerPack.start()
    .getFromHtml(uri)
    .select("div.title > a").text();
```

#### set userAgent example (CrawlerPack >= 1.1)
```java
System.out.println(
  CrawlerPack.start()
    .setUserAgent("")
    .getFromHtml(uri)
    .select("*")
);    
```

#### Cookie example
```java
// ptt 八掛版創立首篇廢文標題
String uri = "https://www.ptt.cc/bbs/Gossiping/M.1119222611.A.7A9.html";

CrawlerPack.start()
    .addCookie("over18","1")  // 必需在 getFromXXX 前設定Cookie
    .getFromHtml(uri)
    .select("span:containsOwn(標題) + span:eq(1)").text();
```

#### Compressed data example (gzip/gz)
```java
// 北市Youbike資訊
String uri = "gz:https://tcgbusfs.blob.core.windows.net/blobyoubike/YouBikeTP.gz";

// 列出所有大安區內的租借站
CrawlerPack.start()
    .getFromJson(uri)
    .select("retVal > *:contains(大安區)")
```

#### Compressed data example (zip)
```java
// 內政部實價登錄
String uri = "zip:http://plvr.land.moi.gov.tw"
             + "/Download?type=zip&fileName=lvr_landxml.zip"
             + "!/A_LVR_LAND_A.XML";  // 解壓縮後取出的檔案路徑+名稱

// org.jsoup.select.Elements
Elements elems = CrawlerPack.start()
                    .getFromXml(uri)
                    .select("買賣");

for(Element elem: elems){
    System.out.println(
        elem.select("鄉鎮市區").text() +
        "," + elem.select("總價元").text()
    );
}
```

## Tips

#### 指定文件編碼 
爬蟲包的主要目標，是提供簡易入門的操作模式。然而爬蟲包的效能並不理想，主要原因是編碼偵測
，為了降低預設操作難度，使用了 [juniversalchardet](https://code.google.com/archive/p/juniversalchardet/)
自動偵測遠端內容編碼。直接指定遠端編碼可跳過自動偵測，提升一點效能。如果遠端為UTF8編碼
，便不需要再指定。


以台灣證交所網站為例，若不指定編碼時，平均約600ms完成
```java
// TWSE 2015'三大法人買賣金額統計表
String uri = "http://www.twse.com.tw/ch/trading/fund/BFI82U/BFI82U_print.php"
            +"?begin_date=20150101&end_date=20151231&report_type=month";

# Guava Stopwatch
Stopwatch timer = Stopwatch.createStarted();
CrawlerPack.start()
    .getFromHtml(uri);
System.out.println( timer.stop().toString() );
// avg 600ms 
```


指定遠端編碼為big5後，減少了一點時間，減少的時間，會與你的處理器效能有關
```java
Stopwatch timer = Stopwatch.createStarted();
CrawlerPack.start()
    .setRemoteEncoding("big5")
    .getFromHtml(uri);
System.out.println( timer.stop().toString() );
// avg 480ms 
```

#### 設定 User Agent
部份網站會使用 User-Agent 來阻擋GoogleBot或爬蟲。爬蟲包( >= 1.1)預設會偽裝為一般瀏覽器。
 

|套件              |預設User-Agent|
|---               |:--|
|Jsoup             |Java/1.8.0_20|
|Apache Commons VFS|Jakarta-Commons-VFS|
|CrawlerPack       |Mozilla/5.0 (CrawlerPack; )|

## 除錯 (CrawlerPack >= 1.1)
爬蟲包內預設除錯訊息等級為『Warn』，如果要調整除錯的等級，可依照下面範例調整
```java
// set to debug level
CrawlerPack.setLoggerLevel(SimpleLog.LOG_LEVEL_DEBUG);
 
// turn off logging
CrawlerPack.setLoggerLevel(SimpleLog.LOG_LEVEL_OFF);
```


----


## Change log
#### 1.1
* 主要調動
    - 更新: Jsoup 套件版本至 1.9.2
    - 更新: JAVA-Json 套件版本至 20160212
    - 更新: 移除 Slf4j 套件需求
    - 調整: XML解析器改用原生 Jsoup XML parser (新版 Jsoup 已支援non-ASCII字元XML解析)


* 新功能: static CrawlerPack.setLoggingLevel(int level) 可調整爬蟲包除錯訊息等級
* 新功能: userAgent(String agent) 可調整userAgent的內容


* 調整: 爬蟲包預設除錯訊息等級，調整至 Warn
* 調整: 爬蟲包取得http/https，現在預設會加入userAgent資訊
 



#### 1.0.3-1
* 更新 Apache Commons-VFS 套件版本至 2.1 
 
#### 1.0.3
* 修正(跳過) 壓縮格式無法取得字元長度的臭蟲

#### 1.0.2
* 修正抓取含路徑的打包檔時會出現 NullPointerException 問題
* 修正自動編碼偵測造成資料清空的bug

#### 1.0.1
* 調整 getFromHtml 改使用 Jsoup 內建 Html parser
* 增加自動編碼偵測功能  (add library juniversalchardet)
* 增加 setRemoteEncoding(String encoding) 設定遠端內文編碼

#### 1.0.0
* 調整 api 操作界面
* 增加對Cookie的支援

#### 0.9.2
* 修正解析註解以及 js 中特殊符號的錯誤
* 修正動態網頁資料被cache的問題

#### 0.9.1
* 增加授權，使用Apache 2.0 公開授權
* 專案已上傳至公開的 Maven Repository 現在可以直接透過pom.xml使用爬蟲包
* 修正 https PKIX 驗證無法通過的問題

## Reference
* JCConf 2015 TW 高效率資料爬蟲組合包 投影片 http://www.slideshare.net/ssuser438746/jcconf-2015-tw
