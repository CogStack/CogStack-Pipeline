#!/bin/bash

set -e

EXAMPLES_REL_PATH=../../examples/

# assumes that the examples database dumps are already downloaded
# bash $EXAMPLES_REL_PATH/download_db_dumps.sh

# assumes that python virtualenv module is installed
# pip install virtualenv

# create a python virtual environment
python -m virtualenv test-env
source test-env/bin/activate

# install the necessary requirements
pip install -r requirements.txt

# run tests
python run_tests.py

# finish
deactivate
