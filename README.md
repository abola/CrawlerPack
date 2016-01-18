## JCConf 2015 TW 爬蟲組合包

高效率資料爬蟲組合包 投影片 http://www.slideshare.net/ssuser438746/jcconf-2015-tw

簡易使用程式碼

    // 指定 URI format 來源
    String api = "https://raw.githubusercontent.com/abola/CrawlerPack/master/test.json";
    // 依資料格式，轉化為 Jsoup 物件
    Document jsoup = CrawlerPack.getFromJson(api);
    // 使用 CSS selector 來取用資料
    System.out.print( jsoup.select("results name").get(0).text() );

* 調整中項目 
* * 在 http/https 中使用 cookie 
* * 加入 maven repository

