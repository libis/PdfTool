package be.libis.pdftool;

import be.libis.toolbox.CommonOptions;
import com.lexicalscope.jewel.cli.Option;

import java.io.File;

public interface SplitOptions extends CommonOptions {
  @Option(shortName = "i", longName = "file_input", description = "Input PDF file")
  File getSourceFile();

  @Option(shortName = "o", longName = "file_output", description = "Output PDF file (base name)")
  File getTargetFile();

  @Option(shortName = "C", longName = "current_dir", description = "Set current directory before processing files")
  String getDirectory();
  boolean isDirectory();

  @Option(shortName = "p", longName = "page", description = "Split PDF at page number")
  int getPage();
  boolean isPage();

  @Option(shortName = "e", longName = "every_page", description = "Split PDF at every page")
  boolean getEveryPage();
}
