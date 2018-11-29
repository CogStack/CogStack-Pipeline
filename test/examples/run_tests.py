#!/usr/bin/python

import sys
import os

from examples_tests import *


def print_usage():
    print "usage: python run_test.py [path_to_examples_dir]"


def run_all(examples_main_path):
    """
    Helper function to run all the test cases
    :arg: examples_main_path: the path to main examples directory
    """
    # test cases to run
    test_cases = [TestExample1,
                  TestExample2,
                  TestExample3,
                  TestExample4,
                  TestExample5s1,
                  TestExample5s2,
                  TestExample6,
                  TestExample7,
                  TestExample8,
                  TestExample9]

    # load all the specified test cases
    test_loader = unittest.TestLoader()
    suite = unittest.TestSuite()

    for test_case in test_cases:
        test_names = test_loader.getTestCaseNames(test_case)

        for test_name in test_names:
            suite.addTest(test_case(methodName=test_name,
                                    examples_path=examples_main_path))

    # run the test suite
    result = unittest.TextTestRunner().run(suite)
    return int(not result.wasSuccessful())


if __name__ == '__main__':
    if len(sys.argv) < 1:
        print_usage()
        exit(0)

    # parse the examples path
    if len(sys.argv) > 1:
        examples_path = os.path.normpath(sys.argv[1])
    else:
        rel_path = "../../examples"
        cur_dir = os.path.dirname(os.path.realpath(__file__))
        examples_path = os.path.normpath(os.path.join(cur_dir, rel_path))

    # run the tests and return the result
    exit_code = run_all(examples_path)
    exit(exit_code)
