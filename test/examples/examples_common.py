#!/usr/bin/python

import unittest
import os
import logging
import subprocess
import time
import yaml

from connectors import *


class TestSingleExample(unittest.TestCase):
    """
    A common base class for the examples test cases
    """
    def __init__(self,
                 example_path,
                 sub_case="",
                 use_local_image_build=True,
                 image_build_rel_dir="../../../",
                 *args, **kwargs):
        """
        :param example_path: the absolute patch to the examples main directory
        :param sub_case: the specific sub case to test
        :param use_local_image_build: whether to use a locally build CogStack Pipeline image
        :param image_build_rel_dir: the relative directory where the image Dockerfile is located
        :param args: any additional arguments passed on to the parent class
        :param kwargs: any additional arguments passed on to the parent class
        """
        super(TestSingleExample, self).__init__(*args, **kwargs)

        # set paths and directories info
        self.example_path = example_path
        self.sub_case = sub_case
        self.deploy_dir = '__deploy'
        self.use_local_image_build = use_local_image_build
        self.image_build_rel_dir = image_build_rel_dir
        self.deploy_path = os.path.join(self.example_path, self.deploy_dir)
        if len(self.sub_case) > 0:
            self.deploy_path = os.path.join(self.deploy_path, self.sub_case)
            self.image_build_rel_dir += "../"

        # set commands
        self.setup_cmd = 'bash setup.sh'
        self.docker_cmd_up = 'docker-compose up -d'  # --detach
        self.docker_cmd_down = 'docker-compose down -v'  # --volumes

        # set up logger
        log_format = '[%(asctime)s] %(name)s: %(message)s'
        logging.basicConfig(format=log_format, level=logging.INFO)
        self.log = logging.getLogger(self.__class__.__name__)

    @staticmethod
    def getRecordsCountFromSourceDb(connector, table_name):
        """
        Queries the table for the number of records
        in the database specified by the connector
        :param connector: the database connector :class:~JdbcConnector
        :param table_name: the name of the table to query
        :return: the number of records
        """
        cursor = connector.cursor()
        cursor.execute("SELECT COUNT(*) FROM %s" % table_name)
        res = cursor.fetchall()
        # res is a list of tuples
        return int(res[0][0])

    @staticmethod
    def getRecordsCountFromTargetEs(connector, index_name):
        """
        Queries the index for the number of documents (_count)
        :param connector: the ElasticSearch connector :class:~ElasticSearchConnector
        :param index_name: the name of the index to query
        :return: the number of records
        """
        res = connector.count(index_name)
        return int(res['count'])

    def addBuildContextInComposeFile(self):
        """
        Add the build context key in the Docker Compose file
        to be using a locally build image
        """
        compose_file = os.path.join(self.deploy_path, "docker-compose.override.yml")
        with open(compose_file, 'r') as c_file:
            compose_yaml = yaml.safe_load(c_file)

        # check whether the service key exists and add the build context
        if 'cogstack-pipeline' not in compose_yaml['services']:
            compose_yaml['services']['cogstack-pipeline'] = dict()
        compose_yaml['services']['cogstack-pipeline']['build'] = self.image_build_rel_dir

        # save the file in-place
        with open(compose_file, 'w') as c_file:
            yaml.dump(compose_yaml, c_file, default_flow_style=False)

    def setUp(self):
        """
        Runs test case set up function
        """
        # run setup for the example
        self.log.info("Setting up ...")
        try:
            out = subprocess.check_output(self.setup_cmd, cwd=self.example_path, shell=True)
            if len(out) > 0:
                self.log.debug(out)
        except Exception as e:
            self.log.error("Failed to setup example: %s" % e)
            if hasattr(e, 'output'):
                self.log.error("Output: %s" % e.output)
            self.fail(e.message)

        # replace the image to local build
        if self.use_local_image_build:
            try:
                self.addBuildContextInComposeFile()
            except Exception as e:
                self.log.error("Failed to add the local build context: %s" % e)
                self.fail(e.message)

        # run docker-compose
        self.log.info("Starting the services ...")
        try:
            out = subprocess.check_output(self.docker_cmd_up, cwd=self.deploy_path, stderr=subprocess.STDOUT, shell=True)
            if len(out) > 0:
                self.log.debug(out)
        except Exception as e:
            # clean up
            try:
                out = subprocess.check_output(self.docker_cmd_down, stderr=subprocess.STDOUT, cwd=self.deploy_path, shell=True)
                if len(out) > 0:
                    self.log.debug(out)
            except Exception as ee:
                self.log.warn("Failed to stop services: %s" % ee)

            self.log.error("Failed to start services: %s" % e)
            self.fail(e.message)

    def tearDown(self):
        """"
        Runs test case tear down function
        """
        # run docker-compose
        self.log.info("Stopping the services ...")
        try:
            out = subprocess.check_output(self.docker_cmd_down, cwd=self.deploy_path, stderr=subprocess.STDOUT, shell=True)
            if len(out) > 0:
                self.log.debug(out)
        except Exception as e:
            self.log.warn("Failed to stop services: %s " % e)

        # clean up the directory
        self.log.info("Cleaning up ...")
        main_deploy_path = os.path.join(self.example_path, self.deploy_dir)
        try:
            out = subprocess.check_output('rm -rf %s' % main_deploy_path, shell=True)
            if len(out) > 0:
                self.log.debug(out)
        except Exception as e:
            self.log.warn("Failed to clean up: %s" % e)


