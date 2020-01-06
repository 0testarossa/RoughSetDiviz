package org;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Rule<T> {
    protected int number;
    protected ArrayList<T> conditions = new ArrayList<>();
    protected ArrayList<RuleDecisionElement> decision = new ArrayList<RuleDecisionElement>();
    protected Map<String, Double> ruleFactorsMap =new HashMap<String,  Double>();

    public Rule (int number, ArrayList<T> conditions, ArrayList<RuleDecisionElement> decision, Map<String, Double> ruleFactorMap) {
        this.number = number;
        this.conditions = conditions;
        this.decision = decision;
        this.ruleFactorsMap = ruleFactorMap;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public ArrayList<T> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<T> conditions) {
        this.conditions = conditions;
    }

    public ArrayList<RuleDecisionElement> getDecision() {
        return decision;
    }

    public void setDecision(ArrayList<RuleDecisionElement> decision) {
        this.decision = decision;
    }

    public Map<String, Double> getRuleFactorsMap() {
        return ruleFactorsMap;
    }

    public void setRuleFactorsMap(Map<String, Double> ruleFactorsMap) {
        this.ruleFactorsMap = ruleFactorsMap;
    }
}
