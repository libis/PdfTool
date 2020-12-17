package be.libis.pdftool;

import be.libis.toolbox.RandomString;
import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfImageObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Copy {
    private static final PdfName blending_mode = PdfGState.BM_HARDLIGHT;
    private static BaseFont baseFont;

    /**
     * Helper class to calculate image and text box sizes.
     * <p>
     * This class takes the text size and rotation into account. It also deals with gap sizes.
     */
    private class Size {
        /**
         * the width of the object's bounding box
         */
        public float real_width;
        /**
         * the height of the object's bounding box
         */
        public float real_height;
        /**
         * the width of the whitespace around the object
         */
        public float gap_width;
        /**
         * the height of the whitespace around the object
         */
        public float gap_height;
        /**
         * the total width of the object including the whitespace
         */
        public float total_width;
        /**
         * the total height of the object including the whitespace
         */
        public float total_height;
        /**
         * the extra height from where to start the object
         */
        public float start_height = 0f;


        /**
         * Constructor for a text box
         *
         * @param watermark_text text for watermark
         */
        public Size(List<String> watermark_text) {
            float cosine = (float) Math.abs(Math.cos(Math.toRadians(text_rotation)));
            float sine = (float) Math.abs(Math.sin(Math.toRadians(text_rotation)));
            float w = 0f;
            for (String text : watermark_text) {
                float _w = baseFont.getWidthPointKerned(text, font_size);
                if (_w > w)
                    w = _w;
            }
            // this is the height that the next lines take
            start_height = (watermark_text.size() - 1) * (font_size * 1.5f);
            float h1 = baseFont.getAscentPoint(watermark_text.get(0), font_size);
            float h2 = baseFont.getDescentPoint(watermark_text.get(0), font_size);
            float h = h1 - h2 + start_height;
            real_width = w * cosine + h * sine;
            real_height = w * sine + h * cosine;
            calculate_derived_sizes();
        }

        /**
         * Constructor for an image
         *
         * @param image image data
         */
        public Size(Image image) {
            real_width = image.getWidth();
            real_height = image.getHeight();
            calculate_derived_sizes();
        }

        /**
         * method that calculates the gap and total sizes once the object's bounding box size is known
         */
        private void calculate_derived_sizes() {
            gap_width = real_width * gap_ratio + gap_size;
            gap_height = real_height * gap_ratio + gap_size;
            total_width = real_width + gap_width;
            total_height = real_height + gap_height;
        }

    }

    public float opacity = 0.1f;
    public float gap_ratio = 0.5f;
    public float gap_size = 0f;
    public float font_size = 20f;
    public float text_rotation = 15.0f;
    public String page_ranges = null;
    public String password = null;
    public boolean allow_print = false;
    public boolean allow_copy = false;
    public boolean allow_assembly = false;
    public boolean allow_annotations = false;
    public String title = null;
    public String subject = null;
    public String keywords = null;
    public String creator = null;
    public String author = null;
    public List<String> wm_text = null;
    public File wm_image = null;
    public Float image_scale = null;

    /**
     * Constructor called from command-line processor.
     *
     * @param source  The PDF to be copied
     * @param target  The PDF to be created
     * @param options The command line options
     */
    public Copy(File source, File target, CopyOptions options) {
        if (options.isOpacity())
            opacity = options.getOpacity();
        if (options.isGapRatio())
            gap_ratio = options.getGapRatio();
        if (options.isGapSize())
            gap_size = options.getGapSize();
        if (options.isFontSize())
            font_size = options.getFontSize();
        if (options.isTextRotation())
            text_rotation = options.getTextRotation();
        if (options.isPageRanges())
            page_ranges = options.getPageRanges();
        if (options.isPassword()) {
            password = options.getPassword();
            allow_print = options.getAllowPrint();
            allow_copy = options.getAllowCopy();
            allow_assembly = options.getAllowAssembly();
            allow_annotations = options.getAllowAnnotations();
        }
        if (options.isTitle())
            title = options.getTitle();
        if (options.isSubject())
            subject = options.getSubject();
        if (options.isKeywords())
            keywords = options.getKeywords();
        if (options.isCreator()) {
            creator = options.getCreator();
        }
        if (options.isAuthor())
            author = options.getAuthor();
        if (options.isWatermarkText()) {
            wm_text = options.getWatermarkText();
        } else if (options.isWatermarkImage()) {
            wm_image = options.getWatermarkImage();
        }

        if (options.isImageScale()) {
            image_scale = options.getImageScale();
        }

        copyPdf(source, target);
    }


    /**
     * Method that performs the real work
     *
     * @param source The PDF to be copied
     * @param target The PDF to be created
     */
    public void copyPdf(File source, File target) {
        try {

            // Create reader on the source PDF and select the pages that were requested
            PdfReader pdfReader = new PdfReader(source.getAbsolutePath());
            if (page_ranges != null) {
                pdfReader.selectPages(page_ranges);
            }

            if (image_scale != null) {
                int numberOfXrefs = pdfReader.getXrefSize();
                System.out.println(numberOfXrefs);
                PdfObject object;
                PRStream stream;
                for (int i = 0; i < numberOfXrefs; i++) {
                    object = pdfReader.getPdfObject(i);
                    if (object == null || !object.isStream()) continue;
                    System.out.println("IsStream");
                    stream = (PRStream) object;
                    Set<PdfName> keys = stream.getKeys();
                    for (PdfName name : keys) {
                        System.out.println(name.toString());
                        System.out.println(stream.get(name));
                    }
//          if (!PdfName.IMAGE.equals(stream.getAsName(PdfName.SUBTYPE)))
//            continue;
//          PdfObject pdfSubType = stream.get(PdfName.SUBTYPE);
//          if (pdfSubType != null) System.out.println(pdfSubType.toString());
//          if (pdfSubType != null && pdfSubType.toString().equals(PdfName.IMAGE.toString())) {
                    if (Integer.parseInt(String.valueOf(stream.get(PdfName.LENGTH))) < 100000) continue;
                    System.out.println("Parsing ...");

                    PdfImageObject imageObject = new PdfImageObject(stream);
                    BufferedImage bufferedImage = imageObject.getBufferedImage();
                    if (bufferedImage == null) continue;
                    System.out.println(bufferedImage.getWidth());
                    System.out.println(bufferedImage.getHeight());
                    int width = (int)(bufferedImage.getWidth() * image_scale);
                    int height = (int)(bufferedImage.getHeight() * image_scale);
                    System.out.println(width);
                    System.out.println(height);
                    BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    java.awt.geom.AffineTransform transform = java.awt.geom.AffineTransform.getScaleInstance(image_scale, image_scale);
                    Graphics2D graphics2D = newImage.createGraphics();
                    graphics2D.drawRenderedImage(bufferedImage, transform);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(newImage, "JPG", byteArrayOutputStream);
                    stream.clear();
                    stream.setData(byteArrayOutputStream.toByteArray(), false, PRStream.BEST_COMPRESSION);
                    stream.put(PdfName.TYPE, PdfName.XOBJECT);
                    stream.put(PdfName.SUBTYPE, PdfName.IMAGE);
                    stream.put(PdfName.FILTER, PdfName.DCTDECODE);
                    stream.put(PdfName.WIDTH, new PdfNumber(width));
                    stream.put(PdfName.HEIGHT, new PdfNumber(height));
                    stream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                    stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);          }

//        }
            }

            PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(target));

            // Set encryption on the target PDF
            if (password != null) {
                String owner_password = new RandomString(32).nextString();
                int permissions = (allow_print ? PdfCopy.ALLOW_PRINTING : 0)
                        + (allow_copy ? PdfCopy.ALLOW_COPY : 0)
                        + (allow_assembly ? PdfCopy.ALLOW_ASSEMBLY : 0)
                        + (allow_annotations ? PdfCopy.ALLOW_MODIFY_ANNOTATIONS : 0);
                int encryption = PdfCopy.STANDARD_ENCRYPTION_40 | PdfCopy.DO_NOT_ENCRYPT_METADATA;
                pdfStamper.setEncryption(password.getBytes(), owner_password.getBytes(), permissions, encryption);
            }

            // Disabled compression as jHove complains about this.
            //pdfStamper.setFullCompression();

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

            // Initialize the watermark objects
            Image image = null;
            List<String> watermark_text = null;
            Size size = null;

            if (wm_text != null) {
                baseFont = BaseFont.createFont("FreeSansBold.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
                watermark_text = wm_text;
                size = new Size(watermark_text);
            } else if (wm_image != null) {
                image = Image.getInstance(wm_image.getAbsolutePath());
                size = new Size(image);
            }

            // Process each page from the reader selection
            int numberOfPages = pdfReader.getNumberOfPages();
            for (int index = 1; index <= numberOfPages; index++) {
                // Process watermark objects if required
                if (size != null) {
                    // get over content to put watermark on top of all other objects
                    PdfContentByte overContent = pdfStamper.getOverContent(index);
                    Rectangle pageSize  = pdfReader.getPageSizeWithRotation(index);

                    PdfPatternPainter patternPainter = overContent.createPattern(pageSize.getWidth(), pageSize.getHeight());

                    // create and set the graphics state
                    PdfGState pdfGState = new PdfGState();
                    pdfGState.setFillOpacity(opacity);
                    pdfGState.setStrokeOpacity(opacity);
                    pdfGState.setBlendMode(blending_mode);
                    patternPainter.saveState();
                    patternPainter.setGState(pdfGState);

                    if (image != null) {
                        patternPainter.addImage(image, size.real_width, 0, 0, size.real_height, 0, 0);
                    } else {
                        patternPainter.beginText();
                        patternPainter.setTextRenderingMode(PdfPatternPainter.TEXT_RENDER_MODE_FILL);
                        // set font for text watermark
                        patternPainter.setFontAndSize(baseFont, font_size);
                        patternPainter.setColorFill(BaseColor.BLACK);
                        AffineTransform affineTransform = new AffineTransform();
                        for(float x = size.gap_width / 2f; x < pageSize.getWidth(); x += size.total_width) {
                            for(float y = 0 + size.gap_height / 2f + size.start_height; y < pageSize.getHeight(); y += size.total_height) {
                                affineTransform.setToTranslation(x,y);
                                affineTransform.rotate(Math.toRadians(text_rotation));
                                patternPainter.setTextMatrix(affineTransform);
                                float delta_y = font_size * 1.5f;
                                for (String text : watermark_text) {
                                    patternPainter.showTextKerned(text);
                                    patternPainter.newlineText();
                                    patternPainter.moveText(0, - delta_y);
                                }
                            }
                        }
                        patternPainter.endText();
                    }

                    patternPainter.restoreState();

//          // get the page dimensions
//          Rectangle boundingBox = pdfStamper.getImportedPage(pdfReader, index).getBoundingBox();
//          float boundingBoxLeft = boundingBox.getLeft();
//          float boundingBoxRight = boundingBox.getRight();
//          float boundingBoxBottom = boundingBox.getBottom();
//          float boundingBoxTop = boundingBox.getTop();

//          overContent.rectangle(boundingBoxLeft, boundingBoxTop, boundingBoxRight, boundingBoxBottom);
                    overContent.rectangle(pageSize.getLeft(), pageSize.getBottom(), pageSize.getWidth(), pageSize.getHeight());
                    overContent.setPatternFill(patternPainter);
                    overContent.fill();

                }
            }
            pdfStamper.close();
            pdfReader.close();
        } catch (Exception e) {
            System.out.format("Caught exception: %s", e.toString());
            e.printStackTrace(System.out);
        }

    }

}
