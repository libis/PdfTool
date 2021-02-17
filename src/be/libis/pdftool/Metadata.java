package be.libis.pdftool;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Command(name = "metadata", description = "Copy a PDF file and set some metadata")
public class Metadata implements Runnable {
    @Spec
    private CommandSpec spec;

    @Option(order = 0, names = {"-d", "--dir"}, description = "Working directory", defaultValue = ".")
    private File directory;

    @Option(order = 1, names = {"-i", "--input"}, description = "Input PDF file", required = true)
    private File sourceFile;

    @Option(order = 2, names = {"-o", "--output"}, description = "Output PDF file", required = true)
    private File targetFile;

    @Option(order = 3, names = {"-t", "--title"}, description = "PDF Metadata title value")
    private String title;

    @Option(order = 4, names = {"-a", "--author"}, description = "PDF Metadata author value")
    private String author;

    @Option(order = 5, names = {"-s", "--subject"}, description = "PDF Metadata subject value")
    private String subject;

    @Option(order = 6, names = {"-k", "--keywords"}, description = "PDF Metadata keywords value")
    private String keywords;

    @Option(order = 7, names = {"-c", "--creator"}, description = "PDF Metadata creator value")
    private String creator;

    @Override
    public void run() {
        validate();
        try {
            copyPdf(sourceFile.getAbsolutePath(), targetFile.getAbsolutePath());
        } catch (IOException | DocumentException e) {
            System.err.println("Failed to create PDF file:");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void validate() {
        if (directory.getAbsoluteFile().exists()) {
            System.setProperty("user.dir", directory.getAbsolutePath());
        } else {
            throw new CommandLine.ParameterException(spec.commandLine(), "Directory '" + directory.getAbsolutePath() + "' nog found");
        }

        if (!sourceFile.getAbsoluteFile().exists()) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Source file '" + sourceFile.getAbsolutePath() + "' not found");
        }

        if (!sourceFile.getAbsoluteFile().canRead()) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Source file '" + sourceFile.getAbsolutePath() + "' cannot be read");
        }

        File targetDir = targetFile.getAbsoluteFile().getParentFile();
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Could not create target dir: '" + targetDir.getAbsolutePath() + "'");
        }

    }

    private void copyPdf(String source, String target) throws IOException, DocumentException {

        PdfReader pdfReader = new PdfReader(new FileInputStream(source));
        PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(target));

        // Change metadata
        Map<String, String> info = pdfStamper.getMoreInfo();
        if (info == null) {
            info = new HashMap<>();
        }
        String key = "Title";
        if (title != null)
            info.put(key, title);
        key = "Subject";
        if (subject != null)
            info.put(key, subject);
        key = "Keywords";
        if (keywords != null)
            info.put(key, keywords);
        key = "Creator";
        if (creator != null)
            info.put(key, creator);
        key = "Author";
        if (author != null)
            info.put(key, author);
        pdfStamper.setMoreInfo(info);

        pdfStamper.close();
        pdfReader.close();

    }

}
