# PdfTool

A PDF manipulation tool written in Java using 
[iText 5](https://itextpdf.com/en/products/itext-5-legacy) Community and 
[picocli](https://picocli.info/).

The tool allows to split or merge PDFs, update PDF metadata,
protect a PDF and add watermarks to it.

## Usage

```shell
$ java -jar PdfTool.jar <COMMAND> <OPTIONS> <ARGUMETNS> 
```

The application provides help by adding the --help option. 
The help may be different based on the commands that are provided:

```shell
$ java -jar PdfTool.jar --help
```
```
Usage: PdfTool [-hV] COMMAND
PDF manipulation tool. Allows copy, split and merge of PDF files.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  split      Split a PDF file into a set of PDFs
  merge      Merge a set of PDF files into one PDF
  select     Select a range of pages from a PDF
  watermark  Create a watermarked copy of a PDF.
  metadata   Copy a PDF file and set some metadata
  protect    Copy a PDF file and protect it
```

```shell
$ java -jar PdfTool.jar watermark --help
```
```
Usage: PdfTool watermark [-hV] COMMAND
Create a watermarked copy of a PDF.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  header  Create a copy of a PDF file with a watermark in the page header
  image   Create a copy of a PDF file with an image watermark
  text    Create a copy of a PDF file with a text watermark
```

```shell
$ java -jar PdfTool.jar watermark text --help
```
```
Usage: PdfTool watermark text [-hV] [-d=<directory>] [--gap=<gap>] -i=<sourceFile> -o=<targetFile> [--opacity=<opacity>] [--padding=<padding>] [--rotation=<rotation>] [--size=<fontSize>] <text>...
Create a copy of a PDF file with a text watermark
      <text>...               Watermark text - each value will be printed on a separate line
  -d, --dir=<directory>       Working directory
      --gap=<gap>             Amount of blank padding to add (points = 1/72 inch) [0]
  -h, --help                  Show this help message and exit.
  -i, --input=<sourceFile>    Input PDF file
  -o, --output=<targetFile>   Output PDF file
      --opacity=<opacity>     Opacity - specify as fraction [0.1]
      --padding=<padding>     Fraction of total width/height to use as blank padding [0.5]
      --rotation=<rotation>   Rotation of the watermark text (in degrees) [15]
      --size=<fontSize>       Font size for the watermark text [20]
  -V, --version               Print version information and exit.
```

Note that the PdfTool jar file requires two other jars to be on the classpath:
- bcprov-jdk15on-167.jar
- bcpkix-jdk15on-167.jar

## Development

This application is build by using [IntelliJ Idea](https://www.jetbrains.com/idea/)
Community version. The project files for the IDE are included in the repository.

We used the [GraalVM](https://www.graalvm.org/) JDK, version 21.0.0.2 
based on Java 11. The use of [sdkman](https://sdkman.io/)
to install the JDK is recommended. Install the JDK with this command:

```shell
$ sdk install java 21.0.0.2.r11-grl
```

## License

The software and source code is provided under the 
[Affero General Public License (AGPL)](http://www.gnu.org/licenses/agpl-3.0.html). 