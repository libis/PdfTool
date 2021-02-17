package be.libis.pdftool;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(name = "split", description = "Split a PDF file into a set of PDFs")
public class Split implements Runnable {
    @Spec private CommandSpec spec;

    @Option(order = 0, names = {"-d", "--dir"}, description = "Working directory", defaultValue = ".")
    private File directory;

    @Option(order = 1, names = {"-i", "--input"}, description = "Input PDF file", required = true)
    private File sourceFile;

    @Option(order = 2, names = {"-o", "--output"}, description = "Output PDF file (file name with extension)", required = true)
    private File targetFile;

    @Option(order = 3, names = {"-p", "--page"}, description = "Split PDF after page number (default is 0, meaning split at every page)",
            arity = "0..1", defaultValue = "0", fallbackValue = "0")
    private Integer page;

    @Override
    public void run() {
        validate();

        String baseName = targetFile.toString();
        int last = baseName.lastIndexOf('.');
        if (last > 0) {
            baseName = baseName.substring(0, last);
        }

        if (page == 0) {
            try {
                splitAllPages(sourceFile.getAbsoluteFile(), baseName);
            } catch (IOException | DocumentException e) {
                System.err.println("There was an error processing the PDF.");
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            try {
                splitAtPage(sourceFile.getAbsoluteFile(), baseName, page);
            } catch (IOException | DocumentException e) {
                System.err.println("There was an error processing the PDF.");
                e.printStackTrace();
                System.exit(-1);
            }
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

        if (page < 0) {
            throw new ParameterException(spec.commandLine(), "Negative page values are not allowed");
        }
    }

    private void splitAllPages(File source, String baseName) throws IOException, DocumentException {

        PdfReader reader = new PdfReader(new FileInputStream(source));
        int numberOfPages = reader.getNumberOfPages();
        reader.close();

        String baseFormat = baseName + "-%0" + String.valueOf(numberOfPages).length() + "d.pdf";
        List<Integer> pages = new ArrayList<>();

        for (int page = 0; page < numberOfPages; page++) {
            pages.clear();
            pages.add(page + 1);
            reader = new PdfReader(new FileInputStream((source)));
            reader.selectPages(pages);
            String fileName = String.format(baseFormat, page + 1);
            PdfUtils.copyPdf(reader, new FileOutputStream(fileName));
            System.out.println(fileName);
            reader.close();
        }

    }

    private void splitAtPage(File source, String baseName, int page) throws IOException, DocumentException {

        assert (page > 0);

        PdfReader reader = new PdfReader(new FileInputStream(source));
        int numberOfPages = reader.getNumberOfPages();

        reader.selectPages(String.format("-%d", page));
        PdfUtils.copyPdf(reader, new FileOutputStream(baseName + "-1.pdf"));
        reader.close();

        if (page < numberOfPages) {
            reader = new PdfReader(new FileInputStream(source));
            reader.selectPages(String.format("%d-%d", page + 1, numberOfPages));
            PdfUtils.copyPdf(reader, new FileOutputStream(baseName + "-2.pdf"));
        }
        reader.close();
    }
}
