package be.libis.pdftool;

import be.libis.toolbox.CommonOptions;
import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;

import java.io.File;
import java.util.List;

public interface MergeOptions extends CommonOptions {
  @Option(shortName = "o", longName = "file_output", description = "Output PDF file")
  File getTargetFile();

  @Option(shortName = "C", longName = "current_dir", description = "Set current directory before processing files")
  String getDirectory();
  boolean isDirectory();

  @Unparsed(name="FILE", description = "List of input files")
  List<File> getFiles();
}
