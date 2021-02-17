package be.libis.pdftool;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.SimpleBookmark;

import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Command(name = "merge", description = "Merge a set of PDF files into one PDF")
public class Merge implements Runnable {
    @Spec private CommandSpec spec;

    @Option(order = 1, names = {"-d", "--dir"}, description = "Working directory", defaultValue = ".")
    private File directory;

    @Option(order = 2, names = {"-o", "--output"}, description = "Output PDF file", required = true)
    private File targetFile;

    @Parameters(index = "0", description = "List of input files", arity = "1..*")
    private List<File> files;

    @Override
    public void run() {

        validate();

        merge(files, targetFile);
    }

    private void validate() {
        if (directory.getAbsoluteFile().exists()) {
            System.setProperty("user.dir", directory.getAbsolutePath());
        } else {
            throw new ParameterException(spec.commandLine(), "Directory '" + directory.getAbsolutePath() + "' nog found");
        }

        File targetDir = targetFile.getAbsoluteFile().getParentFile();
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new ParameterException(spec.commandLine(), "Could not create target dir: '" + targetDir.getAbsolutePath() + "'");
        }

        for (File file : files) {
            if (!file.getAbsoluteFile().exists()) {
                throw new ParameterException(spec.commandLine(), "Source file '" + file.getAbsolutePath() + "' not found");
            }

            if (!file.getAbsoluteFile().canRead()) {
                throw new ParameterException(spec.commandLine(), "Source file '" + file.getAbsolutePath() + "' cannot be read");
            }
        }
    }

    public void merge(List<File> inputFiles, File target) {
        try {

            int pageOffset = 0;
            ArrayList<HashMap<String, Object>> bookmarkList = new ArrayList<>();
            Document document = null;
            PdfCopy writer = null;

            for (File input : inputFiles) {
                PdfReader reader = new PdfReader(new FileInputStream(input.getAbsolutePath()));
                reader.consolidateNamedDestinations();

                int numberOfPages = reader.getNumberOfPages();

                List<HashMap<String, Object>> bookmarks = SimpleBookmark.getBookmark(reader);
                if (bookmarks != null) {
                    if (pageOffset != 0) {
                        SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
                    }
                    bookmarkList.addAll(bookmarks);
                }
                pageOffset += numberOfPages;

                if (document == null) {
                    document = new Document(reader.getPageSizeWithRotation(1));
                    writer = new PdfSmartCopy(document, new FileOutputStream(target));
                    document.open();
                }

                writer.addDocument(reader);

                reader.close();

            }

            if (!bookmarkList.isEmpty()) {
                writer.setOutlines(bookmarkList);
            }

            assert document != null;
            document.close();

        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
    }

}
