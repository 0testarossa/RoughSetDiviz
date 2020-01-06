package org.xmcda;

import org.Pair;
import org.xmcda.*;
import org.xmcda.utils.Coord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputsHandler {

    private static final String PREFERENCES_S = "preferences_S";
    private static final String PREFERENCES_SC = "preferences_Sc";

    private static final String MESSAGES = "messages";

    private OutputsHandler() {

    }

    public static class Output{
        private Map<Pair, Integer> assignmentsS;
        private Map<Pair, Integer> assignmentsSc;

        public Map<Pair, Integer> getAssignmentsS() {
            return assignmentsS;
        }
        public Map<Pair, Integer> getAssignmentsSc() {
            return assignmentsSc;
        }

        public void setAssignmentsS(Map<Pair, Integer>  assignmentsS) {
            this.assignmentsS = assignmentsS;
        }
        public void setAssignmentsSc(Map<Pair, Integer>  assignmentsSc){ this.assignmentsSc = assignmentsSc; }
    }

    public static final String xmcdaV3Tag(String outputName)
    {
        switch(outputName)
        {
            case PREFERENCES_S:
            case PREFERENCES_SC:
                return "alternativesMatrix";
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
            case PREFERENCES_S:
            case PREFERENCES_SC:
                return "alternativesComparisons";
            case MESSAGES:
                return "methodMessages";
            default:
                throw new IllegalArgumentException(String.format("Unknown output name '%s'",outputName));
        }
    }

    public static Map<String, XMCDA> convert(Map<Pair, Integer> preferencesS,
            Map<Pair, Integer> preferencesSc)
    {
        final HashMap<String, XMCDA> xResults = new HashMap<>();

        XMCDA results = new XMCDA();
        XMCDA resultsSc = new XMCDA();

        AlternativesMatrix<Integer> resultS = new AlternativesMatrix<>();
        AlternativesMatrix<Integer> resultSc = new AlternativesMatrix<>();

        for (Map.Entry<Pair, Integer> sMap : preferencesS.entrySet()) {
            Integer value = sMap.getValue();
            Alternative alt1 = new Alternative(sMap.getKey().getElementLeft());
            Alternative alt2 = new Alternative(sMap.getKey().getElementRight());
            Coord<Alternative, Alternative> coord = new Coord<>(alt1, alt2);
            QualifiedValues<Integer> values = new QualifiedValues<>(new QualifiedValue<>(value));
            resultS.put(coord, values);

        }

        for (Map.Entry<Pair, Integer> scMap : preferencesSc.entrySet()) {
            Integer value = scMap.getValue();
            Alternative alt1 = new Alternative(scMap.getKey().getElementLeft());
            Alternative alt2 = new Alternative(scMap.getKey().getElementRight());
            Coord<Alternative, Alternative> coord = new Coord<>(alt1, alt2);
            QualifiedValues<Integer> values = new QualifiedValues<>(new QualifiedValue<>(value));
            resultSc.put(coord, values);
        }

        results.alternativesMatricesList.add(resultS);
        resultsSc.alternativesMatricesList.add(resultSc);

        xResults.put(PREFERENCES_S, results);
        xResults.put(PREFERENCES_SC, resultsSc);
        return xResults;
    }
}
