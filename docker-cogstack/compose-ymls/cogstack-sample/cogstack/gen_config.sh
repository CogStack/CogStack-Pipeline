#!/bin/bash

CONF_DIR="conf"
TEMPLATE="template.properties"


if [ ! -d "$CONF_DIR" ]; then
	mkdir $CONF_DIR
fi


echo "Generating properites files for CogStack for each available view (synthetic data)"
available_views=(
	observations
	procedures
	allergies
	careplans
	conditions
	imaging_studies
	immunizations
	medications
	)

for f in ${available_views[@]}; do
	conf_file="${f}.properties"
	view_name="${f}"_view
	echo "-- Creating fie: $conf_file for $view_name"
	cat $TEMPLATE | sed "s/@VIEW_NAME@/$view_name/g" > $CONF_DIR/$conf_file
done


# copy the MTSamples properties file
#
echo "Copying the MTSamples properties file"
MT_FILE="mt.properties"
cp $MT_FILE $CONF_DIR/ 
