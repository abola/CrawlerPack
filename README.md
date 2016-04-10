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
    <version>1.0.3</version>
</dependency>
```
To add a dependency using Gradle:
```gradle
dependencies {
    compile 'com.github.abola:crawler:1.0.3'
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
String url = "http://opendata2.epa.gov.tw/AQX.json";

CrawlerPack.start()
    .getFromJson(url)
    .getElementsByTag("pm2.5").text();
```

#### XML format example
```java    
// 104司人力銀行上 10萬月薪以上的工作資料
String url = "http://www.104.com.tw/i/apis/jobsearch.cfm?order=2&fmt=4&cols=JOB,NAME&slmin=100000&sltp=S&pgsz=20";
    
CrawlerPack.start()
    .getFromXml(url)
    .select("item").get(0).attr("job") ;
```
#### Html format example
```java
// ptt 笨版最新文章列表
String url = "https://www.ptt.cc/bbs/StupidClown/index.html";

CrawlerPack.start()
    .getFromHtml(url)
    .select("div.title > a").text();
```

#### Cookie example
```java
// ptt 八掛版創立首篇廢文標題
String url = "https://www.ptt.cc/bbs/Gossiping/M.1119222611.A.7A9.html";

CrawlerPack.start()
    .addCookie("over18","1")  // 必需在 getFromXXX 前設定Cookie
    .getFromHtml(url)
    .select("span:containsOwn(標題) + span:eq(1)").text();
```

#### Compressed data example
```java
// 北市Youbike資訊
String url = "gz:https://tcgbusfs.blob.core.windows.net/blobyoubike/YouBikeTP.gz";
    
// 目前編號0004站借用資訊
CrawlerPack.start()
    .getFromJson(url)
    .select("0004")
    .select("sarea, ar, tot, sbi").text();
```

## Milestone
* 給點建議如何 https://github.com/abola/CrawlerPack/issues/new

## Change log
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
