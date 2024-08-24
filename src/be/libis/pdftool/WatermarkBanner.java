package be.libis.pdftool;

import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

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
import java.util.Collections;

@Command(name = "banner", description = "Create a copy of a PDF file with a vertical text banner attached to the right of each page")
public class WatermarkBanner implements Runnable {
    @Spec
    private CommandSpec spec;

    @Option(order = 0, names = {"-d", "--dir"}, description = "Working directory", defaultValue = ".")
    private File directory;

    @Option(order = 1, names = {"-i", "--input"}, description = "Input PDF file", required = true)
    private File sourceFile;

    @Option(order = 2, names = {"-o", "--output"}, description = "Output PDF file", required = true)
    private File targetFile;

    @Option(order = 3, names = "--size", description = "Font size for the watermark text [${DEFAULT-VALUE}]", defaultValue = "20")
    private Float fontSize;

    @Option(order = 4, names = {"--width"}, description = "Width of the banner [${DEFAULT-VALUE}]", defaultValue = "36")
    private Float bannerWidth;

    @Option(order = 5, names = {"--background-red"}, description = "Background color, red component [${DEFAULT-VALUE}]", defaultValue = "0x52")
    private Integer bg_r;

    @Option(order = 6, names = {"--background-green"}, description = "Background color, green component [${DEFAULT-VALUE}]", defaultValue = "0xBD")
    private Integer bg_g;

    @Option(order = 7, names = {"--background-blue"}, description = "Background color, blue component [${DEFAULT-VALUE}]", defaultValue = "0xEC")
    private Integer bg_b;

    @Option(order = 8, names = {"--text-red"}, description = "Background color, red component [${DEFAULT-VALUE}]", defaultValue = "0xFF")
    private Integer txt_r;

    @Option(order = 9, names = {"--text-green"}, description = "Background color, green component [${DEFAULT-VALUE}]", defaultValue = "0xFF")
    private Integer txt_g;

    @Option(order = 10, names = {"--text-blue"}, description = "Background color, blue component [${DEFAULT-VALUE}]", defaultValue = "0xFF")
    private Integer txt_b;

    @Parameters(description = "Watermark text")
    private String text;

    @SuppressWarnings("CallToPrintStackTrace")
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

