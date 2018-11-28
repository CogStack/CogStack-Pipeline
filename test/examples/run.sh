#!/bin/bash
set -e


# global variables
#
EXAMPLES_REL_PATH=../../examples
DOWNLOAD_SCRIPT=download_db_dumps.sh



# parse the input args
#
for i in "$@"; do
    case $i in
        --examples-path=*)
        EXAMPLES_PATH="${i#*=}"
        ;;
        --download-dumps)
        DOWNLOAD_DUMPS=1
        ;;
        *)
        # unknown option
        ;;
    esac
done



# check the requirements
#
echo "Checking the requirements ..."

# assumes that python virtualenv module is installed
# otherwise: pip install virtualenv
python -c "import virtualenv"
if [ "$?" -ne "0" ]; then
    echo "Error: Python virtualenv module not available"
    exit 1
fi

# check whether the example path is valid
if [ -z $EXAMPLES_PATH ]; then
    EXAMPLES_PATH=$EXAMPLES_REL_PATH
fi

if [ ! -e $EXAMPLES_PATH/$DOWNLOAD_SCRIPT ]; then
    echo "Error: invalid examples path"
    echo "(the script to download database dumps does not exist: $EXAMPLES_PATH/$DOWNLOAD_SCRIPT)"
    exit 1
fi

# do we need to download database dumps?
if [ ! -z $DOWNLOAD_DUMPS ]; then
    echo "Downloading examples database dumps ..."
    ( cd $EXAMPLES_PATH && bash $DOWNLOAD_SCRIPT )
fi
# otherwise: assumes that the examples database dumps are already downloaded



# proceed with the tests
#
echo "Starting the tests ..."

# create a python virtual environment
python -m virtualenv test-env
source test-env/bin/activate

# install the necessary requirements
pip install -r requirements.txt

# run tests
python run_tests.py

if [ "$?" -ne "0" ]; then
    echo "Error: one or more tests failed"
    exit 1
fi

# finish
deactivate
