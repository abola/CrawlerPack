## JCConf 2015 TW 爬蟲組合包

高效率資料爬蟲組合包 投影片 http://www.slideshare.net/ssuser438746/jcconf-2015-tw

簡易使用程式碼

    String api = "https://raw.githubusercontent.com/abola/CrawlerPack/master/test.json";
    Document jsoup = CrawlerPack.getFromJson(api);
    System.out.print( jsoup.select("results name").get(0).text() );
    