class TestSingleExampleDb2Es(TestSingleExample):
    """
    A common base class for examples reading the records from a single database source
    and storing them in ElasticSearch sink
    """
    def __init__(self, source_conn_conf, source_table_name, es_conn_conf, es_index_name,
                 wait_for_source_ready_s=10,
                 wait_for_sink_ready_s=60,
                 *args, **kwargs):
        """
        :param source_conn_conf: the source JDBC connector configuration :class:~JdbcConnectorConfig
        :param source_table_name: the source database table name
        :param es_conn_conf: the sink ElasticSearch connector configuration :class:~ElasticConnectorConfig
        :param es_index_name: the sink ElasticSearch index name
        :param wait_for_source_ready_s: delay [in s] to wait until source is ready to query
        :param wait_for_sink_ready_s: delay [in s] to wait until the sink (and data) becomes ready to query
        :param args: any additional arguments passed on to the parent class
        :param kwargs: any additional arguments passed on to the parent class
        """
        super(TestSingleExampleDb2Es, self).__init__(*args, **kwargs)
        self.source_conn_conf = source_conn_conf
        self.source_table_name = source_table_name
        self.es_conn_conf = es_conn_conf
        self.es_index_name = es_index_name

        self.wait_for_souce_ready_s = wait_for_source_ready_s
        self.wait_for_sink_ready_s = wait_for_sink_ready_s

    def test_source_sink_mapping(self):
        """"
        Runs a simple test verifying the number of records in the source and the sink
        """
        # wait here until DBs become ready
        self.log.info("Waiting for source/sink to become ready ...")
        time.sleep(self.wait_for_souce_ready_s)

        source_conn = JdbcConnector(self.source_conn_conf)
        es_conn = ElasticConnector(self.es_conn_conf)

        # wait here until ES becomes ready
        self.log.info("Waiting for cogstack pipeline to process records ...")
        time.sleep(self.wait_for_sink_ready_s)
        recs_in = self.getRecordsCountFromSourceDb(source_conn.connector, self.source_table_name)
        recs_out = self.getRecordsCountFromTargetEs(es_conn.connector, self.es_index_name)

        self.assertEqual(recs_in, recs_out, "Records counts differ between source (%s) and sink (%s)." % (recs_in, recs_out))


class TestSingleExampleDb2Db(TestSingleExample):
    """
    A common base class for examples reading the records from a single database source
    and storing them in the same or another database sink
    """
    def __init__(self, source_conn_conf, source_table_name, sink_conn_conf, sink_table_name,
                 wait_for_source_ready_s=10,
                 wait_for_sink_ready_s=60,
                 *args, **kwargs):
        """
        :param source_conn_conf: the source JDBC connector configuration :class:~JdbcConnectorConfig
        :param source_table_name: the source database table name
        :param sink_conn_conf: the sink JDBC connector configuration :class:~JdbcConnectorConfig
        :param sink_table_name: the sink database table name
        :param wait_for_source_ready_s: delay [in s] to wait until source is ready to query
        :param wait_for_sink_ready_s: delay [in s] to wait until the sink (and data) becomes ready to query
        :param args: any additional arguments passed on to the parent class
        :param kwargs: any additional arguments passed on to the parent class
        """
        super(TestSingleExampleDb2Db, self).__init__(*args, **kwargs)
        self.source_conn_conf = source_conn_conf
        self.source_table_name = source_table_name
        self.sink_conn_conf = sink_conn_conf
        self.sink_table_name = sink_table_name

        self.wait_for_souce_ready_s = wait_for_source_ready_s
        self.wait_for_sink_ready_s = wait_for_sink_ready_s

    def test_source_sink_mapping(self):
        """"
        Runs a simple test verifying the number of records in the source and the sink
        """
        # wait here until DBs become ready
        self.log.info("Waiting for source/sink to become ready ...")
        time.sleep(self.wait_for_souce_ready_s)

        source_conn = JdbcConnector(self.source_conn_conf)
        sink_conn = JdbcConnector(self.sink_conn_conf)

        # wait here until sink becomes ready
        self.log.info("Waiting for cogstack pipeline to process records ...")
        time.sleep(self.wait_for_sink_ready_s)

        recs_in = self.getRecordsCountFromSourceDb(source_conn.connector, self.source_table_name)
        recs_out = self.getRecordsCountFromSourceDb(sink_conn.connector, self.sink_table_name)

        self.assertEqual(recs_in, recs_out, "Records counts differ between source (%s) and sink (%s)." % (recs_in, recs_out))
