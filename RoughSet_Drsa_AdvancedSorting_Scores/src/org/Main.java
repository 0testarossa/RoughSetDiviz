package org;

import org.xmcda.OutputsHandler;

import java.util.*;

public class Main {

    public static OutputsHandler.Output<Double> main
            (Matrix matrix, ArrayList<Variant> variantsArrayRef, RulesSet rulesSet) {
        Map<Pair, Double> resultMap = advancedSortingAlgorithm(matrix, variantsArrayRef, rulesSet);
        OutputsHandler.Output<Double> outputHandler = new OutputsHandler.Output<>();
        outputHandler.setAssignments(resultMap);
        return outputHandler;
    }

    public static Map<Pair, Double> advancedSortingAlgorithm
            (Matrix matrix, ArrayList<Variant> variantsRef, RulesSet rulesSet) {
        AdvancedSorting advancedSorting = new AdvancedSorting(matrix, rulesSet, variantsRef);
        advancedSorting.sort();
        return advancedSorting.scoresMap;
    }
}
