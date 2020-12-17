package be.libis.pdftool;

//import java.util.logging.Logger;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that splits PDF documents.
 */
public class Split {

  public Split(File source, File target, SplitOptions options) {
    try {

      if (options.getEveryPage()) {
        splitAllPages(source, target);
      } else if (options.isPage()) {
        splitAtPage(source, target, options.getPage());
      } else {
        System.err.println("No action to perform. Specify a page option.");
      }
    } catch (IOException | DocumentException e) {
      e.printStackTrace();
    }
  }

  private void splitAllPages(File source, File target) throws IOException, DocumentException {

    PdfReader reader = new PdfReader(new FileInputStream(source));
    int numberOfPages = reader.getNumberOfPages();

    String baseName = target.toString().substring(0, target.toString().lastIndexOf('.')) + "-%0" +
            String.valueOf(numberOfPages).length() + "d.pdf";

    List<Integer> pages = new ArrayList<>();

    for (int page = 0; page < numberOfPages; page++) {
      pages.clear();
      pages.add(page + 1);
      reader.selectPages(pages);
      PdfUtils.copyPdf(reader, new FileOutputStream(String.format(baseName, page + 1)));
      reader = new PdfReader(new FileInputStream((source)));
    }

    reader.close();
  }

  private void splitAtPage(File source, File target, int page) throws IOException, DocumentException {

    assert (page > 0);

    String baseName = target.toString().substring(0, target.toString().lastIndexOf('.'));

    PdfReader reader = new PdfReader(new FileInputStream(source));
    int numberOfPages = reader.getNumberOfPages();

    reader.selectPages(String.format("1-%d", page));
    PdfUtils.copyPdf(reader, new FileOutputStream(baseName + "-1.pdf"));

    if (page < numberOfPages) {
      reader = new PdfReader(new FileInputStream(source));
      reader.selectPages(String.format("%d-%d", page + 1, numberOfPages));
      PdfUtils.copyPdf(reader, new FileOutputStream(baseName + "-2.pdf"));
    }

    reader.close();
  }

}
