package be.libis.pdftool;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Command(name = "image", description = "Create a copy of a PDF file with an image watermark")
public class WatermarkImage implements Runnable {
    @Spec
    private CommandSpec spec;

    @Option(order = 0, names = {"-d", "--dir"}, description = "Working directory", defaultValue = ".")
    private File directory;

    @Option(order = 1, names = {"-i", "--input"}, description = "Input PDF file", required = true)
    private File sourceFile;

    @Option(order = 2, names = {"-o", "--output"}, description = "Output PDF file", required = true)
    private File targetFile;

    @Option(order = 3, names = "--opacity", description = "Opacity - specify as fraction [${DEFAULT-VALUE}]", defaultValue = "0.1")
    private Float opacity;

    @Parameters(description = "Watermark image file", arity = "1")
    private File image;

    @Override
    public void run() {
        validate();
        try {
            watermarker(sourceFile.getAbsoluteFile(), targetFile.getAbsoluteFile());
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

        if (!image.getAbsoluteFile().exists()) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Image file '" + image.getAbsolutePath() + "' not found");
        }

        if (opacity < 0 || opacity > 1) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Opacity should be fraction between 0.0 and 1.0");
        }
    }

    private static final PdfName blending_mode = PdfGState.BM_HARDLIGHT;

    private void watermarker(File source, File target) throws IOException, DocumentException {

        PdfReader pdfReader = new PdfReader(new FileInputStream(source));
        PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(target));

        Image image_obj;

        image_obj = Image.getInstance(image.getAbsolutePath());

        // Process each page from the reader selection
        int numberOfPages = pdfReader.getNumberOfPages();
        for (int index = 1; index <= numberOfPages; index++) {
            // Process watermark objects if required
            // get over content to put watermark on top of all other objects
            PdfContentByte overContent = pdfStamper.getOverContent(index);
            Rectangle pageSize = pdfReader.getPageSizeWithRotation(index);

            PdfPatternPainter patternPainter = overContent.createPattern(pageSize.getWidth(), pageSize.getHeight());

            // create and set the graphics state
            PdfGState pdfGState = new PdfGState();
            pdfGState.setFillOpacity(opacity);
            pdfGState.setStrokeOpacity(opacity);
            pdfGState.setBlendMode(blending_mode);
            patternPainter.saveState();
            patternPainter.setGState(pdfGState);

            patternPainter.addImage(image_obj, image_obj.getWidth(), 0, 0, image_obj.getHeight(), 0, 0);

            patternPainter.restoreState();

            overContent.rectangle(pageSize.getLeft(), pageSize.getBottom(), pageSize.getWidth(), pageSize.getHeight());
            overContent.setPatternFill(patternPainter);
            overContent.fill();

        }
        pdfStamper.close();
        pdfReader.close();
    }

}
