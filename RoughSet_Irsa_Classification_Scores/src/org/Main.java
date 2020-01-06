package org;

import org.xmcda.OutputsHandler;

import java.util.*;

public class Main {

    public static OutputsHandler.Output<Double> main(Matrix matrix, RulesSet rulesSet) {
        Map<Pair, Double> resultMap = sortingAlgorithm(matrix, rulesSet);
        OutputsHandler.Output<Double> outputHandler = new OutputsHandler.Output<>();
        outputHandler.setAssignments(resultMap);
        return outputHandler;
    }

    public static Map<Pair, Double> sortingAlgorithm (Matrix matrix, RulesSet rulesSet) {
        LERS lers = new LERS(matrix, rulesSet);
        lers.sort();
        return lers.scoresMap;
    }
}
