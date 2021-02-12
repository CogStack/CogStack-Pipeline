#!/bin/bash
set -e

# output model files
MEDMEN_DIR=./medmen
MODEL_CDB=$MEDMEN_DIR/cdb.dat
MODEL_VCB=$MEDMEN_DIR/vocab.dat
MODEL_META=$MEDMEN_DIR/mc_status

if [[ ! -f "$MODEL_CDB"  || ! -f "$MODEL_VCB" ]]; then
  echo "Downloading model: MedMentions"
  if [[ ! -d $MEDMEN_DIR ]]; then
    mkdir $MEDMEN_DIR
  fi
  # download the model as described in the MedCAT repo
  curl https://s3-eu-west-1.amazonaws.com/zkcl/vocab.dat > $MODEL_VCB
  curl https://s3-eu-west-1.amazonaws.com/zkcl/cdb-medmen.dat > $MODEL_CDB
else
  echo "MedMentions model already present -- skipping download"
fi

if [[ ! -d "$MODEL_META" ]]; then
  echo "Downloading meta model: status"
  curl https://zkcl.s3-eu-west-1.amazonaws.com/mc_status.zip > $MEDMEN_DIR/mc_status.zip && \
    (cd $MEDMEN_DIR && unzip mc_status.zip) && \
    rm $MEDMEN_DIR/mc_status.zip
else
  echo "Meta model already present -- skipping download"
fi
