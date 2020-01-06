package org;

import org.xmcda.OutputsHandler;

import java.util.*;

public class Main {
    public static OutputsHandler.Output<Integer> main(Matrix matrix, RulesSet rulesSet) {
        Map<Pair, Integer> resultMap = simpleSortingAlgorithm(matrix, rulesSet);
        OutputsHandler.Output<Integer> outputsHandler = new OutputsHandler.Output<>();
        outputsHandler.setAssignments(resultMap);
        return outputsHandler;
    }

    public static Map<Pair, Integer> simpleSortingAlgorithm (Matrix matrix, RulesSet rulesSet) {
        SimpleSorting simpleSorting = new SimpleSorting(matrix, rulesSet);
        simpleSorting.sort();
        return simpleSorting.scoresMap;
    }
}
