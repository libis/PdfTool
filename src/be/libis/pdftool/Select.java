package be.libis.pdftool;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Command(name = "select", description = "Select a range of pages from a PDF")
public class Select implements Runnable {
    @Spec
    private CommandSpec spec;

    @Option(order = 0, names = {"-d", "--dir"}, description = "Working directory", defaultValue = ".")
    private File directory;

    @Option(order = 1, names = {"-i", "--input"}, description = "Input PDF file", required = true)
    private File sourceFile;

    @Option(order = 2, names = {"-o", "--output"}, description = "Output PDF file", required = true)
    private File targetFile;

    @Option(order = 3, names = {"-r", "--range"}, required = true,
            description = "A range of pages, specified as [!][o][odd][e][even]start-end\n" +
            "The '!' modifier removes the range from what is already selected. " +
            "The range changes are incremental, that is, numbers are added or deleted as the range appears. " +
            "The start or the end, but not both, can be omitted, defaulting to the first and last page respectively.",
            arity = "1..*")
    private List<String> range;

    @Override
    public void run() {
        validate();

        try {
            selectRange(sourceFile.getAbsoluteFile(), targetFile.getAbsoluteFile());
        } catch (IOException | DocumentException e) {
            System.err.println("There was an error processing the PDF.");
            e.printStackTrace();
            System.exit(-1);
        }

    }

    private void validate() {
        if (directory.getAbsoluteFile().exists()) {
            System.setProperty("user.dir", directory.getAbsolutePath());
        } else {
            throw new ParameterException(spec.commandLine(), "Directory '" + directory.getAbsolutePath() + "' nog found");
        }

        if (!sourceFile.getAbsoluteFile().exists()) {
            throw new ParameterException(spec.commandLine(), "Source file '" + sourceFile.getAbsolutePath() + "' not found");
        }

        if (!sourceFile.getAbsoluteFile().canRead()) {
            throw new ParameterException(spec.commandLine(), "Source file '" + sourceFile.getAbsolutePath() + "' cannot be read");
        }

        File targetDir = targetFile.getAbsoluteFile().getParentFile();
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new ParameterException(spec.commandLine(), "Could not create target dir: '" + targetDir.getAbsolutePath() + "'");
        }

    }

    private void selectRange(File source, File target) throws IOException, DocumentException {

        PdfReader reader = new PdfReader(new FileInputStream(source));
        reader.selectPages(String.join(",", range));

        PdfUtils.copyPdf(reader, new FileOutputStream(target));

        reader.close();

    }
}
