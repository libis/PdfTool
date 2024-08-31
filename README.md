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
  banner  Create a copy of a PDF file with a vertical text banner attached to the right of each page
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

We used the [GraalVM](https://www.graalvm.org/) JDK, version 21.0.0.2 based on Java 11. Most Java 11 based SDKs are The use of [sdkman](https://sdkman.io/) or [asdf](https://asdf-vm.com/)
to install the JDK is recommended. Install the JDK with this command:

```shell
$ sdk install java 21.0.0.2.r11-grl
```
But any Java SDK version 11, should work just fine. You may have to reconfigure your IDE to user that SDK.

## Testing

A Makefile is supplied that will test most of the options. It takes a pdf and performs a number of operations on it.
You can inspect manually and visually if the output is what is expected.

It is not possible to automate the tests as each PDF file will be slightly different due to the timestamping in the PDF.
We can therefor not provide master files to compare the results against as they will always be binary different even if
the operations are performed well.

The Makefile expects a myfile.pdf and a logo.png file to be present. You can use your own files by overriding some
variables:

```shell
$ PDF_NAME=otherfile IMG_FILE=mylogo.jpg make -e
```
```
java -jar out/artifacts/PdfTool_jar/PdfTool.jar split -i otherfile.pdf -o otherfile-page.pdf
otherfile-page-01.pdf
otherfile-page-02.pdf
otherfile-page-03.pdf
otherfile-page-04.pdf
otherfile-page-05.pdf
otherfile-page-06.pdf
otherfile-page-07.pdf
otherfile-page-08.pdf
otherfile-page-09.pdf
otherfile-page-10.pdf
java -jar out/artifacts/PdfTool_jar/PdfTool.jar merge -o otherfile-merged.pdf otherfile-page-*.pdf
java -jar out/artifacts/PdfTool_jar/PdfTool.jar metadata  -i otherfile.pdf -o otherfile-metadata.pdf -a author -c creator -k keywords -s subject -t title
java -jar out/artifacts/PdfTool_jar/PdfTool.jar protect --open-password=open --edit-password=edit -i otherfile.pdf -o otherfile-protected.pdf
java -jar out/artifacts/PdfTool_jar/PdfTool.jar select -r o -r "\!3-5" -r 10 -i otherfile.pdf -o otherfile-selected1.pdf
java -jar out/artifacts/PdfTool_jar/PdfTool.jar select -r o -r "\!3-5,10" -i otherfile.pdf -o otherfile-selected2.pdf
java -jar out/artifacts/PdfTool_jar/PdfTool.jar select -r "o,\!3-5,10" -i otherfile.pdf -o otherfile-selected3.pdf
java -jar out/artifacts/PdfTool_jar/PdfTool.jar watermark banner -i otherfile.pdf -o otherfile-wm-banner.pdf "LIBIS KU Leuven: " --text-red 0x54 --text-green 0xbc --text-blue 0xeb --background-red 0x00 --background-green 0x40 --background-blue 0x7A --add-filename
java -jar out/artifacts/PdfTool_jar/PdfTool.jar watermark text -i otherfile.pdf -o otherfile-wm-text.pdf --padding 0.8 --gap 50 --rotation -30 "LIBIS KU Leuven"
java -jar out/artifacts/PdfTool_jar/PdfTool.jar watermark image -i otherfile.pdf -o otherfile-wm-image.pdf --opacity 0.8 mylogo.jpg
```

## License

The software and source code is provided under the 
[Affero General Public License (AGPL)](http://www.gnu.org/licenses/agpl-3.0.html) just like the iText5 library. 