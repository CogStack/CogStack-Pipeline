#!/bin/bash
set -e

RAWDATA_PATH="../../rawdata"

IN_SYN_DATA="$RAWDATA_PATH/synsamples.tgz"

DATA_SIZE="small"
IN_MT_PDF_DATA="$RAWDATA_PATH/mtsamples-@TYPE@-$DATA_SIZE.tar.bz2"

DB_DUMP_FILE="db_samples-@TYPE@-$DATA_SIZE.sql.gz"


doc_types=(docx
	pdf-text
	pdf-img
	jpg)

for dt in ${doc_types[@]}; do
	mt_data="${IN_MT_PDF_DATA/@TYPE@/$dt}"
	db_dump="${DB_DUMP_FILE/@TYPE@/$dt}"

	echo "----------------------------------------------------------------"
	echo "Preparing data for sub-example: ${dt}"
	bash prepare_single_db.sh $IN_SYN_DATA $mt_data $db_dump

	echo "DB dump stored as: $db_dump"
done

echo "Done."