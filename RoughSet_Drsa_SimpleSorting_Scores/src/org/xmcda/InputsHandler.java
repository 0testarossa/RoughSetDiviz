package org.xmcda;

import org.*;

import org.xmcda.value.ValuedLabel;
import pl.poznan.put.recommendation.exceptions.InputDataException;
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
        protected Matrix matrix;

        public Matrix getMatrix() {
            return matrix;
        }

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

        public void setRulesSet(RulesSet rulesSet) { this.rulesSet = rulesSet; }
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
            checkAndExtractCategoriesRanking(inputs, xmcda, errors);
            computeCrriteriaNumericValues(inputs);
            addCriteriaToVariants(inputs);
            createMatrix(inputs);
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
            criteriaArray.add(newAttribute);
            criteriaNameArray.add(criteriaId);
            inputs.getCriteriaMap().put(criteriaId,newAttribute);
        }

        inputs.setCriteriaArray(criteriaArray);
        inputs.setCriteriaNameArray(criteriaNameArray);


        checkAndExtractCriteriaPreferencesDirection(inputs, xmcda, errors);
    }


    protected static void checkAndExtractCriteriaPreferencesDirection(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws InputDataException {
        if (xmcda.criteriaScalesList.size() != 1) {
            String errorMessage = "You need to provide one not empty criteria scales list.";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }

        CriteriaScales criteriaDirection = xmcda.criteriaScalesList.get(0);
        if(criteriaDirection.size() != inputs.getCriteriaNameArray().size()) {
            String errorMessage = "Criteria and criteria scales have different size";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }
        for (Map.Entry<Criterion, CriterionScales> criterionEntry : criteriaDirection.entrySet()) {
            if(!inputs.getCriteriaNameArray().contains(criterionEntry.getKey().id())) {
                String errorMessage = "Criterion in scales file doesn't exists";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }
            try {
                if (criterionEntry.getValue().get(0).getClass().getSimpleName().equals("QuantitativeScale")) {
                    QuantitativeScale<String> scale = (QuantitativeScale<String>) criterionEntry.getValue().get(0);
                    String scaleDirection = scale.getPreferenceDirection().name();
                    if (!"min".equalsIgnoreCase(scaleDirection) && !"max".equalsIgnoreCase((scaleDirection))) {
                        String errorMessage = "Each criterion scale must be a label \"min\" or \"max\".";
                        errors.addError(errorMessage);
                        throw new InputDataException(errorMessage);
                    }
                    inputs.getCriteriaMap().get(criterionEntry.getKey().id()).setType("numerical");
                    inputs.getCriteriaMap().get(criterionEntry.getKey().id()).setDirectionPreference(scaleDirection);
                }
                    else if (criterionEntry.getValue().get(0).getClass().getSimpleName().equals("QualitativeScale")) {
                        QualitativeScale<String> scale = (QualitativeScale<String>) criterionEntry.getValue().get(0);
                        String scaleDirection = scale.getPreferenceDirection().name();

                    if (!"min".equalsIgnoreCase(scaleDirection) && !"max".equalsIgnoreCase((scaleDirection))) {
                        String errorMessage = "Each criterion scale must be a label \"min\" or \"max\".";
                        errors.addError(errorMessage);
                        throw new InputDataException(errorMessage);
                    }

                        inputs.getCriteriaMap().get(criterionEntry.getKey().id()).setType("ordinal");
                        inputs.getCriteriaMap().get(criterionEntry.getKey().id()).setDirectionPreference(scaleDirection);

                        Map<Integer, String> scaleMap = new LinkedHashMap<Integer, String>();
                        ArrayList<Integer> rankArray = new ArrayList<Integer>();
                        ArrayList<String> scaleArray = new ArrayList<String>();

                    for(ValuedLabel scaleValues: scale) {
                        if(rankArray.contains(scaleValues.getValue().getValue())) {
                            String errorMessage = "Each criterion rank must be unique";
                            errors.addError(errorMessage);
                            throw new InputDataException(errorMessage);
                        }
                        if(scaleMap.values().contains(scaleValues.getLabel())) {
                            String errorMessage = "Each criterion rank label must be unique";
                            errors.addError(errorMessage);
                            throw new InputDataException(errorMessage);
                        }
                        scaleMap.put((Integer) scaleValues.getValue().getValue(), scaleValues.getLabel());
                        rankArray.add((Integer) scaleValues.getValue().getValue());
                    }
                    Collections.sort(rankArray);
                    if(!"max".equalsIgnoreCase((scaleDirection))) {
                        Collections.reverse(rankArray);
                    }
                    for(Integer rank: rankArray) {
                        scaleArray.add(scaleMap.get(rank));
                    }

                    inputs.getCriteriaMap().get(criterionEntry.getKey().id()).setOriginalValues(scaleArray);

                    } else {
                        String errorMessage = "Nominal scale isn't allowed here";
                        errors.addError(errorMessage);
                        throw new InputDataException(errorMessage);
                    }
                } catch(InputDataException e){
                    throw e;
                } catch(Exception e){
                    String errorMessage = "Each criterion scale must be a label \"min\" or \"max\".";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
        }
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
                    if(inputs.getCriteriaMap().get(criterion.id()).getType().equals("numerical")) {
                        if(!(profilesPerformance.getValue(alternative, criterion) instanceof Integer || profilesPerformance.getValue(alternative, criterion) instanceof Double)) {
                            String errorMessage = "Criterion type in performance table isn't compatible with scale";
                            errors.addError(errorMessage);
                            throw new InputDataException(errorMessage);
                        }
                    }

                    if(inputs.getCriteriaMap().get(criterion.id()).getType().equals("ordinal")) {
                        if(!(profilesPerformance.getValue(alternative, criterion) instanceof String)) {
                            String errorMessage = "Criterion type in performance table isn't compatible with scale";
                            errors.addError(errorMessage);
                            throw new InputDataException(errorMessage);
                        }
                    }
                    Object value = profilesPerformance.getValue(alternative, criterion);
                    inputs.getVariantsMap().get(alternative.id()).getValuesMap().put(criterion.id(), String.valueOf(value));

                    if(inputs.getCriteriaMap().get(criterion.id()).getType().equals("numerical")) {
                        if(!(inputs.getCriteriaMap().get(criterion.id()).getOriginalValues().contains(String.valueOf(value)))) {
                            inputs.getCriteriaMap().get(criterion.id()).getOriginalValues().add(String.valueOf(value));
                        }
                    }
            }

                }
                sortNumericalCriteria(inputs);
            }

    protected static void sortNumericalCriteria(Inputs inputs) {
        for (Map.Entry<String, Attribute> criterionEntry : inputs.getCriteriaMap().entrySet()) {
            if(!criterionEntry.getKey().equals("decision") && inputs.getCriteriaMap().get(criterionEntry.getKey()).getType().equals("numerical")) {
                Collections.sort(inputs.getCriteriaMap().get(criterionEntry.getKey()).getOriginalValues());
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

    protected static void checkAndExtractCategoriesRanking(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws InputDataException {
        if (xmcda.categoriesValuesList.isEmpty()) {
            String errorMessage = "No categories values list has been supplied";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }

        if (xmcda.categoriesValuesList.size() > 1) {
            String errorMessage = "More than one categories values list has been supplied";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }

        CategoriesValues categoriesValuesList = xmcda.categoriesValuesList.get(0);
        if(categoriesValuesList.size() < inputs.getCriteriaMap().get("decision").getOriginalValues().size()) {
            String errorMessage = "Category in category_values file is missing";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }
        if (!categoriesValuesList.isNumeric()) {
            String errorMessage = "Each of the categories ranks must be integer";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }

        try {
            CategoriesValues<Integer> categoriesValuesClass = categoriesValuesList.convertTo(Integer.class);
            xmcda.categoriesValuesList.set(0, categoriesValuesClass);
            Map<Integer, String> categoriesClassMap = new LinkedHashMap<Integer, String>();
            ArrayList<Integer> categoriesClassRankArray = new ArrayList<Integer>();

            for(Map.Entry<Category, LabelledQValues<Integer>> category : categoriesValuesClass.entrySet()) {
                if(categoriesClassRankArray.contains(category.getValue().get(0).getValue())) {
                    String errorMessage = "There can not be two categories with the same rank.";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
                if(!inputs.getCriteriaMap().get("decision").getOriginalValues().contains(category.getKey().id())) {
                    String errorMessage = "Category doesn't exist";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
                categoriesClassRankArray.add(category.getValue().get(0).getValue());
                categoriesClassMap.put(category.getValue().get(0).getValue(), category.getKey().id());
            }

            Collections.sort(categoriesClassRankArray);
            ArrayList<String> categoriesClassArray = new ArrayList<String>();
            for(Integer rank: categoriesClassRankArray) {
                categoriesClassArray.add(categoriesClassMap.get(rank));
            }
            inputs.getCriteriaMap().get("decision").setOriginalValues(categoriesClassArray);
        } catch (InputDataException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = "An error occurred. Remember that each rank has to be integer.";
            errors.addError(errorMessage);
            throw new InputDataException(errorMessage);
        }
    }

    protected static void computeCrriteriaNumericValues(Inputs inputs) {
        for(Map.Entry<String,Attribute> attribute: inputs.criteriaMap.entrySet()) {
            attribute.getValue().computeValues();
        }
    }

    protected static void addCriteriaToVariants(Inputs inputs) {
        for(Map.Entry<String,Variant> variant: inputs.variantsMap.entrySet()) {
            variant.getValue().setAttributesMap(inputs.getCriteriaMap());
        }
    }

    protected static void createMatrix(Inputs inputs) {
        inputs.setMatrix(new Matrix(inputs.getCriteriaMap(), inputs.getVariantsMap()));
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
                if(operator.toString().equals("leq")) {
                    conditionOperator = "<=";
                } else if(operator.toString().equals("geq")) {
                    conditionOperator = ">=";
                } else {
                    String errorMessage = "Wrong condition operator";
                    errors.addError(errorMessage);
                    throw new InputDataException(errorMessage);
                }
                QualifiedValue qualifiedValue = condition.getValue();
                Double value;
                if(!(qualifiedValue.getValue() instanceof String)) {
                    value = (Double)qualifiedValue.getValue();
                } else {
                    if(inputs.getMatrix().getAttributes().get(attribute).getIntegerValues().get(qualifiedValue.getValue().toString()) == null) {
                        String errorMessage = "The given label value doesn't exists in criteria values";
                        errors.addError(errorMessage);
                        throw new InputDataException(errorMessage);
                    }
                    value = inputs.getMatrix().getAttributes().get(attribute).getIntegerValues().get(qualifiedValue.getValue().toString());
                }
                RuleConditionElement ruleConditionElement = new RuleConditionElement(attribute, conditionOperator, value);
                actualRuleconditions.add(ruleConditionElement);
            }
            if(decisions.size() != 1) {
                String errorMessage = "You need to provide exactly one deicion in each rule";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }
            Decision decision = decisions.get(0);
            if(decision.getCategoriesInterval() == null) {
                String errorMessage = "Wrong decision type";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }
            if(decision.getCategoriesInterval().getLowerBound() == null && decision.getCategoriesInterval().getUpperBound() == null) {
                String errorMessage = "There is no Union";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }
            String decisionValue;
            String decisionOperator;
            if(decision.getCategoriesInterval().getLowerBound() != null) {
                decisionValue = decision.getCategoriesInterval().getLowerBound().id();
                decisionOperator = ">=";
            } else {
                decisionValue = decision.getCategoriesInterval().getUpperBound().id();
                decisionOperator = "<=";
            }
            if(decisionValue == null || !inputs.getCriteriaMap().get("decision").getOriginalValues().contains(decisionValue)) {
                String errorMessage = "Category in rule doesn't exists";
                errors.addError(errorMessage);
                throw new InputDataException(errorMessage);
            }
            RuleDecisionElement ruleDecisionElement = new RuleDecisionElement(decisionOperator, decisionValue);
            ArrayList<RuleDecisionElement> ruleDecisionList = new ArrayList<RuleDecisionElement>();
            ruleDecisionList.add(ruleDecisionElement);
            org.Rule newRule = new org.Rule(j, actualRuleconditions, ruleDecisionList);
            ruleList.add(newRule);
        }
        inputs.setRulesSet(new RulesSet(ruleList));
    }
}
