package org;

import org.xmcda.OutputsHandler;

import java.util.*;

public class Main {
    public static OutputsHandler.Output main(Matrix matrix, RulesSet rulesSet) {
        Map<Pair, String> esultMap = rankingAlgorithm(matrix, rulesSet);
        Map<Pair, Integer> sClassAssignment = new LinkedHashMap<Pair, Integer>();
        Map<Pair, Integer> scClassAssignment = new LinkedHashMap<Pair, Integer>();

        for(Pair pair: esultMap.keySet()) {
            if(esultMap.get(pair).equals("S") || esultMap.get(pair).equals("SSc")) {
                sClassAssignment.put(pair,1);
            }
            if(esultMap.get(pair).equals("Sc") || esultMap.get(pair).equals("SSc")) {
                scClassAssignment.put(pair,1);
            }
        }
        OutputsHandler.Output outputHandler = new OutputsHandler.Output();
        outputHandler.setAssignmentsS(sClassAssignment);
        outputHandler.setAssignmentsSc(scClassAssignment);
        return outputHandler;
    }

    public static Map<Pair, String> rankingAlgorithm (Matrix matrix, RulesSet rulesSet2) {
        ranking ranking = new ranking(matrix, rulesSet2);
        ranking.sort();
        return ranking.relationMap;
    }
}
