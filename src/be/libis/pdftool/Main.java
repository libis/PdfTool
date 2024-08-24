package be.libis.pdftool;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(name = "PdfTool", description = "PDF manipulation tool. Allows copy, split and merge of PDF files.",
        scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true, usageHelpAutoWidth = true,
        synopsisSubcommandLabel = "COMMAND", versionProvider = Version.class, sortOptions = false,
        subcommands = {Split.class, Merge.class, Select.class, Watermark.class, Metadata.class, Protect.class}
)
public class Main implements Runnable {
    @Spec
    CommandSpec spec;

    public static void main(String... args) {
        int exitCode = new CommandLine(new Main())
                .registerConverter(Byte.class, Byte::decode)
                .registerConverter(Byte.TYPE, Byte::decode)
                .registerConverter(Short.class, Short::decode)
                .registerConverter(Short.TYPE, Short::decode)
                .registerConverter(Integer.class, Integer::decode)
                .registerConverter(Integer.TYPE, Integer::decode)
                .registerConverter(Long.class, Long::decode)
                .registerConverter(Long.TYPE, Long::decode)
                .setUsageHelpLongOptionsMaxWidth(30)
                .execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}
