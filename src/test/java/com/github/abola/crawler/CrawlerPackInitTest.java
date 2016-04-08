package com.github.abola.crawler;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by abolalee on 2016/4/8.
 */
public class CrawlerPackInitTest {

    @Test
    public void startCrawlerInstance(){
        try {
            CrawlerPack.start();
        }catch ( Exception anyException ){
            Assert.fail( "Create CrawlerPack instance fail. Cause by: " + anyException.getMessage() );
        }
    }

}
