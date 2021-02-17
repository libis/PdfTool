package be.libis.pdftool;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Spec;

@Command(name = "watermark", description = "Create a watermarked copy of a PDF.",
        subcommands = {WatermarkHeader.class, WatermarkImage.class, WatermarkText.class}
)
public class Watermark implements Runnable {
    @Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}
