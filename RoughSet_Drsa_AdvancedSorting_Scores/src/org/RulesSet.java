package org;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RulesSet<T> {
    protected Map<Integer, Rule<T>> rulesMap =new HashMap<Integer, Rule<T>>();

    public RulesSet (ArrayList<Rule<T>> rules) {
        Map<Integer, Rule<T>> rulesMap = new HashMap<Integer, Rule<T>>();
        for(Rule<T> rule: rules) {
            rulesMap.put(rule.number,rule);
        }
        this.rulesMap = rulesMap;
    }

    public Map<Integer, Rule<T>> getRulesMap() {
        return rulesMap;
    }

    public void setRulesMap(Map<Integer, Rule<T>> rulesMap) {
        this.rulesMap = rulesMap;
    }
}
