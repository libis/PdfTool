package be.libis.pdftool;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.SimpleBookmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that merges PDF documents.
 */
public class Merge {

  public Merge(List<File> inputFiles, File target) {
    try {

      int pageOffset = 0;
      ArrayList<HashMap<String, Object>> bookmarkList = new ArrayList<>();
      Document document = null;
      PdfCopy writer = null;

      for (File input : inputFiles) {

        PdfReader reader = new PdfReader(input.getAbsolutePath());
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
