PDF_TOOL := java -jar out/artifacts/PdfTool_jar/PdfTool.jar
PDF_NAME := payara

test:
	${PDF_TOOL} split -i ${PDF_NAME}.pdf -o ${PDF_NAME}-page.pdf
	${PDF_TOOL} merge -o ${PDF_NAME}-merged.pdf ${PDF_NAME}-page-*.pdf
	${PDF_TOOL} metadata  -i ${PDF_NAME}.pdf -o ${PDF_NAME}-metadata.pdf -a author -c creator -k keywords -s subject -t title
	${PDF_TOOL} protect --open-password=open --edit-password=edit -i ${PDF_NAME}.pdf -o ${PDF_NAME}-protected.pdf
	${PDF_TOOL} select -r o -r "\!3-5" -r 10 -i ${PDF_NAME}.pdf -o ${PDF_NAME}-selected.pdf
	${PDF_TOOL} watermark banner -i ${PDF_NAME}.pdf -o ${PDF_NAME}-wm-header.pdf "LIBIS KU Leuven" --text-red 0x54 --text-green 0xbc --text-blue 0xeb --background-red 0x00 --background-green 0x40 --background-blue 0x7A
	${PDF_TOOL} watermark text -i ${PDF_NAME}.pdf -o ${PDF_NAME}-wm-text.pdf --opacity 0.5 --padding 0.8 --gap 50 --rotation -30 "LIBIS KU Leuven"
