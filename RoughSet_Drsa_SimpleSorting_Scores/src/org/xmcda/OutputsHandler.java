package org.xmcda;

import org.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputsHandler {

    private static final String ASSIGNMENTS = "assignments";
    private static final String MESSAGES = "messages";

    private OutputsHandler() {

    }

    public static class Output<Type>{
        private Map<Pair, Type> assignments;
        public Map<Pair, Type> getAssignments() {
            return assignments;
        }
        public void setAssignments(Map<Pair, Type> assignments) {
            this.assignments = assignments;
        }
    }

    public static final String xmcdaV3Tag(String outputName)
    {
        switch(outputName)
        {
            case ASSIGNMENTS:
                return "alternativesAssignments";
            case MESSAGES:
                return "programExecutionResult";
            default:
                throw new IllegalArgumentException(String.format("Unknown output name '%s'",outputName));
        }
    }

    public static final String xmcdaV2Tag(String outputName)
    {
        switch(outputName)
        {
            case ASSIGNMENTS:
                return "alternativesAffectations";
            case MESSAGES:
                return "methodMessages";
            default:
                throw new IllegalArgumentException(String.format("Unknown output name '%s'",outputName));
        }
    }

    public static Map<String, XMCDA> convert(Map<String, List<String>> assignments)
    {
        final HashMap<String, XMCDA> xResults = new HashMap<>();

        XMCDA assignmentsXmcdaObject = new XMCDA();
        AlternativesAssignments alternativesAssignments = new AlternativesAssignments();

        for (Map.Entry<String, List<String>> alternativeEntry : assignments.entrySet()) {
            for(String categoryName : alternativeEntry.getValue()){
                AlternativeAssignment tmpAssignment = new AlternativeAssignment();
                tmpAssignment.setAlternative(new Alternative(alternativeEntry.getKey()));
                Category category = new Category(categoryName);
                tmpAssignment.setCategory(category);
                alternativesAssignments.add(tmpAssignment);
            }
        }

        assignmentsXmcdaObject.alternativesAssignmentsList.add(alternativesAssignments);
        xResults.put(ASSIGNMENTS, assignmentsXmcdaObject);

        return xResults;
    }

    public static Map<String, XMCDA> convertSortInputExamples(Map<Pair, Integer> alternativesSupport)
    {
        final HashMap<String, XMCDA> xResults = new HashMap<>();

        XMCDA assignmentsXmcdaObject = new XMCDA();
        AlternativesAssignments alternativeAssignments = new AlternativesAssignments();

        for (Map.Entry<Pair, Integer> alternativeEntry : alternativesSupport.entrySet()) {
            Category category = new Category(alternativeEntry.getKey().getElementRight());
            AlternativeAssignment<Integer> tmpAssignment = new AlternativeAssignment<>();

            QualifiedValue<Integer> value = new QualifiedValue<>();
            QualifiedValues<Integer> valuesSet = new QualifiedValues<>();

            value.setValue(alternativeEntry.getValue());
            valuesSet.add(value);
            tmpAssignment.setAlternative(new Alternative(alternativeEntry.getKey().getElementLeft()));
            tmpAssignment.setValues(valuesSet);
            tmpAssignment.setCategory(category);
            alternativeAssignments.add(tmpAssignment);
        }

        assignmentsXmcdaObject.alternativesAssignmentsList.add(alternativeAssignments);

        xResults.put(ASSIGNMENTS, assignmentsXmcdaObject);

        return xResults;
    }
}
