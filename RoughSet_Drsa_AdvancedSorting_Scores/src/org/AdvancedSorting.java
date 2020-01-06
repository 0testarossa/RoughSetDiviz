package org;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdvancedSorting {
    protected Matrix matrix;
    protected RulesSet rulesSet;
    protected Map<Pair, Double> scoresMap =new HashMap<Pair,  Double>();
    protected ArrayList<Variant> alternativesRef;

    public AdvancedSorting(Matrix matrix, RulesSet rulesSet, ArrayList<Variant> alternativesRef) {
        this.matrix = matrix;
        this.rulesSet = rulesSet;
        this.alternativesRef = alternativesRef;
    }

    public void sort() {
        Map<Pair, Double> scoresMap =new HashMap<Pair,  Double>();
        for(Variant variant: alternativesRef) {
            for(String className: matrix.attributes.get("decision").originalValues) {
                double score = computeScore(className, variant);
                Pair variantClassPair = new Pair(variant.name, className);
                scoresMap.put(variantClassPair, score);
            }
        }
        this.scoresMap = scoresMap;
    }

    public double computeScore(String className, Variant selectedVariant) {
        ArrayList<Variant>allVariantsHavingDecisionClassGood = new ArrayList<Variant>();
        ArrayList<Variant>allVariantsHavingDecisionClassNotGood = new ArrayList<Variant>();

        ArrayList<Variant> allVariantsValues = new ArrayList<Variant>(matrix.variants.values());
        for(Variant variant: allVariantsValues) {
            String decisionClass = variant.valuesMap.get("decision");
            if(decisionClass.equals(className)){
                allVariantsHavingDecisionClassGood.add(variant);
            } else {
                allVariantsHavingDecisionClassNotGood.add(variant);
            }
        }

        Variant newVariant = selectedVariant;

        ArrayList<Variant> allClassGoodVariants = new ArrayList<Variant>();
        ArrayList<Variant> allNotClassGoodVariants = new ArrayList<Variant>();
        ArrayList<Variant> allVariants = new ArrayList<Variant>(matrix.variants.values());
        ArrayList<Rule> allRules = new ArrayList<Rule>(rulesSet.rulesMap.values());
        for(Variant variant: allVariants) {
            String decisionClass = variant.valuesMap.get("decision");
            if(decisionClass.equals(className)) {
                allClassGoodVariants.add(variant);
            } else {
                allNotClassGoodVariants.add(variant);
            }
        }

        ArrayList<Rule> allCoverageRules = new ArrayList<Rule>();
        for(Rule<RuleConditionElement> rule: allRules) {
            boolean goodRuleCondition = true;
            for(RuleConditionElement condition: rule.conditions) {
                String attribute = condition.leftSide;
                String operator = condition.operator;
                double conditionValue = condition.rightSide;

                String variantValue = newVariant.valuesMap.get(attribute);
                double intVariantValue = newVariant.getIntValue(attribute, variantValue);

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
                allCoverageRules.add(rule);
            }
        }

        ArrayList<Rule> allCoverageRulesMatchingGoodClass = new ArrayList<Rule>();
        ArrayList<Rule> allCoverageRulesNotMatchingGoodClass = new ArrayList<Rule>();

        for(Rule<RuleConditionElement> rule: allCoverageRules) {
            for(RuleDecisionElement decision: rule.decision) {
                double intDecision = matrix.attributes.get("decision").integerValues.get(decision.decisionClass);
                if(decision.operator.equals(">=")) {
                    if(matrix.attributes.get("decision").integerValues.get(className) >= intDecision) {
                        allCoverageRulesMatchingGoodClass.add(rule);
                    } else {
                        allCoverageRulesNotMatchingGoodClass.add(rule);
                    }
                } else {
                    if(matrix.attributes.get("decision").integerValues.get(className) <= intDecision) {
                        allCoverageRulesMatchingGoodClass.add(rule);
                    } else {
                        allCoverageRulesNotMatchingGoodClass.add(rule);
                    }
                }
            }
        }

        Map<Integer, ArrayList<Variant>> coverageMap =new HashMap<Integer,  ArrayList<Variant>>();
        for(Rule<RuleConditionElement> rule:allCoverageRules) {
            ArrayList<Variant> variants = new ArrayList<Variant>(matrix.variants.values());
            for(RuleConditionElement condition: rule.conditions) {
                String attribute = condition.leftSide;
                String operator = condition.operator;
                double conditionValue = condition.rightSide;

                ArrayList<Variant> variantsToRemove = new ArrayList<Variant>();
                for(Variant actualVariant: variants ) {
                    String variantValue = actualVariant.valuesMap.get(attribute);
                    double intVariantValue = actualVariant.getIntValue(attribute, variantValue);
                    if(operator.equals("<=")) {
                        if(!(intVariantValue <= conditionValue)){
                            variantsToRemove.add(actualVariant);
                        }
                    } else if(operator.equals(">=")) {
                        if(!(intVariantValue >= conditionValue)) {
                            variantsToRemove.add(actualVariant);
                        }
                    } else {
                        if(!(intVariantValue == conditionValue)) {
                            variantsToRemove.add(actualVariant);
                        }
                    }
                }
                for(Variant variantToRemove: variantsToRemove) {
                    variants.remove(variantToRemove);
                }
            }
            coverageMap.put(rule.number, variants);
        }

        ArrayList<String> intAllVariantsHavingDecisionClassGood = new ArrayList<String>();
        for(Variant variant: allVariantsHavingDecisionClassGood) {
            intAllVariantsHavingDecisionClassGood.add(variant.name);
        }
        ArrayList<String> intRestVariants = new ArrayList<String>();
        for(Rule rule: allCoverageRulesMatchingGoodClass) {
            for(Variant variant: coverageMap.get(rule.number)) {
                if(!intRestVariants.contains(variant.name)) {
                    intRestVariants.add(variant.name);
                }
            }
        }
        double nominatorCounter = 0;
        if(intRestVariants.size() > intAllVariantsHavingDecisionClassGood.size()) {
            for(String object: intRestVariants) {
                if(intAllVariantsHavingDecisionClassGood.contains(object)) {
                    nominatorCounter += 1;
                }
            }
        } else {
            for(String object: intAllVariantsHavingDecisionClassGood) {
                if(intRestVariants.contains(object)) {
                    nominatorCounter += 1;
                }
            }
        }

        nominatorCounter = nominatorCounter * nominatorCounter;

        double denominatorCounter = intAllVariantsHavingDecisionClassGood.size() * intRestVariants.size();

        double positiveScore = 0.0;
        if(nominatorCounter != 0.0 && denominatorCounter != 0.0) {
            positiveScore = nominatorCounter/denominatorCounter;
        }

        ArrayList<String> intAllVariantsHavingDecisionClassNotGood = new ArrayList<String>();
        for(Variant variant: allVariantsHavingDecisionClassNotGood) {
            intAllVariantsHavingDecisionClassNotGood.add(variant.name);
        }
        ArrayList<String> intRest2Variants = new ArrayList<String>();
        for(Rule rule: allCoverageRulesNotMatchingGoodClass ) {
            for(Variant variant: coverageMap.get(rule.number)) {
                if(!intRest2Variants.contains(variant.name)) {
                    intRest2Variants.add(variant.name);
                }
            }
        }
        double negativeNominatorCounter = 0;
        if(intRest2Variants.size() > intAllVariantsHavingDecisionClassNotGood.size()) {
            for(String object: intRest2Variants) {
                if(intAllVariantsHavingDecisionClassNotGood.contains(object)) {
                    negativeNominatorCounter += 1;
                }
            }
        } else {
            for(String object: intAllVariantsHavingDecisionClassNotGood) {
                if(intRest2Variants.contains(object)) {
                    negativeNominatorCounter += 1;
                }
            }
        }

        negativeNominatorCounter = negativeNominatorCounter * negativeNominatorCounter;

        double negativeDenominatorCounter = intAllVariantsHavingDecisionClassNotGood.size() * intRest2Variants.size();

        double negativeScore = 0.0;
        if(negativeNominatorCounter != 0.0 && negativeDenominatorCounter != 0.0) {
            negativeScore = negativeNominatorCounter / negativeDenominatorCounter;
        }

        return positiveScore - negativeScore;
    }
}
