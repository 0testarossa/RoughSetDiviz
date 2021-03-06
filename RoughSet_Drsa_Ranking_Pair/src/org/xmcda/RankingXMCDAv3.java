package org.xmcda;
import org.Main;

import java.io.File;
import java.util.Map;

public class RankingXMCDAv3 {

    private RankingXMCDAv3() {

    }

    public static void main(String[] args) throws Utils.InvalidCommandLineException {
        final Utils.Arguments params = Utils.parseCmdLineArguments(args);

        final String indir = params.inputDirectory;
        final String outdir = params.outputDirectory;

        final File prgExecResults = new File(outdir, "messages.xml");

        final ProgramExecutionResult executionResult = new ProgramExecutionResult();

        final XMCDA xmcda = new XMCDA();

        Referenceable.DefaultCreationObserver.currentMarker="alternatives";
        Utils.loadXMCDAv3(xmcda, new File(indir, "alternatives.xml"), true, executionResult, "alternatives");
        Referenceable.DefaultCreationObserver.currentMarker="criteria";
        Utils.loadXMCDAv3(xmcda, new File(indir, "criteria.xml"), true, executionResult, "criteria");
        Referenceable.DefaultCreationObserver.currentMarker="criteria_scales";
        Utils.loadXMCDAv3(xmcda, new File(indir, "criteria_scales.xml"), true, executionResult, "criteriaScales");
        Referenceable.DefaultCreationObserver.currentMarker="performanceTable";
        Utils.loadXMCDAv3(xmcda, new File(indir, "performance_table.xml"), true, executionResult, "performanceTable");
        Referenceable.DefaultCreationObserver.currentMarker="categories";
        Utils.loadXMCDAv3(xmcda, new File(indir, "categories.xml"), true, executionResult, "categories");
        Referenceable.DefaultCreationObserver.currentMarker="rules";
        Utils.loadXMCDAv3(xmcda, new File(indir, "rules.xml"), true, executionResult, "rules");


        if ( ! (executionResult.isOk() || executionResult.isWarning() ) ) {
            Utils.writeProgramExecutionResultsAndExit(prgExecResults, executionResult, Utils.XMCDA_VERSION.v3);
        }

        final InputsHandler.Inputs inputs = InputsHandler.checkAndExtractInputs(xmcda, executionResult);

        if ( ! ( executionResult.isOk() || executionResult.isWarning() ) || inputs == null ) {
            Utils.writeProgramExecutionResultsAndExit(prgExecResults, executionResult, Utils.XMCDA_VERSION.v3);
        }

        final OutputsHandler.Output results;
        try
        {
            results = Main.main(inputs.matrix, inputs.getRulesSet());
        }
        catch (Exception e) {
            executionResult.addError(Utils.getMessage("The calculation could not be performed, reason: ", e));
            Utils.writeProgramExecutionResultsAndExit(prgExecResults, executionResult, Utils.XMCDA_VERSION.v3);
            return;
        }

        //convert results
        final Map<String, XMCDA> xResults = OutputsHandler.convert(results.getAssignmentsS(), results.getAssignmentsSc());

        //write results
        final org.xmcda.parsers.xml.xmcda_v3.XMCDAParser parser = new org.xmcda.parsers.xml.xmcda_v3.XMCDAParser();

        for ( Map.Entry<String, XMCDA> keyEntry : xResults.entrySet() )
        {
            File outputFile = new File(outdir, String.format("%s.xml", keyEntry.getKey()));
            try
            {
                parser.writeXMCDA(keyEntry.getValue(), outputFile, OutputsHandler.xmcdaV3Tag(keyEntry.getKey()));
            }
            catch (Exception exception)
            {
                final String err = String.format("Error while writing %s.xml, reason: ", keyEntry.getKey());
                executionResult.addError(Utils.getMessage(err, exception));
                // Whatever the error is, clean up the file: we do not want to leave an empty or partially-written file
                outputFile.delete();
            }
        }

        Utils.writeProgramExecutionResultsAndExit(prgExecResults, executionResult, Utils.XMCDA_VERSION.v3);
    }
}