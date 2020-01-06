package org;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SimpleSorting {
    protected Matrix matrix;
    protected RulesSet rulesSet;
    protected Map<Pair, Integer> scoresMap =new HashMap<Pair,  Integer>();

    public SimpleSorting(Matrix matrix, RulesSet rulesSet) {
        this.matrix = matrix;
        this.rulesSet = rulesSet;
    }

    public void sort() {
        Map<Pair, Integer> scoresMap =new HashMap<Pair,  Integer>();
        for(Variant variant: matrix.variants.values()) {
            scoresMap = computeScore(variant, scoresMap);
        }
        this.scoresMap = scoresMap;
    }

    public Map<Pair, Integer> computeScore(Variant variant, Map<Pair, Integer> scoresMap) {
        ArrayList<Rule> allRules = new ArrayList<Rule>(rulesSet.rulesMap.values());
        ArrayList<Rule> allCoveringRules = new ArrayList<Rule>();
        for(Rule<RuleConditionElement> rule: allRules){
            boolean goodRuleCondition = true;
            for(RuleConditionElement condition: rule.conditions) {
                String attribute = condition.leftSide;
                String operator = condition.operator;
                double conditionValue = condition.rightSide;

                String variantValue = variant.valuesMap.get(attribute);
                double intVariantValue = variant.getIntValue(attribute, variantValue);
                if(operator.equals("<=")) {
                    if(!(intVariantValue <= conditionValue)){
                        goodRuleCondition = false;
                        break;
                    }
                } else if(operator.equals(">=")) {
                    if(!(intVariantValue >= conditionValue)) {
                        goodRuleCondition = false;
                        break;
                    }
                } else {
                    if(!(intVariantValue == conditionValue)) {
                        goodRuleCondition = false;
                        break;
                    }
                }
            }
            if(goodRuleCondition) {
                allCoveringRules.add(rule);
            }
        }

        String upSet = "JustTotalUnknownValue";
        String downSet = "JustTotalUnknownValue";


        for(Rule<RuleDecisionElement> rule: allCoveringRules) {
            for(RuleDecisionElement decision: rule.decision) {
                if(decision.operator.equals(">=")) {
                    if(upSet.equals("JustTotalUnknownValue")){
                        upSet = decision.decisionClass;
                    } else {
                        double decisionValue = variant.getIntValue("decision",decision.decisionClass);
                        double upSetValue = variant.getIntValue("decision", upSet);
                        double maxValue = decisionValue > upSetValue ? decisionValue : upSetValue;
                        upSet = matrix.attributes.get("decision").stringValues.get(maxValue);
                    }
                } else {
                    if(downSet.equals("JustTotalUnknownValue")) {
                        downSet = decision.decisionClass;
                    } else {
                        double decisionValue = variant.getIntValue("decision",decision.decisionClass);
                        double downSetValue = variant.getIntValue("decision", downSet);
                        double minValue = decisionValue < downSetValue ? decisionValue : downSetValue;
                        downSet = matrix.attributes.get("decision").stringValues.get(minValue);
                    }
                }
            }
        }

        double intUpSet = upSet.equals("JustTotalUnknownValue") ? 0.0 : variant.getIntValue("decision",upSet);
        double intDownSet = downSet.equals("JustTotalUnknownValue") ? 0.0 : variant.getIntValue("decision",downSet);


        if(upSet.equals("JustTotalUnknownValue") && downSet.equals("JustTotalUnknownValue")) {
            for(String className: matrix.attributes.get("decision").originalValues) {
                Pair variantClassPair = new Pair(variant.name, className);
                scoresMap.put(variantClassPair, 1);
            }
        } else if(upSet.equals("JustTotalUnknownValue")) {
            for(String className: matrix.attributes.get("decision").originalValues) {
                Pair variantClassPair = new Pair(variant.name, className);
                if(variant.getIntValue("decision",className) <= intDownSet) {
                    scoresMap.put(variantClassPair, 1);
                } else {
                    scoresMap.put(variantClassPair, 0);
                }
            }
        } else if(downSet.equals("JustTotalUnknownValue")) {
            for(String className: matrix.attributes.get("decision").originalValues) {
                Pair variantClassPair = new Pair(variant.name, className);
                if(variant.getIntValue("decision",className) >= intUpSet) {
                    scoresMap.put(variantClassPair, 1);
                } else {
                    scoresMap.put(variantClassPair, 0);
                }
            }
        } else {
            double lowestClass = intDownSet < intUpSet ? intDownSet : intUpSet;
            double highestClass = intDownSet > intUpSet ? intDownSet : intUpSet;

            for(String className: matrix.attributes.get("decision").originalValues) {
                Pair variantClassPair = new Pair(variant.name, className);
                if((variant.getIntValue("decision",className) >= lowestClass)  && (variant.getIntValue("decision",className) <= highestClass)) {
                    scoresMap.put(variantClassPair, 1);
                } else {
                    scoresMap.put(variantClassPair, 0);
                }
            }
        }
        return scoresMap;
    }
}
