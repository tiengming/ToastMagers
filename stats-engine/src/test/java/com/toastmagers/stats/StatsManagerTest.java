package com.toastmagers.stats;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class StatsManagerTest {

    private StatsManager statsManager;

    @Before
    public void setUp() {
        statsManager = new StatsManager();
    }

    @Test
    public void testRecordInterceptAndStats() {
        statsManager.recordIntercept("com.ad.rogue", "ad_channel", "NOTIFICATION");
        statsManager.recordIntercept("com.ad.rogue", "ad_channel", "NOTIFICATION");
        statsManager.recordIntercept("com.ad.rogue", null, "TOAST");
        statsManager.recordIntercept("com.spam.app", "marketing", "NOTIFICATION");
        statsManager.recordPassThrough("com.important.chat");

        Assert.assertEquals(4, statsManager.getTotalIntercepts());
        Assert.assertEquals(1, statsManager.getTotalAllowed());
        Assert.assertEquals(1, statsManager.getTotalToastIntercepts());
        Assert.assertEquals(3, statsManager.getTotalNotificationIntercepts());

        Assert.assertEquals(3, statsManager.getAppInterceptCount("com.ad.rogue"));
        Assert.assertEquals(1, statsManager.getAppInterceptCount("com.spam.app"));

        Map<String, Long> topApps = statsManager.getTopBlockedApps(5);
        Assert.assertFalse(topApps.isEmpty());
        Assert.assertEquals(Long.valueOf(3), topApps.get("com.ad.rogue"));
    }

    @Test
    public void testReset() {
        statsManager.recordIntercept("com.ad.rogue", "ad_channel", "NOTIFICATION");
        Assert.assertEquals(1, statsManager.getTotalIntercepts());

        statsManager.reset();
        Assert.assertEquals(0, statsManager.getTotalIntercepts());
        Assert.assertEquals(0, statsManager.getAppInterceptCount("com.ad.rogue"));
    }
}
