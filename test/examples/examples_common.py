#!/usr/bin/python

import unittest
import os
import logging
import subprocess
import time

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
                 image_suffix="",
                 uses_image_override=False,
                 *args, **kwargs):
        """
        :param example_path: the absolute patch to the examples main directory
        :param sub_case: the specific sub case to test
        :param use_local_image_build: whether to use a locally build CogStack Pipeline image
        :param image_build_rel_dir: the relative directory where the image Dockerfile is located
        :param image_suffix: the suffix of the image to be used
        :param uses_image_override: whether use the modified Docker Compose override file
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
        self.image_suffix = image_suffix
        self.uses_image_override = uses_image_override

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

    def replaceImageNameInComposeFile(self):
        """
        Replaces the name of the image in the Docker Compose file
        to be using a local build in the deployment
        """
        # check whether we use standard compose file or override
        compose_file = "docker-compose.yml"
        if self.uses_image_override:
            compose_file = "docker-compose.override.yml"
        compose_file = os.path.join(self.deploy_path, compose_file)

        # set the replace target string
        to_replace = ['image: cogstacksystems/cogstack-pipeline:latest',
                      'image: cogstacksystems/cogstack-pipeline:dev-latest',
                      'image: cogstacksystems/cogstack-pipeline:local']
        if len(self.image_suffix) > 0:
            to_replace = [n.replace(':', '%s:' % self.image_suffix) for n in to_replace]

        # read file to memory
        with open(compose_file, 'r') as f_in:
            file_content = f_in.read()

        # replace the image to build string
        target = 'build: %s' % self.image_build_rel_dir
        for s in to_replace:
            if s in file_content:
                file_content = file_content.replace(s, target)

        # write the file out
        with open(compose_file, 'w') as f_out:
            f_out.write(file_content)

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
                self.replaceImageNameInComposeFile()
            except Exception as e:
                self.log.error("Failed to replace image string to the local build: %s" % e)
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
            out = subprocess.check_output(['rm', '-rf', main_deploy_path])
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
