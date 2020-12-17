import be.libis.pdftool.Merge;
import be.libis.pdftool.MergeOptions;
import be.libis.toolbox.CommonOptionsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MergePdf {

  /**
   * Main application entry point.
   *
   * The method parses the command line arguments and checks them for common mistakes.
   * The real work is left to the class constructor which is called at the end.
   *
   * @param args the command line arguments
   */
    public static void main(String[] args) {
      MergeOptions options = new CommonOptionsManager<MergeOptions>().processOptions(MergeOptions.class, args);

      if (options == null)
        return; // error messages are printed by GeneralOptionsHandler

      Logger logger = LogManager.getLogger(MergePdf.class.getName());

      if (options.isDirectory()) {
        File directory = new File(options.getDirectory()).getAbsoluteFile();
        if (directory.exists()) {
          System.setProperty("user.dir", directory.getAbsolutePath());
        } else {
          logger.error("Directory '" + directory.getAbsolutePath() + "' not found.");
          return;
        }
      }

      boolean file_missing = false;
      List<File> inputFiles = new ArrayList<>();
      for (File input : options.getFiles()) {
        if (!input.getAbsoluteFile().exists()) {
          logger.error("Input file '" + input.getAbsolutePath() + "' not found.");
          file_missing = true;
        }
        if (!input.getAbsoluteFile().canRead()) {
          logger.error("Input file '" + input.getAbsolutePath() + "' cannot be read.");
          file_missing = true;
        }
        inputFiles.add(input.getAbsoluteFile());
      }

      if (file_missing) {
        return;
      }

      File target = options.getTargetFile().getAbsoluteFile();
      File targetDir = target.getParentFile();
      if (!targetDir.exists() && !targetDir.mkdirs()) {
        logger.error(("Could not create target dir: '" + targetDir.getAbsolutePath() + "'."));
        return;
      }

      new Merge(inputFiles, target);
    }

}