        if (bannerWidth < fontSize) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Width of banner should be >= font size");
        }

    }

    private void watermarker(File source, File target) throws IOException, DocumentException {

        PdfReader pdfReader = new PdfReader(new FileInputStream(source));
        PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(target));

        BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
        Size size = new Size(Collections.singletonList(text), 0, 0, baseFont, fontSize, 0); // this is the original (unscaled) text size; if the banner is too short the text will be scaled
        BaseColor bannerColor = new BaseColor(bg_r, bg_g, bg_b); // (R, G, B)
        BaseColor textColor = new BaseColor(txt_r, txt_g, txt_b); // (R, G, B)

        int numberOfPages = pdfReader.getNumberOfPages();
        for (int i = 1; i <= numberOfPages; i++) {
            int rotation = pdfReader.getPageRotation(i);
            PdfDictionary pageDict = pdfReader.getPageN(i);
            PdfArray cropbox = pageDict.getAsArray(PdfName.CROPBOX);
            PdfArray trimbox = pageDict.getAsArray(PdfName.TRIMBOX);
            PdfArray bleedbox = pageDict.getAsArray(PdfName.BLEEDBOX);
            PdfArray artbox = pageDict.getAsArray(PdfName.ARTBOX);
            PdfArray mediabox = pageDict.getAsArray(PdfName.MEDIABOX);
            if (rotation % 90 == 0 && rotation >= 0 && rotation <= 270 && mediabox != null) { // should be true for any valid pdf
                float llx = mediabox.getAsNumber(0).floatValue(); // ll = lower left
                float lly = mediabox.getAsNumber(1).floatValue();
                float urx = mediabox.getAsNumber(2).floatValue(); // ur = upper right
                float ury = mediabox.getAsNumber(3).floatValue();
                float cb_llx = llx;
                float cb_lly = lly;
                float cb_urx = urx;
                float cb_ury = ury;
                if (cropbox != null) {
                    cb_llx = cropbox.getAsNumber(0).floatValue();
                    cb_lly = cropbox.getAsNumber(1).floatValue();
                    cb_urx = cropbox.getAsNumber(2).floatValue();
                    cb_ury = cropbox.getAsNumber(3).floatValue();
                }
                PdfArray[] intermediateBoxes = {trimbox, bleedbox, artbox};
                // to ensure that the banner is visible it must fit inside the cropbox:
                // - extend the cropbox to make room for a banner
                // - extend the other boxes just enough to cover the extended cropbox
                // - if content exists outside the cropbox, draw the banner over any existing content
                float bannerX, bannerY, bannerLength;
                if (rotation == 0) {
                    // make room for a banner on the right
                    if (urx < cb_urx + bannerWidth)
                        mediabox.set(2, new PdfNumber(cb_urx + bannerWidth));
                    for (PdfArray box : intermediateBoxes)
                        if (box != null && box.getAsNumber(2).floatValue() < cb_urx + bannerWidth)
                            box.set(2, new PdfNumber(cb_urx + bannerWidth));
                    if (cropbox != null)
                        cropbox.set(2, new PdfNumber(cb_urx + bannerWidth));
                    bannerX = cb_urx;
                    bannerY = cb_lly;
                    bannerLength = cb_ury - cb_lly;
                } else if (rotation == 90) {
                    // make room for a banner at the top, which will appear on the right after the page is rotated
                    if (ury < cb_ury + bannerWidth)
                        mediabox.set(3, new PdfNumber(cb_ury + bannerWidth));
                    for (PdfArray box : intermediateBoxes)
                        if (box != null && box.getAsNumber(3).floatValue() < cb_ury + bannerWidth)
                            box.set(3, new PdfNumber(cb_ury + bannerWidth));
                    if (cropbox != null)
                        cropbox.set(3, new PdfNumber(cb_ury + bannerWidth));
                    bannerX = cb_ury - lly;
                    bannerY = urx - cb_urx;
                    bannerLength = cb_urx - cb_llx;
                } else if (rotation == 180) {
                    // make room for a banner on the left, which will appear on the right after the page is rotated
                    if (llx > cb_llx - bannerWidth)
                        mediabox.set(0, new PdfNumber(cb_llx - bannerWidth));
                    for (PdfArray box : intermediateBoxes)
                        if (box != null && box.getAsNumber(0).floatValue() > cb_llx - bannerWidth)
                            box.set(0, new PdfNumber(cb_llx - bannerWidth));
                    if (cropbox != null)
                        cropbox.set(0, new PdfNumber(cb_llx - bannerWidth));
                    bannerX = urx - cb_llx;
                    bannerY = ury - cb_ury;
                    bannerLength = cb_ury - cb_lly;
                } else { // rotation == 270
                    // make room for a banner at the bottom, which will appear on the right after the page is rotated
                    if (lly > cb_lly - bannerWidth)
                        mediabox.set(1, new PdfNumber(cb_lly - bannerWidth));
                    for (PdfArray box : intermediateBoxes)
                        if (box != null && box.getAsNumber(1).floatValue() > cb_lly - bannerWidth)
                            box.set(1, new PdfNumber(cb_lly - bannerWidth));
                    if (cropbox != null)
                        cropbox.set(1, new PdfNumber(cb_lly - bannerWidth));
                    bannerX = ury - cb_lly;
                    bannerY = cb_llx - llx;
                    bannerLength = cb_urx - cb_llx;
                }

                PdfContentByte overContent = pdfStamper.getOverContent(i);

                // draw the colored rectangle

                overContent.saveState();
                overContent.setColorFill(bannerColor);
                overContent.rectangle(bannerX, bannerY, bannerWidth, bannerLength);
                overContent.fill();
                overContent.restoreState();

                // draw the text over the rectangle

                overContent.beginText();
                overContent.saveState();

                overContent.setFontAndSize(baseFont, fontSize);
                overContent.setColorFill(textColor);

                // text positioning, 90° rotation and scaling are performed by a single affine transformation
                float scale = 1f;
                if (size.real_width > bannerLength - bannerWidth) // allow empty space of at least bannerWidth/2 on each side
                    scale = (bannerLength - bannerWidth) / size.real_width;
                float x = bannerX + (bannerWidth - scale * size.real_height) / 2f; // to center (scaled) text between bannerX and bannerX + bannerWidth
                float y = (bannerY + bannerLength) - (bannerLength - scale * size.real_width) / 2f; // to center (scaled) text between bannerY and bannerY + bannerLength
                AffineTransform af = new AffineTransform();
                af.setToTranslation(x, y);
                af.concatenate(AffineTransform.getRotateInstance(- Math.PI/2, 0, 0));
                af.concatenate(AffineTransform.getScaleInstance(scale, scale));
                overContent.setTextMatrix(af);
                overContent.showTextKerned(text);

                overContent.restoreState();
                overContent.endText();
            }
        }

        pdfStamper.close();
        pdfReader.close();
    }

}
