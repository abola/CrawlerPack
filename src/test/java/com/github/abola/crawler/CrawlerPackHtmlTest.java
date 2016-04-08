package com.github.abola.crawler;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Abola Lee <abola921@gmail.com>
 */
public class CrawlerPackHtmlTest {

    @Test
    public void htmlParserTest(){
        try {
            CrawlerPack.start()
                    .getFromHtml("https://www.google.com");
        }catch(Exception anyEx){
            Assert.fail("Html parsing error. CauseBy: " + anyEx.getMessage());
        }

    }
}
