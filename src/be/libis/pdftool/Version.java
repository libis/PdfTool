package be.libis.pdftool;

import picocli.CommandLine;

public class Version implements CommandLine.IVersionProvider {

    static final String version = "1.0.beta";

    @Override
    public String[] getVersion() throws Exception {
        return new String[] {"PdfTool " + version};
    }
}
