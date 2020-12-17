import be.libis.pdftool.Copy;
import be.libis.pdftool.CopyOptions;
import be.libis.toolbox.CommonOptionsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class CopyPdf {

  /**
   * Main application entry point.
   *
   * The method parses the command line arguments and checks them for common mistakes.
   * The real work is left to the class constructor which is called at the end.
   *
   * @param args the command line arguments
   */
    public static void main(String[] args) throws IOException {
      CopyOptions options = new CommonOptionsManager<CopyOptions>().processOptions(CopyOptions.class, args);

      if (options == null)
        return;

      Logger logger = LogManager.getLogger(CopyPdf.class.getName());

      if ((!options.isWatermarkText() && !options.isWatermarkImage())
          || (options.isWatermarkText() && options.isWatermarkImage())) {
        logger.error("Either watermark text or watermark image must be specified.");
        return;
      }

      File source = options.getSourceFile();

      if (!source.exists()) {
        logger.error("Source file '" + source.getAbsolutePath() + "' not found.");
        return;
      }

      if (!source.canRead()) {
        logger.error("Source file '" + source.getAbsolutePath() + "' cannot be read.");
        return;
      }

      File target = options.getTargetFile();

      if (!target.exists()) {
        if (!target.createNewFile()) {
          logger.error("Target file '" + target.getAbsolutePath() + "' cannot be created.");
          return;
        }
      }

      if (!target.canWrite()) {
        logger.error("Target file '" + target.getAbsolutePath() + "' cannot be written.");
        return;
      }

      new Copy(source, target, options);
    }

}
