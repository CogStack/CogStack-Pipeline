#!/bin/bash
set -e

AWS_S3_BUCKET_URL="http://cogstack.s3.amazonaws.com"
DATA_PATH="share/examples/db_dump"
DATA_PATH_URL="$AWS_S3_BUCKET_URL/$DATA_PATH"


files=(
example1/db_samples.sql.gz
example2/db_samples.sql.gz
example3/db_samples-syn.sql.gz
example3/db_samples-mt.sql.gz
example4/db_samples-docx-small.sql.gz
example4/db_samples-pdf-text-small.sql.gz
example4/db_samples-pdf-img-small.sql.gz
example4/db_samples-jpg-small.sql.gz)

for filename in ${files[@]}; do
	url="$DATA_PATH_URL/$filename"
	db_file="${filename##*/}"
	ex_dir="${filename%/*}"
	out_file="$ex_dir/db_dump/$db_file"

	echo "Downloading:"
	echo "-- url: $url"
	echo "-- saving as: $out_file"

	curl "$url" -o "$out_file" -s

	echo "OK."
done

echo "Done."