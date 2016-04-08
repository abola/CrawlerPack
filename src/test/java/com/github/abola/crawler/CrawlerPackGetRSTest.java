package com.github.abola.crawler;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Abola Lee on 2016/4/9.
 */
public class CrawlerPackGetRSTest {

    @Test
    public void loadRemoteFile(){
        try {
            CrawlerPack
                    .start()
                    .getFromRemote("https://raw.githubusercontent.com/abola/CrawlerPack/master/test.json");
        }catch ( Exception anyException ){
            Assert.fail( "Load remote file fail. Cause by: " + anyException.getMessage() );
        }
    }
}
