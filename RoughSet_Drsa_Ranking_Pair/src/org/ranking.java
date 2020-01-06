package org;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ranking {

    public Matrix matrix;
    public RulesSet rulesSet;
    public Map<Pair, String> relationMap =new HashMap<Pair,  String>();

    public ranking(Matrix matrix, RulesSet rulesSet) {
        this.matrix = matrix;
        this.rulesSet = rulesSet;
    }

    public void sort() {
        Map<Pair, String> relationMap =new HashMap<Pair,  String>();
        for(Variant variantX: matrix.variants.values()) {
            for(Variant variantY: matrix.variants.values()) {
                String relation = computeRelation(variantX,variantY);
                Pair variantsPair = new Pair(variantX.name,variantY.name);
                relationMap.put(variantsPair, relation);
            }
        }
        this.relationMap = relationMap;
    }

    public String computeRelation (Variant variantX, Variant variantY) {
        ArrayList<Rule> allRules = new ArrayList<Rule>(rulesSet.rulesMap.values());
        String relation = "";
        for(Rule<RuleConditionElementDelta> rule: allRules) {
            boolean goodConditions = true;
            for(RuleConditionElementDelta condition: rule.conditions) {
                if(condition.delta) {
                    String variantXValue = variantX.valuesMap.get(condition.leftSide);
                    double variantXIntValue = variantX.getIntValue(condition.leftSide,variantXValue);
                    String variantYValue = variantY.valuesMap.get(condition.leftSide);
                    double variantYIntValue = variantY.getIntValue(condition.leftSide,variantYValue);
                    double delta;
                    if(condition.conditionPair.elementLeft.equals("x")) {
                        delta = variantXIntValue-variantYIntValue;
                    } else {
                        delta = variantYIntValue-variantXIntValue;
                    }
                    if(condition.operator.equals(">=")) {
                        if(delta < condition.rightSide) {
                            goodConditions = false;
                            break;
                        }
                    } else{
                        if(delta > condition.rightSide) {
                            goodConditions = false;
                            break;
                        }
                    }
                } else {
                    Variant variantInCondition = condition.conditionPair.elementLeft.equals("x") ? variantX : variantY;
                    String variantValue = variantInCondition.valuesMap.get(condition.leftSide);
                    double variantIntValue = variantInCondition.getIntValue(condition.leftSide,variantValue);

                    if(condition.operator.equals(">=")) {
                        if(variantIntValue < condition.rightSide) {
                            goodConditions = false;
                            break;
                        }
                    } else{
                        if(variantIntValue > condition.rightSide) {
                            goodConditions = false;
                            break;
                        }
                    }
                }
            }
            if(goodConditions) {
                for(RuleDecisionElement decision: rule.decision) {
                    if(decision.decisionClass.equals("S")) {
                        if(relation.equals("")) {
                            relation = "S";
                        } else if(relation.equals("Sc")) {
                            relation = "SSc";
                        }
                    } else if(decision.decisionClass.equals("Sc")) {
                        if(relation.equals("")) {
                            relation = "Sc";
                        } else if(relation.equals("S")) {
                            relation = "SSc";
                        }
                    }
                    else {
                        relation = "SSc";
                    }
                }
                if(relation.equals("SSc")) {
                    break;
                }

            }
        }
        if(relation.equals("")) {
            relation = "incomparable";
        }
        return relation;
    }
}
