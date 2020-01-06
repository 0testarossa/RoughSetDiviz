package org.xmcda;

import java.util.ArrayList;
import java.util.Arrays;

public class SortingCLI {
    public SortingCLI(){};

    public static void main(String[] args) throws Exception
    {
        final ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
        if ( argsList.remove("--v2") )
        {
            ClassificationXMCDAv2.main((String[]) argsList.toArray(new String[]{}));
        }
        else if ( argsList.remove("--v3") )
        {
            ClassificationXMCDAv3.main((String[]) argsList.toArray(new String[]{}));
        }
        else
        {
            System.err.println("missing mandatory option --v2 or --v3");
            System.exit(-1);
        }
    }
}
