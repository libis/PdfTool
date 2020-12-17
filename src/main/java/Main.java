import be.libis.pdftool.CopyOptions;
import be.libis.pdftool.MergeOptions;
import be.libis.pdftool.SplitOptions;
import com.lexicalscope.jewel.cli.CliFactory;

import java.io.PrintStream;
import java.util.Properties;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author KrisD
 */
public class Main {

  static Properties prop = System.getProperties();

  public static void main(String[] args) {

    PrintStream out = System.err;

    String class_path = prop.getProperty("java.class.path", null);

    out.println("\nUsage: java -cp " + class_path + " <command> ...");

    out.println();
    out.println("\ncommand: CopyPdf  - Copy a PDF file and perform some actions\n");
    out.println(CliFactory.createCli(CopyOptions.class).getHelpMessage());

    out.println();
    out.println("\ncommand: MergePdf  - Merge a set of PDF files into one PDF\n");
    out.println(CliFactory.createCli(MergeOptions.class).getHelpMessage());

    out.println();
    out.println("\ncommand: SplitPdf - Split a PDF file into a set of PDFs\n");
    out.println(CliFactory.createCli(SplitOptions.class).getHelpMessage());
  }

}
