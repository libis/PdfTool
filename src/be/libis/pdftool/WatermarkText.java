package be.libis.pdftool;

import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.*;
import java.util.List;

@Command(name = "text", description = "Create a copy of a PDF file with a text watermark")
public class WatermarkText implements Runnable {
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

    @Option(order = 4, names = "--padding", description = "Fraction of total width/height to use as blank padding [${DEFAULT-VALUE}]", defaultValue = "0.5")
    private Float padding;

    @Option(order = 5, names = "--gap", description = "Amount of blank padding to add (points = 1/72 inch) [${DEFAULT-VALUE}]", defaultValue = "0")
    private Float gap;

    @Option(order = 6, names = "--size", description = "Font size for the watermark text [${DEFAULT-VALUE}]", defaultValue = "20")
    private Float fontSize;

    @Option(order = 7, names = "--rotation", description = "Rotation of the watermark text (in degrees) [${DEFAULT-VALUE}]", defaultValue = "15")
    private Float rotation;

    @Parameters(description = "Watermark text - each value will be printed on a separate line", arity = "1..*")
    private List<String> text;

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

        if (fontSize <= 0) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Font size should be > 0");
        }


        if (padding < 0) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Padding should be >= 0");
        }

        if (gap < 0) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Gap should be >= 0");
        }

        if (opacity < 0 || opacity > 1) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Opacity should be fraction between 0.0 and 1.0");
        }
    }

    private static final PdfName blending_mode = PdfGState.BM_HARDLIGHT;

    private void watermarker(File source, File target) throws IOException, DocumentException {

        PdfReader pdfReader = new PdfReader(new FileInputStream(source));
        PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(target));

        Size size;

        BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
        size = new Size(text, padding, gap, baseFont, fontSize, rotation);

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

            patternPainter.beginText();
            patternPainter.setTextRenderingMode(PdfPatternPainter.TEXT_RENDER_MODE_FILL);

            // set font for text watermark
            patternPainter.setFontAndSize(baseFont, fontSize);
            patternPainter.setColorFill(BaseColor.BLACK);
            AffineTransform affineTransform = new AffineTransform();
            for (float x = size.gap_width / 2f; x < pageSize.getWidth(); x += size.total_width) {
                for (float y = 0 + size.gap_height / 2f + size.start_height; y < pageSize.getHeight(); y += size.total_height) {
                    affineTransform.setToTranslation(x, y);
                    affineTransform.rotate(Math.toRadians(rotation));
                    patternPainter.setTextMatrix(affineTransform);
                    float delta_y = fontSize * 1.5f;
                    for (String line : text) {
                        patternPainter.showTextKerned(line);
                        patternPainter.newlineText();
                        patternPainter.moveText(0, -delta_y);
                    }
                }
            }

            patternPainter.endText();
            patternPainter.restoreState();

            overContent.rectangle(pageSize.getLeft(), pageSize.getBottom(), pageSize.getWidth(), pageSize.getHeight());
            overContent.setPatternFill(patternPainter);
            overContent.fill();

        }
        pdfStamper.close();
        pdfReader.close();
    }

}
