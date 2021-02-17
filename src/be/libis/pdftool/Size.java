package be.libis.pdftool;

import com.itextpdf.text.pdf.BaseFont;

import java.util.List;

/**
 * Helper class to calculate image and text box sizes.
 * <p>
 * This class takes the text size and rotation into account. It also deals with gap sizes.
 */
public class Size {
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
    public float start_height;


    /**
     * Constructor for a text box
     *
     * @param watermark_text text for watermark
     */
    public Size(List<String> watermark_text, float padding, float gap,
                BaseFont baseFont, float fontSize, float textRotation) {
        float cosine = (float) Math.abs(Math.cos(Math.toRadians(textRotation)));
        float sine = (float) Math.abs(Math.sin(Math.toRadians(textRotation)));
        float w = 0f;
        for (String text : watermark_text) {
            float _w = baseFont.getWidthPointKerned(text, fontSize);
            if (_w > w) w = _w;
        }
        // this is the height that the next lines take
        start_height = (watermark_text.size() - 1) * (fontSize * 1.5f);
        float h1 = baseFont.getAscentPoint(watermark_text.get(0), fontSize);
        float h2 = baseFont.getDescentPoint(watermark_text.get(0), fontSize);
        float h = h1 - h2 + start_height;
        real_width = w * cosine + h * sine;
        real_height = w * sine + h * cosine;
        calculate_derived_sizes(padding, gap);
    }

    /**
     * method that calculates the gap and total sizes once the object's bounding box size is known
     */
    private void calculate_derived_sizes(float padding, float gap) {
        gap_width = real_width * padding + gap;
        gap_height = real_height * padding + gap;
        total_width = real_width + gap_width;
        total_height = real_height + gap_height;
    }

}
