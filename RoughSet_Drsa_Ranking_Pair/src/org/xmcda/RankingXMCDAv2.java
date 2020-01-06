package org.xmcda;

import org.Main;
import org.xmcda.converters.v2_v3.XMCDAConverter;
import org.xmcda.parsers.xml.xmcda_v2.XMCDAParser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class RankingXMCDAv2 {
    private static final ProgramExecutionResult executionResult = new ProgramExecutionResult();

    /**
     * Loads, converts and inserts the content of the XMCDA v2 {@code file} into {@code xmcdaV3}.
     * Updates {@link #executionResult} if an error occurs.
     *
     * @param file         the XMCDA v2 file to be loaded
     * @param marker       the marker to use, see {@link Referenceable.DefaultCreationObserver#currentMarker}
     * @param xmcdaV3     the object into which the content of {@file} is inserted
     * @param v2TagsOnly the list of XMCDA v2 tags to be loaded
     */
    private static void convertToV3AndMark(File file, String marker, org.xmcda.XMCDA xmcdaV3,
                                           String... v2TagsOnly) {
        final org.xmcda.v2.XMCDA xmcdaV2 = new org.xmcda.v2.XMCDA();
        Referenceable.DefaultCreationObserver.currentMarker = marker;
        Utils.loadXMCDAv2(xmcdaV2, file, true, executionResult, v2TagsOnly);
        try {
            XMCDAConverter.convertTo_v3(xmcdaV2, xmcdaV3);
        } catch (Exception e) {
            executionResult.addError(Utils.getMessage("Could not convert " + file.getPath() + " to XMCDA v3, reason: ", e));
        }
    }

    private static void readFiles(XMCDA xmcda, String indir) {
        convertToV3AndMark(new File(indir, "alternatives.xml"), "alternatives", xmcda, "alternatives");
        convertToV3AndMark(new File(indir, "criteria.xml"), "criteria", xmcda, "criteria");
        convertToV3AndMark(new File(indir, "criteria.xml"), "criteria_scales", xmcda, "criteria_scales");
        convertToV3AndMark(new File(indir, "performance_table.xml"), "performanceTable", xmcda, "performanceTable");
        convertToV3AndMark(new File(indir,  "categories.xml"), "categories", xmcda,"categories");
        convertToV3AndMark(new File(indir,  "rules.xml"), "rules", xmcda,"rules");

    }

    private static void handleResults(String outdir, Map<String, XMCDA> xResults) {
        org.xmcda.v2.XMCDA resultsV2;
        for (Map.Entry<String, XMCDA> outputNameEntry : xResults.entrySet()) {
            File outputFile = new File(outdir, String.format("%s.xml", outputNameEntry.getKey()));
            try {
                resultsV2 = XMCDAConverter.convertTo_v2(outputNameEntry.getValue());
                if (resultsV2 == null)
                    throw new IllegalStateException("Conversion from v3 to v2 returned a null value");
            } catch (Exception e) {
                final String err = String.format("Could not convert %s into XMCDA_v2, reason: ", outputNameEntry.getKey());
                executionResult.addError(Utils.getMessage(err, e));
                continue;
            }
            try {
                XMCDAParser.writeXMCDA(resultsV2, outputFile, OutputsHandler.xmcdaV2Tag(outputNameEntry.getKey()));
            } catch (Exception e) {
                final String err = String.format("Error while writing %s.xml, reason: ", outputNameEntry.getKey());
                executionResult.addError(Utils.getMessage(err, e));
                outputFile.delete();
            }
        }
    }

    public static void main(String[] args) throws Utils.InvalidCommandLineException {
        final Utils.Arguments params = Utils.parseCmdLineArguments(args);
        final String indir = params.inputDirectory;
        final String outdir = params.outputDirectory;
        final File prgExecResultsFile = new File(outdir, "messages.xml");

        final org.xmcda.XMCDA xmcda = new org.xmcda.XMCDA();

          readFiles(xmcda, indir);

          if (!(executionResult.isOk() || executionResult.isWarning())) {
              System.out.println(executionResult.isError());
             Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
          }


          final InputsHandler.Inputs inputs = InputsHandler.checkAndExtractInputs(xmcda, executionResult);
        if (!(executionResult.isOk() || executionResult.isWarning()) || inputs == null) {
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
        }

        final OutputsHandler.Output results;
        try {
            results = Main.main(inputs.matrix, inputs.getRulesSet());
        } catch (Exception e) {
            executionResult.addError(Utils.getMessage("The calculation could not be performed, reason: ", e));
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
            return;
        }

        final Map<String, XMCDA> xResults = OutputsHandler.convert(results.getAssignmentsS(), results.getAssignmentsSc());
        handleResults(outdir, xResults);

        try {
            checkFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!executionResult.isError()) {
            executionResult.addDebug("Success");
        }
        Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
    }

    public static void checkFiles() throws IOException
    {
        BufferedReader reader1 = new BufferedReader(new FileReader("./././tests/out1.v3/preferences_S.xml"));

        BufferedReader reader2 = new BufferedReader(new FileReader("./././tests/out1.v2/preferences_S.xml"));

        String line1 = reader1.readLine();

        String line2 = reader2.readLine();
        reader2.close();

        while(line1 != null) {

            if(line1.contains("<row>")) {
                line1 = reader1.readLine();
                String actualAlternative = line1.replace("<alternativeID>","");
                actualAlternative = actualAlternative.replace("</alternativeID>","");
                actualAlternative = actualAlternative.replaceAll("\\s+","");
                while(!line1.contains("/row")) {
                    if(line1.contains("<column>")) {
                        line1 = reader1.readLine();
                        String line1Replaced = line1.replace("<alternativeID>","");
                        line1Replaced = line1Replaced.replace("</alternativeID>","");
                        line1 = reader1.readLine();
                        line1 = reader1.readLine();
                        line1 = reader1.readLine();
                        String line1Replaced2 = actualAlternative;
                        line1Replaced2 = line1Replaced2.replaceAll("\\s+","");
                        line1Replaced = line1Replaced.replaceAll("\\s+","");

                        reader2 = new BufferedReader(new FileReader("./././tests/out1.v2/preferences_S.xml"));
                        String line2Replaced = "";
                        String line2Replaced2 = "";
                        while(!(line2Replaced.equalsIgnoreCase(line1Replaced2) && line2Replaced2.equalsIgnoreCase(line1Replaced)) && !line2.contains("</xmcda:XMCDA>")) {
                            line2 = reader2.readLine();;
                            if(line2.contains("<initial>")) {
                                line2 = reader2.readLine();
                                line2Replaced = line2.replace("<alternativeID>","");
                                line2Replaced = line2Replaced.replace("</alternativeID>","");
                                line2 = reader2.readLine();
                                line2 = reader2.readLine();
                                line2 = reader2.readLine();
                                line2Replaced2 = line2.replace("<alternativeID>","");
                                line2Replaced2 = line2Replaced2.replace("</alternativeID>","");

                                line2Replaced = line2Replaced.replaceAll("\\s+","");
                                line2Replaced2 = line2Replaced2.replaceAll("\\s+","");
                            }
                        }
                        if(line2.contains("</xmcda:XMCDA>")) {
                            System.out.println("difference");
                            System.out.println(line1Replaced);
                            System.out.println(line1Replaced2);
                        } else {

                        }
                        reader2.close();
                    } else {
                        line1 = reader1.readLine();
                    }

                }
            }

            line1 = reader1.readLine();
        }
        reader1.close();
    }
}
