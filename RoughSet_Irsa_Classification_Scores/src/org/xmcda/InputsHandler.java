package org.xmcda;

import org.*;
import org.xmcda.v2.Qualitative;
import org.xmcda.value.ValuedLabel;
import pl.poznan.put.recommendation.exceptions.InputDataException;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class InputsHandler {

    private InputsHandler() {

    }

    public static class Inputs {
        protected ArrayList<Variant> variantsArray = new ArrayList<Variant>();
        protected ArrayList<Attribute> criteriaArray = new ArrayList<Attribute>();
        protected ArrayList<String> variantsNameArray = new ArrayList<String>();
        protected ArrayList<String> criteriaNameArray = new ArrayList<String>();
        protected Map<String, Variant> variantsMap = new LinkedHashMap<String, Variant>();
        protected Map<String, Attribute> criteriaMap = new LinkedHashMap<String, Attribute>();
        protected RulesSet rulesSet;
        protected Map<String, String> factorsMap = new LinkedHashMap<String, String>();
        protected Matrix matrix;

        public void setMatrix(Matrix matrix) {
            this.matrix = matrix;
        }

        public Map<String, Variant> getVariantsMap() {
            return variantsMap;
        }

        public Map<String, Attribute> getCriteriaMap() {
            return criteriaMap;
        }

        public void setVariantsArray(ArrayList<Variant> variantsArray) {
            this.variantsArray = variantsArray;
        }

        public ArrayList<Attribute> getCriteriaArray() {
            return criteriaArray;
        }

        public void setCriteriaArray(ArrayList<Attribute> criteriaArray) {
            this.criteriaArray = criteriaArray;
        }

        public void setVariantsNameArray(ArrayList<String> variantsNameArray) {
            this.variantsNameArray = variantsNameArray;
        }

        public ArrayList<String> getCriteriaNameArray() {
            return criteriaNameArray;
        }

        public void setCriteriaNameArray(ArrayList<String> criteriaNameArray) {
            this.criteriaNameArray = criteriaNameArray;
        }

        public ArrayList<String> getVariantsNameArray() {
            return variantsNameArray;
        }

        public RulesSet getRulesSet() { return rulesSet; }

        public void setRulesSet(RulesSet rulesSet) { this.rulesSet = rulesSet; }

        public Map<String, String> getFactorsMap() { return factorsMap; }

        public void setFactorsMap(Map<String, String> factorsMap) { this.factorsMap = factorsMap; }
    }

    public static Inputs checkAndExtractInputs(XMCDA xmcda, ProgramExecutionResult xmcdaExecResults) {
        Inputs inputsDict = checkInputs(xmcda, xmcdaExecResults);
        if (xmcdaExecResults.isError())
            return null;
        return inputsDict;
    }

    protected static Inputs checkInputs(XMCDA xmcda, ProgramExecutionResult errors) {
        Inputs inputs = new Inputs();

        try {
            checkAndExtractAlternatives(inputs, xmcda, errors);
            checkAndExtractCriteria(inputs, xmcda, errors);
            checkAndExtractTable(inputs, xmcda, errors);
            checkAndExtractCategories(inputs, xmcda, errors);
            addCriteriaToVariants(inputs);
            createMatrix(inputs);
            checkAndExtractParameters(inputs, xmcda, errors);
            checkAndExtractRules(inputs, xmcda, errors);

        } catch (InputDataException exception) {
            //Just catch the exceptions and skip other functions
        }

        return inputs;
    }


    protected static void checkAndExtractAlternatives(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws InputDataException {
        if (xmcda.alternatives.isEmpty()) {
            String errorMessage = "No alternatives list has been supplied";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }

        ArrayList<String> alternativesIds = new ArrayList<String>(xmcda.alternatives.getActiveAlternatives().stream().filter(a -> "alternatives".equals(a.getMarker())).map(
                Alternative::id).collect(Collectors.toList()));

        if (alternativesIds.isEmpty()) {
            String errorMessage = "The alternatives list can not be empty";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }

        ArrayList<Variant> alternativesArray = new ArrayList<Variant>();
        ArrayList<String> alternativesNameArray = new ArrayList<String>();
        for(String alternativeId: alternativesIds) {
            Variant newVariant = new Variant(alternativeId);
            alternativesArray.add(newVariant);
            alternativesNameArray.add(alternativeId);
            inputs.getVariantsMap().put(alternativeId,newVariant);

        }
        inputs.setVariantsArray(alternativesArray);
        inputs.setVariantsNameArray(alternativesNameArray);
    }

    protected static void checkAndExtractCriteria(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws InputDataException {
        if (xmcda.criteria.isEmpty()) {
            String errorMessage = "You need to provide a not empty criteria list.";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }

        ArrayList<String> criteriaIds = new ArrayList<String>((xmcda.criteria.getActiveCriteria().stream().filter(a -> "criteria".equals(a.getMarker())).map(
                Criterion::id).collect(Collectors.toList())));

        if (criteriaIds.isEmpty()) {
            String errorMessage = "The criteria list can not be empty";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }

        ArrayList<Attribute> criteriaArray = new ArrayList<Attribute>();
        ArrayList<String> criteriaNameArray = new ArrayList<String>();
        for(String criteriaId: criteriaIds) {
            Attribute newAttribute = new Attribute(criteriaId);
            newAttribute.setType("nominal");
            criteriaArray.add(newAttribute);
            criteriaNameArray.add(criteriaId);
            inputs.getCriteriaMap().put(criteriaId,newAttribute);
        }

        inputs.setCriteriaArray(criteriaArray);
        inputs.setCriteriaNameArray(criteriaNameArray);
    }


    protected static void checkAndExtractTable(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws InputDataException {
        if (xmcda.performanceTablesList.size() != 1) {
            String errorMessage = "There should be only one performance table";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }

            if (xmcda.performanceTablesList.get(0).hasMissingValues()) {
                String errorMessage = "The performance table has missing values.";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }

            @SuppressWarnings("unchecked")
            PerformanceTable profilesPerformance = xmcda.performanceTablesList.get(0);
            ArrayList<Alternative> alternativesArray = new ArrayList<Alternative>(profilesPerformance.getAlternatives());
            ArrayList<Criterion> criteriaArray = new ArrayList<Criterion>(profilesPerformance.getCriteria());
            if(inputs.getVariantsNameArray().size() != alternativesArray.size()) {
                String errorMessage = "Number of alternarives in performance table file doesn't match";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }
            ArrayList<String> readAlternativesNames = new ArrayList<>();
            for (Alternative alternative : alternativesArray) {
                if(!inputs.getVariantsNameArray().contains(alternative.id())) {
                    String errorMessage = "Alternative in performance table doesn't exists";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
                if(readAlternativesNames.contains(alternative.id())) {
                    String errorMessage = "Found two the same alternatives in performance table";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
                readAlternativesNames.add(alternative.id());
                if(inputs.getCriteriaNameArray().size() != criteriaArray.size()) {
                    String errorMessage = "Number of criterions in performance table file doesn't match";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
                ArrayList<String> readCriteriaNames = new ArrayList<>();
                for (Criterion criterion : criteriaArray) {


                    if(!inputs.getCriteriaNameArray().contains(criterion.id())) {
                        String errorMessage = "Criterion in performance table doesn't exists";
                        errors.addError(errorMessage);
                        throw new InputDataException(errorMessage);
                    }
                    if(readCriteriaNames.contains(criterion.id())) {
                        String errorMessage = "Found two the same criteria in performance table ";
                        errors.addError(errorMessage);
                        throw new InputDataException(errorMessage);
                    }
                    readCriteriaNames.add(criterion.id());

                        if(!(profilesPerformance.getValue(alternative, criterion) instanceof String)) {
                            String errorMessage = "Criterion type in performance table isn't compatible with nominal scale";
                            errors.addError(errorMessage);
                            throw new InputDataException(errorMessage);
                        }

                    Object value = profilesPerformance.getValue(alternative, criterion);

                        if(!(inputs.getCriteriaMap().get(criterion.id()).getOriginalValues().contains(String.valueOf(value)))) {
                            inputs.getCriteriaMap().get(criterion.id()).getOriginalValues().add(String.valueOf(value));
                        }
                    inputs.getVariantsMap().get(alternative.id()).getValuesMap().put(criterion.id(), String.valueOf(value));
            }
                }
            }

    protected static void checkAndExtractCategories(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws InputDataException {
        if (xmcda.categories.isEmpty()) {
            String errorMessage = "No categories has been supplied.";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }

        if (xmcda.categories.size() == 1) {
            String errorMessage = "You should supply at least 2 categories.";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }

        ArrayList<String> categories = new ArrayList<String>(xmcda.categories.getActiveCategories().stream().filter(a -> "categories".equals(a.getMarker())).map(
                Category::id).collect(Collectors.toList()));

        if (categories.isEmpty()) {
            String errorMessage = "The category list can not be empty.";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }

        Attribute decisionAttribute = new Attribute("decision");
        decisionAttribute.setType("ordinal");

        for(String category: categories) {
            decisionAttribute.getOriginalValues().add(category);
        }

        inputs.getCriteriaNameArray().add("decision");
        inputs.getCriteriaArray().add(decisionAttribute);
        inputs.getCriteriaMap().put("decision", decisionAttribute);
    }

    protected static void addCriteriaToVariants(Inputs inputs) {
        for(Map.Entry<String,Variant> variant: inputs.variantsMap.entrySet()) {
            variant.getValue().setAttributesMap(inputs.getCriteriaMap());
        }
    }

    protected static void createMatrix(Inputs inputs) {
        inputs.setMatrix(new Matrix(inputs.getCriteriaMap(), inputs.getVariantsMap()));
    }

    protected static void checkAndExtractParameters(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws InputDataException {

        if (!xmcda.programParametersList.isEmpty()) {

            if (xmcda.programParametersList.size() > 1) {
                String errorMessage = "Only one programParameter list is expected";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }

            if (xmcda.programParametersList.get(0).size() > 3) {
                String errorMessage = "Parameter's list mustn't contain more than three elements";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }

            if (xmcda.programParametersList.get(0).size() < 1) {
                String errorMessage = "Parameter's list must contain at least one element";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }

            checkAndExtractCutPoint(inputs, xmcda, errors);
        }
    }

    protected static void checkAndExtractCutPoint(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws InputDataException {
        String factor;
        Map<String, String> factorsMap = new LinkedHashMap<String, String>();

        for(int i=0;i<xmcda.programParametersList.get(0).size();i++) {
            if(!(xmcda.programParametersList.get(0).get(i).id().equals("specificity") ||
                    xmcda.programParametersList.get(0).get(i).id().equals("strength") ||
                    xmcda.programParametersList.get(0).get(i).id().equals("matching"))) {
                String errorMessage = String.format("Invalid parameter id '%s'", xmcda.programParametersList.get(0).get(i).id());
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }

            final ProgramParameter<?> prgParam = xmcda.programParametersList.get(0).get(i);

            if (prgParam.getValues() == null || (prgParam.getValues() != null && prgParam.getValues().size() != 1)) {
                String errorMessage = String.format("Parameter '%s' must have a single value only", prgParam.id());
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }

            try {
                if(!((prgParam.getValues().get(0).getValue() instanceof String) ||
                        (prgParam.getValues().get(0).getValue() instanceof Double) ||
                        (prgParam.getValues().get(0).getValue() instanceof Integer))) {
                    String errorMessage = "Invalid value type for parameter, it must be a label, real or integer type";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
                factor = String.valueOf(prgParam.getValues().get(0).getValue());
                if(prgParam.id().equals("specificity")) {
                    if(factorsMap.get("specificity") != null) {
                        String errorMessage = "Parameter specificity has already been defined";
                        errors.addError(errorMessage);
                        throw new InputDataException(errorMessage);
                    }
                    if(factor.equals("default")) {
                        factorsMap.put("specificity","default");
                    } else if(factor.equals("1") || factor.equals("1.0")) {
                        factorsMap.put("specificity","1");

                    } else {
                        String errorMessage = "Invalid value for parameter specificity, it must be either \"default\" or \"1\" or \"1.0\"";
                        errors.addError(errorMessage);
                        throw new InputDataException(errorMessage);
                    }
                } else if(prgParam.id().equals("strength")){
                    if(factorsMap.get("strength") != null) {
                        String errorMessage = "Parameter strength has already been defined";
                        errors.addError(errorMessage);
                        throw new InputDataException(errorMessage);
                    }
                    if(factor.equals("default")) {
                        factorsMap.put("strength","default");
                    } else if(factor.equals("roughMeasure")) {
                        factorsMap.put("strength","roughMeasure");
                    } else {
                        String errorMessage = "Invalid value for parameter strengthFactor, it must be either \"default\" or \"roughMeasure\"";
                        errors.addError(errorMessage);
                        throw new InputDataException(errorMessage);
                    }
                } else {
                    if(factorsMap.get("matching") != null) {
                        String errorMessage = "Parameter matching has already been defined";
                        errors.addError(errorMessage);
                        throw new InputDataException(errorMessage);
                    }
                    if(factor.equals("default")) {
                        factorsMap.put("matching","default");
                    } else if(factor.equals("1") || factor.equals("1.0")) {
                        factorsMap.put("matching","1");
                    } else {
                        String errorMessage = "Invalid value for parameter matching, it must be either \"default\" or \"1\" or \"1.0\"";
                        errors.addError(errorMessage);
                        throw new InputDataException(errorMessage);
                    }
                }

            } catch (InputDataException e) {
                throw e;
            } catch (Exception exception) {
                String errorMessage = "Invalid parameter value, it must be text value.";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }
        }
        inputs.setFactorsMap(factorsMap);
    }

    protected static void checkAndExtractRules(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws InputDataException {
        Rules rules = xmcda.rules;
        int j=-1;
        ArrayList<org.Rule> ruleList = new ArrayList<org.Rule>();
        for(Rule rule: rules) {
            j++;
            ArrayList<RuleConditionElement> actualRuleconditions = new ArrayList<RuleConditionElement>();
            Conditions conditions = rule.getConditions();
            Decisions decisions = rule.getDecisions();
            ArrayList<String> readRuleConditions = new ArrayList<String>();
            if(conditions.getConditionsPairs().size() != 0) {
                String errorMessage = "Condition type can't be equal to conditionPair";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }
            for(Condition condition: conditions.getConditions()) {
                if(!(condition.mcdaConcept().equals("value"))) {
                    String errorMessage = "Condition should have mcdaConcept equal to 'value' value";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
                String attribute = condition.getCriterionID();
                if(inputs.getCriteriaMap().get(attribute) == null) {
                    String errorMessage = "CriterionID in condition doesn't exists";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
                if(readRuleConditions.contains(attribute)) {
                    String errorMessage = "Condition has already existed in that rule";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
                readRuleConditions.add(attribute);
                String operator = condition.getOperator();
                String conditionOperator;
                if(operator.toString().equals("eq")) {
                    conditionOperator = "==";
                } else {
                    String errorMessage = "Wrong condition operator";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
                QualifiedValue qualifiedValue = condition.getValue();
                String value = qualifiedValue.getValue().toString();
                RuleConditionElement ruleConditionElement = new RuleConditionElement(attribute, conditionOperator, value);
                actualRuleconditions.add(ruleConditionElement);
            }
            Decision decision = decisions.get(0);
            if(decision.getCategoryID() == null) {
                String errorMessage = "Wrong decision type";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }

            String decisionValue = decision.getCategoryID();
            String decisionOperator = "==";

            if(decisionValue == null || !inputs.getCriteriaMap().get("decision").getOriginalValues().contains(decisionValue)) {
                String errorMessage = "Category in rule doesn't exists";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }
            RuleDecisionElement ruleDecisionElement = new RuleDecisionElement(decisionOperator, decisionValue);
            ArrayList<RuleDecisionElement> ruleDecisionList = new ArrayList<RuleDecisionElement>();
            ruleDecisionList.add(ruleDecisionElement);

            Map<String, Double> ruleFactor = new HashMap<String,Double>();
            for(Object value: rule.getValues()) {
                QualifiedValue qualifiedValue = (QualifiedValue)value;
                String ruleParameter = qualifiedValue.id();
                Double parameterValue;
                if(qualifiedValue.getValue() instanceof Integer) {
                    parameterValue = new Double((Integer)qualifiedValue.getValue());
                } else if(qualifiedValue.getValue() instanceof  Double) {
                    parameterValue = (Double)qualifiedValue.getValue();
                } else {
                    String errorMessage = "Wrong parameter value type";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }

                if(ruleParameter.equals("specificity")) {
                    String specificityFactorFromMap = inputs.getFactorsMap().get("specificity");
                    if(specificityFactorFromMap != null) {
                        if(specificityFactorFromMap.equals("default")) {
                            ruleFactor.put("specificity",parameterValue);
                        } else {
                            ruleFactor.put("specificity",1.0);
                        }
                    } else {
                        ruleFactor.put("specificity",parameterValue);
                    }
                } else if(ruleParameter.equals("strength")) {
                        ruleFactor.put("strength",parameterValue);
                } else if(ruleParameter.equals("coverageCardinality")) {
                    ruleFactor.put("matching", parameterValue);
                } else {
                    String errorMessage = "Wrong rule parameter name";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
            }

            if(inputs.getFactorsMap().get("strength") != null && inputs.getFactorsMap().get("strength").equals("roughMeasure")) {
                double strengthFactor = ruleFactor.get("strength");
                double roughMeasure = ruleFactor.get("matching");
                if(roughMeasure == 0) {
                    ruleFactor.put("strength",0.0);
                } else {
                    ruleFactor.put("strength",(strengthFactor/roughMeasure));
                }
            }
            if(inputs.getFactorsMap().get("matching") != null && inputs.getFactorsMap().get("matching").equals("1")) {
                ruleFactor.put("matching", 1.0);
            } else {
                ruleFactor.put("matching",0.0);
            }
            org.Rule newRule = new org.Rule(j, actualRuleconditions, ruleDecisionList, ruleFactor);
            ruleList.add(newRule);

        }
        inputs.setRulesSet(new RulesSet(ruleList));
    }
}
