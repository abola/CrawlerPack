## JCConf 2015 TW 爬蟲組合包

簡易使用程式碼

    String api = "https://raw.githubusercontent.com/abola/CrawlerPack/master/test.json";
    Document jsoup = CrawlerPack.getFromJson(api);
    System.out.print( jsoup.select("results name").get(0).text() );
    
