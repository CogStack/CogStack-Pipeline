#!/bin/bash
set -e


# global defines
#
SOFFICE_BIN="/Applications/LibreOffice.app/Contents/MacOS/soffice"

DATA_DIR="./rawdata/"
IN_MT_DATA="$DATA_DIR/mtsamples.tgz"
OUT_MT_PDF_DATA="$DATA_DIR/mtsamples-pdf.tar.bz2"

TMP_DIR=__tmp
OUT_PDF_DIR=__out_pdf
LOG_FILE=__prepare_docs.log


# entry point
#

# check whether the LibreOffice is installed, etc
#
if [ ! -e $SOFFICE_BIN ]; then
	echo "Please set the path for LibreOffice soffice binary"
	exit 0
fi

echo "Preparing the documents data for the examples"


# decompress the text files
#
echo "Decompressing mt samples data"
if [ -e $TMP_DIR ]; then rm -rf $TMP_DIR; fi
mkdir $TMP_DIR

tar -xzf $IN_MT_DATA -C $TMP_DIR


# process the documents
#
NF=$(ls -l $TMP_DIR | wc -l)
echo "Processing the documents -- total: $NF files"

echo "Converting: TXT -> PDF"

if [ -e $LOG_FILE ]; then rm $LOG_FILE; fi
if [ -e $OUT_PDF_DIR ]; then rm -rf $OUT_PDF_DIR; fi
mkdir $OUT_PDF_DIR

echo "*---> processing documents in bulk using LibreOffice"

$SOFFICE_BIN --headless --convert-to pdf --outdir $OUT_PDF_DIR $TMP_DIR/mtsamples-type-*.txt >> $LOG_FILE

echo "*---> compressing the documents and storing them as $OUT_MT_PDF_DATA"
( cd $OUT_PDF_DIR && tar -cJf ../$OUT_MT_PDF_DATA mtsamples-type-*.pdf )


# cleanup
#
echo "Cleaning up"
rm -rf $TMP_DIR
rm -rf $OUT_PDF_DIR

echo "Done."
echo "For more information on documents processing, see log: $LOG_FILE"