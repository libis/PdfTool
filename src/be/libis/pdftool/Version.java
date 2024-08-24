package be.libis.pdftool;

import picocli.CommandLine;

public class Version implements CommandLine.IVersionProvider {

    static final String version = "1.0";

    @Override
    public String[] getVersion() {
        return new String[] {"PdfTool " + version};
    }
}
