package be.libis.pdftool;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kris on 23/11/15.
 */
public class PdfUtils {
  static public void copyPdf(PdfReader input, FileOutputStream output) throws DocumentException, IOException {
    int n = input.getNumberOfPages();
    Document document = new Document();
    PdfCopy copy = new PdfSmartCopy(document, output);
    document.open();
    for (int i = 0; i < n;) {
      copy.addPage(copy.getImportedPage(input, ++i));
    }
    document.close();
  }
}
