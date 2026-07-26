package com.toastmagers.rule;

import org.junit.Assert;
import org.junit.Test;
import java.util.Collections;

public class RuleEngineTest {

    @Test
    public void testWhitelistBypass() {
        RuleEngine engine = new RuleEngine();
        engine.addWhitelistPackage("com.android.systemui");
        engine.addGlobalRule(new Rule("Block spam", Rule.Type.KEYWORD, "Spam", Rule.Action.BLOCK));

        // Whitelisted package should be allowed despite matching block rule
        Rule.Action action = engine.evaluate("com.android.systemui", "This is Spam!", null);
        Assert.assertEquals(Rule.Action.ALLOW, action);

        // Non-whitelisted package should be blocked
        Rule.Action action2 = engine.evaluate("com.rogue.app", "This is Spam!", null);
        Assert.assertEquals(Rule.Action.BLOCK, action2);
    }

    @Test
    public void testAppRulesAndGlobalRules() {
        RuleEngine engine = new RuleEngine();
        engine.addGlobalRule(new Rule("Block AD", Rule.Type.REGEX, ".*(领红包|点击查看).*", Rule.Action.BLOCK));

        // Create specific config for rogue app
        RuleEngine.AppRuleConfig rogueConfig = new RuleEngine.AppRuleConfig(false, Collections.singletonList("ad_channel"), false);
        rogueConfig.addRule(new Rule("Allow special rogue stuff", Rule.Type.KEYWORD, "VipUser", Rule.Action.ALLOW));
        engine.setAppRule("com.rogue.app", rogueConfig);

        // Global rule blocks "领红包" for normal apps
        Assert.assertEquals(Rule.Action.BLOCK, engine.evaluate("com.normal.app", "点击领红包啦！", null));

        // Whitelisted keyword in rogue app rule takes precedence over global block rule if it matches?
        // Let's check matching order. App rules evaluate first.
        Assert.assertEquals(Rule.Action.ALLOW, engine.evaluate("com.rogue.app", "VipUser: 点击领红包啦！", null));
        // Block notification channels in app config
        Assert.assertEquals(Rule.Action.BLOCK, engine.evaluate("com.rogue.app", "Hello", "ad_channel"));
    }

    @Test
    public void testRuleScalingBenchmark() {
        // T-RULE-04: Match scaling performance benchmark test
        RuleEngine engine = new RuleEngine();
        
        // Generate 1000 global rules to test performance scaling
        for (int i = 0; i < 1000; i++) {
            engine.addGlobalRule(new Rule("Rule #" + i, Rule.Type.KEYWORD, "RandomSpamKeyword_" + i, Rule.Action.BLOCK));
        }

        // Add matching rule at the very end
        engine.addGlobalRule(new Rule("Target rule", Rule.Type.KEYWORD, "TargetKeyword", Rule.Action.BLOCK));

        long start = System.nanoTime();
        Rule.Action action = engine.evaluate("com.test.app", "This is some normal text containing TargetKeyword", null);
        long durationNs = System.nanoTime() - start;

        Assert.assertEquals(Rule.Action.BLOCK, action);
        double durationMs = durationNs / 1_000_000.0;
        System.out.println("1000 rules matching took " + durationMs + " ms");
        
        // Ensure 1000 matches execute in a performant manner (e.g. under 100ms)
        Assert.assertTrue("Matching must be fast. Actual: " + durationMs + "ms", durationMs < 100);
    }
}
