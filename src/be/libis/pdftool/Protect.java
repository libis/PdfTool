package be.libis.pdftool;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Command(name = "protect", description = "Copy a PDF file and protect it")
public class Protect implements Runnable {
    @Spec
    private CommandSpec spec;

    @Option(order = 0, names = {"-d", "--dir"}, description = "Working directory",
            defaultValue = ".", showDefaultValue = CommandLine.Help.Visibility.NEVER)
    private File directory;

    @Option(order = 1, names = {"-i", "--input"}, description = "Input PDF file", required = true)
    private File sourceFile;

    @Option(order = 2, names = {"-o", "--output"}, description = "Output PDF file", required = true)
    private File targetFile;

    @Option(order = 3, names = "--open-password", description = "Password required to read the PDF",
            defaultValue = "", showDefaultValue = CommandLine.Help.Visibility.NEVER)
    private String openPassword;

    @Option(order = 4, names = "--edit-password", description = "Password to overrule protections", required = true)
    private String editPassword;

    @Option(order = 6, names = "--print", defaultValue = "false",
            description = "Allow the user to print the document")
    private Boolean print;

    @Option(order = 7, names = "--assist", defaultValue = "false",
            description = "Allow copying content for accessibility only")
    private Boolean assist;

    @Option(order = 7, names = "--copy", defaultValue = "false",
            description = "Allow the user to copy content (implies --assist)")
    private Boolean copy;

    @Option(order = 8, names = "--fill-in", defaultValue = "false",
            description = "Allow the user to fill in a form")
    private Boolean fillIn;

    @Option(order = 9, names = "--comments", defaultValue = "false",
            description = "Allow the user to fill in forms and add comments to the document (implies --fill-in)")
    private Boolean comments;

    @Option(order = 10, names = "--manage", defaultValue = "false",
            description = "Allow the user to insert, remove and rotate pages and add bookmarks")
    private Boolean manage;

    @Option(order = 11, names = "--edit", defaultValue = "false",
            description = "Allow the user to fill in forms, manage pages and bookmarks and edit the content (implies --fill-in and --manage)")
    private Boolean edit;

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

        if (openPassword == null) {
            openPassword = "";
        }

        if (editPassword == null) {
            throw new CommandLine.ParameterException(spec.commandLine(), "A owner password is required");
        }

    }

    private void copyPdf(String source, String target) throws IOException, DocumentException {

        PdfReader pdfReader = new PdfReader(new FileInputStream(source));
        PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(target));

        int permissions = (assist ? PdfCopy.ALLOW_SCREENREADERS : 0) |
                (print ? PdfCopy.ALLOW_PRINTING : 0) |
                (copy ? PdfCopy.ALLOW_COPY : 0) |
                (fillIn ? PdfCopy.ALLOW_FILL_IN : 0) |
                (comments ? PdfCopy.ALLOW_MODIFY_ANNOTATIONS : 0) |
                (manage ? PdfCopy.ALLOW_ASSEMBLY : 0) |
                (edit ? PdfCopy.ALLOW_MODIFY_CONTENTS : 0);
        int encryption = PdfCopy.ENCRYPTION_AES_256 | PdfCopy.DO_NOT_ENCRYPT_METADATA;
        pdfStamper.setEncryption(openPassword.getBytes(), editPassword.getBytes(), permissions, encryption);

        pdfStamper.close();
        pdfReader.close();

    }

}
