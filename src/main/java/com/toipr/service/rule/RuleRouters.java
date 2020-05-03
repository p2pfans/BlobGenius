package com.toipr.service.rule;

import com.toipr.model.data.DataRule;
import com.toipr.service.rule.impl.DefaultRuleRouter;

import java.util.HashMap;
import java.util.Map;

public class RuleRouters {
    protected static Map<String, RuleRouter> mapRegistry = new HashMap<String, RuleRouter>();

    public static RuleRouter getRuleRouter(String name){
        if(mapRegistry.containsKey(name)){
            return mapRegistry.get(name);
        }
        return null;
    }

    public static RuleRouter getRuleRouter(String rid, String dataType){
        String resType = rid + "-" + dataType;
        if(mapRegistry.containsKey(resType)){
            return mapRegistry.get(resType);
        }
        return null;
    }

    public static synchronized RuleRouter getRuleRouter(DataRule rule){
        String resType = rule.getRid() + "-" + rule.getDataType();
        return getRuleRouter(resType, rule.getRuleHost(), rule.getRuleTable());
    }

    public static synchronized RuleRouter getRuleRouter(String name, String ruleNode, String ruleTable){
        if(mapRegistry.containsKey(name)){
            return mapRegistry.get(name);
        }

        DefaultRuleRouter router = new DefaultRuleRouter();
        if(!router.init(ruleNode, ruleTable)){
            return null;
        }
        mapRegistry.put(name, router);
        return router;
    }
}
