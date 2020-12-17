import be.libis.pdftool.Split;
import be.libis.pdftool.SplitOptions;
import be.libis.toolbox.CommonOptionsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class SplitPdf {

  /**
   * Main application entry point.
   *
   * The method parses the command line arguments and checks them for common mistakes.
   * The real work is left to the class constructor which is called at the end.
   *
   * @param args the command line arguments
   */
    public static void main(String[] args) {
      SplitOptions options = new CommonOptionsManager<SplitOptions>().processOptions(SplitOptions.class, args);

      if (options == null)
        return; // error messages are printed by GeneralOptionsHandler

      Logger logger = LogManager.getLogger(SplitPdf.class.getName());

      if (options.isDirectory()) {
        File directory = new File(options.getDirectory()).getAbsoluteFile();
        if (directory.exists()) {
          System.setProperty("user.dir", directory.getAbsolutePath());
        } else {
          logger.error("Directory '" + directory.getAbsolutePath() + "' not found.");
          return;
        }
      }

      File source = options.getSourceFile().getAbsoluteFile();

      if (!source.exists()) {
        logger.error("Source file '" + source.getAbsolutePath() + "' not found.");
        return;
      }

      if (!source.canRead()) {
        logger.error("Source file '" + source.getAbsolutePath() + "' cannot be read.");
        return;
      }

      File target = options.getTargetFile().getAbsoluteFile();
      File targetDir = target.getParentFile();
      if (!targetDir.exists() && !targetDir.mkdirs()) {
        logger.error(("Could not create target dir: '" + targetDir.getAbsolutePath() + "'."));
        return;
      }

      new Split(source, target, options);
    }

}
