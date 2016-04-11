Java 網路資料爬蟲包 GitHub: [https://github.com/abola/CrawlerPack](https://github.com/abola/CrawlerPack)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.abola/crawler/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.abola/crawler)

----

### 關於爬蟲包 ###
在Java上開發爬蟲並不困難，最常見的套件便是[Jsoup](http://jsoup.org/)。
然而[Jsoup](http://jsoup.org/)仍有許多問題。例如無法取得 http/https 以外協定的
資源、無法使用中文XML、不支援壓縮格式等等…以致使用Java來開發爬蟲，往往需要使用相當多
套件，同時也編寫更多的程式碼。爬蟲開發的便利性，相較其它語言；如Python，確實相對遜色。

爬蟲包的產生，主要是以[Jsoup](http://jsoup.org/)為核心，整合各式套件，修補中文處理
問題。並提供***SOP開發模式***，減少開發時間，且更容易上手。

同時爬蟲包也是一個開源專案，採Apache2.0授權*(可商用、再修改)*。
並已上傳至[公開伺服器](https://maven-badges.herokuapp.com/maven-central/com.github.abola/crawler)
提供下載，你可以很簡易的將爬蟲包加入專案中

範例 Maven pom.xml，[其它支援格式](https://maven-badges.herokuapp.com/maven-central/com.github.abola/crawler)
```xml
<dependency>
    <groupId>com.github.abola</groupId>
    <artifactId>crawler</artifactId>
    <version>[1.0.0,2.0.0)</version>
</dependency>
```

----

使用爬蟲包，通常你可以在簡短的數行程式碼中，完成許多繁鎖的工作
```java
// 北市Youbike資訊
String uri = "gz:https://tcgbusfs.blob.core.windows.net/blobyoubike/YouBikeTP.gz";

// 列出所有大安區內的租借站
CrawlerPack.start()
    .getFromJson(uri)
    .select("retVal > *:contains(大安區)")
```

上述範例中，至少進行了以下項目
- 使用 https 協定，取得遠端資源
- 解縮壓檔案 YouBikeTP.gz
- 讀取／解析 Json 格式內容
- 將 Json 轉換為 [Jsoup](http://jsoup.org/) 物件
- 使用 [Jsoup](http://jsoup.org/) 內建的 CSS Selector 取出資料

----

next: 爬蟲包 - SOP開發模式

----

### 爬蟲包 - SOP開發模式 ###

1. **URI定義遠端資源**
: 其中包含各式協定與解壓縮的方式

2. **資料格式解析轉換**
: 支援Json,XML,HTML三種主要資料格式，並轉換為 [Jsoup](http://jsoup.org/) 物件

3. **操作Jsoup套件功能**
: 如 CSS Selector, DOM

----



