#!/bin/bash
set -e

AWS_S3_BUCKET_URL="http://cogstack.s3.amazonaws.com"
DATA_PATH="share/examples/db_dump"
DATA_PATH_URL="$AWS_S3_BUCKET_URL/$DATA_PATH"

# download the db dumps for examples
#
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

	if [ ! -e $ex_dir/db_dump ]; then mkdir $ex_dir/db_dump; fi

	echo "Downloading:"
	echo "-- url: $url"
	echo "-- saving as: $out_file"

	curl "$url" -o "$out_file" -s

	echo "OK."
done


# link the downloaded database dumps
#
if [ ! -e example5/db_dump ]; then mkdir example5/db_dump; fi
ln -s $PWD/example4/db_dump/db_samples-*.sql.gz example5/db_dump/

if [ ! -e example6/db_dump ]; then mkdir example6/db_dump; fi
ln -s $PWD/example2/db_dump/db_samples.sql.gz example6/db_dump/

if [ ! -e example7/db_dump ]; then mkdir example7/db_dump; fi
ln -s $PWD/example2/db_dump/db_samples.sql.gz example7/db_dump/

if [ ! -e example8/db_dump ]; then mkdir example8/db_dump; fi
ln -s $PWD/example2/db_dump/db_samples.sql.gz example8/db_dump/

# use only pdf-text DB dump for example 9
if [ ! -e example9/db_dump ]; then mkdir example9/db_dump; fi
ln -s $PWD/example4/db_dump/db_samples-pdf-text-small.sql.gz example9/db_dump/db_samples.sql.gz

echo "Done."
