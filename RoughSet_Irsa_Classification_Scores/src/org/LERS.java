package org;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LERS {
    protected Matrix matrix;
    protected RulesSet rulesSet;
    protected Map<Pair, Double> scoresMap =new HashMap<Pair,  Double>();

    public LERS (Matrix matrix, RulesSet rulesSet) {
        this.matrix = matrix;
        this.rulesSet = rulesSet;
    }

    public void sort() {
        Map<Pair, Double> scoresMap =new HashMap<Pair,  Double>();
        for(Variant variant: matrix.variants.values()) {
            for(String className: matrix.attributes.get("decision").originalValues) {
                double score = computeScore(className, variant);
                Pair variantClassPair = new Pair(variant.name, className);
                scoresMap.put(variantClassPair, score);
            }
        }
        this.scoresMap = scoresMap;
    }

    public double computeScore(String className, Variant variant) {
        ArrayList<Rule> allRules = new ArrayList<Rule>(rulesSet.rulesMap.values());
        ArrayList<Rule> allGoodDecsionClassRules = new ArrayList<Rule>();

        for(Rule<RuleConditionElement> rule: allRules) {
            for(RuleDecisionElement decision:rule.decision) {
                if(decision.decisionClass.equals(className)){
                    allGoodDecsionClassRules.add(rule);
                    break;
                }
            }
        }
        ArrayList<Rule> allCoverageRules = new ArrayList<Rule>();
        if(allGoodDecsionClassRules.size()>0) {
            for(Rule<RuleConditionElement> rule: allGoodDecsionClassRules) {
                boolean goodCondition = true;
                for(RuleConditionElement condition: rule.conditions) {
                    String variantValue = variant.valuesMap.get(condition.leftSide);
                    if(!(variantValue.equals(condition.rightSide))) {
                        goodCondition = false;
                        break;
                    }
                }
                if(goodCondition) {
                     allCoverageRules.add(rule);
                }
            }
            if(allCoverageRules.size()<1) {
                ArrayList<Rule> allPartiallyCoveringRules = new ArrayList<Rule>();
                for(Rule<RuleConditionElement> rule: allGoodDecsionClassRules) {
                    for(RuleConditionElement condition: rule.conditions) {
                        String variantValue = variant.valuesMap.get(condition.leftSide);
                        if(variantValue.equals(condition.rightSide)) {
                            allPartiallyCoveringRules.add(rule);
                            break;
                        }
                    }
                }
                if(allPartiallyCoveringRules.size()<1) {
                    return 0;
                } else {
                    double actualScore = 0.0;
                    for(Rule<RuleConditionElement> rule: allPartiallyCoveringRules) {
                        int matchingPairs = 0;
                        for(RuleConditionElement condition: rule.conditions) {
                            String variantValue = variant.valuesMap.get(condition.leftSide);
                            if(variantValue.equals(condition.rightSide)) {
                                matchingPairs += 1;
                            }
                        }
                        double matchingFactor;
                        if(rule.ruleFactorsMap.get("matching") == 1.0) {
                            matchingFactor = 1.0;
                        } else {
                            matchingFactor = matchingPairs/((double)(rule.conditions.size()));
                        }
                        actualScore += rule.ruleFactorsMap.get("strength") * rule.ruleFactorsMap.get("specificity") * matchingFactor;

                    }
                    return actualScore;
                }
            } else {
                double actualScore = 0.0;
                for(Rule<RuleConditionElement> rule: allCoverageRules) {
                    actualScore += rule.ruleFactorsMap.get("strength")*rule.ruleFactorsMap.get("specificity");
                }
                return actualScore;
            }
        } else {
            return 0.0;
        }
    }
}
