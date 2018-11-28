#!/usr/bin/python

from examples_common import *
from connectors import *

# default configuration
#
DEFAULT_JDBC_CONFIG = JdbcConnectorConfig(host='localhost',
                                          port='5555',
                                          db_name='db_samples',
                                          user_name='test',
                                          user_pass='test')
DEFAULT_ES_CONFIG = ElasticConnectorConfig(host='localhost',
                                           port=9200)


# individual examples
#
class TestExample1(TestSingleExampleDb2Es):
    """
    Example1 test case description:
    - ingest records from a single database source and store them in ElasticSearch
    """
    def __init__(self, examples_path, *args, **kwargs):
        super(TestExample1, self).__init__(source_conn_conf=DEFAULT_JDBC_CONFIG,
                                           source_table_name='observations_view',
                                           es_conn_conf=DEFAULT_ES_CONFIG,
                                           es_index_name='sample_observations_view',
                                           example_path=os.path.join(examples_path, "example1"),
                                           *args, **kwargs)


class TestExample2(TestSingleExampleDb2Es):
    """
    Example2 test case description:
    - ingest records from a single database source and store them in ElasticSearch
    - used in Quickstart
    """
    def __init__(self, examples_path, *args, **kwargs):
        super(TestExample2, self).__init__(source_conn_conf=DEFAULT_JDBC_CONFIG,
                                           source_table_name='observations_view',
                                           es_conn_conf=DEFAULT_ES_CONFIG,
                                           es_index_name='sample_observations_view',
                                           example_path=os.path.join(examples_path, "example2"),
                                           *args, **kwargs)


class TestExample3(TestSingleExampleDb2Es):
    """
    Example3 test case description:
    - ingest records from a single database source and store them in ElasticSearch (syntetic)
    - ingest records from a single database source and store them in ElasticSearch (mtsamples)
    """
    def __init__(self, examples_path, *args, **kwargs):
        super(TestExample3, self).__init__(source_conn_conf=DEFAULT_JDBC_CONFIG,
                                           source_table_name='observations_view',
                                           es_conn_conf=DEFAULT_ES_CONFIG,
                                           es_index_name='sample_observations_view',
                                           example_path=os.path.join(examples_path, "example3"),
                                           *args, **kwargs)

        self.source_conn_conf_mt = JdbcConnectorConfig(host='localhost',
                                                       port='5556',
                                                       db_name='db_samples',
                                                       user_name='test',
                                                       user_pass='test')
        self.source_table_name_mt = 'mtsamples'
        self.es_index_name_mt = 'sample_mt'

    def test_source_target_mapping_mt(self):
        # test the MTSamples mapping

        self.log.info("Waiting for source/sink to become ready ...")
        time.sleep(self.wait_for_souce_ready_s)
        source_conn_mt = JdbcConnector(self.source_conn_conf_mt)
        es_conn = ElasticConnector(self.es_conn_conf)

        self.log.info("Waiting for cogstack pipeline to process records ...")
        time.sleep(self.wait_for_sink_ready_s)
        recs_in = self.getRecordsCountFromSourceDb(source_conn_mt.connector, self.source_table_name_mt)
        recs_out = self.getRecordsCountFromTargetEs(es_conn.connector, self.es_index_name_mt)

        self.assertEqual(recs_out, recs_in,
                         "There are less MTSamples records stored in sink (%s) than in source (%s)." % (recs_out, recs_in))


class TestExample4(TestSingleExampleDb2Es):
    """
    Example4 test case description:
    - ingest records from a single database source, run through Tika and store them in ElasticSearch
    - using only 'docx' sub-case testing raw text extraction using Tika
    """
    def __init__(self, examples_path, *args, **kwargs):
        super(TestExample4, self).__init__(source_conn_conf=DEFAULT_JDBC_CONFIG,
                                           source_table_name='observations_view',
                                           es_conn_conf=DEFAULT_ES_CONFIG,
                                           es_index_name='sample_observations_view',
                                           example_path=os.path.join(examples_path, "example4"),
                                           sub_case='docx',
                                           wait_for_sink_ready_s=120,
                                           *args, **kwargs)


class TestExample5s1(TestSingleExampleDb2Db):
    """
    Example5-stage1 test case description:
    - ingest records from a single database source, run through Tika and store them in output database
    - using only 'docx' sub-case testing raw text extraction using Tika
    """
    def __init__(self, examples_path, *args, **kwargs):
        super(TestExample5s1, self).__init__(source_conn_conf=DEFAULT_JDBC_CONFIG,
                                             source_table_name='medical_reports',
                                             sink_conn_conf=DEFAULT_JDBC_CONFIG,
                                             sink_table_name='medical_reports_processed',
                                             example_path=os.path.join(examples_path, "example5"),
                                             sub_case='docx',
                                             wait_for_sink_ready_s=120,
                                             *args, **kwargs)


class TestExample5s2(TestSingleExampleDb2Es):
    """
    Example5-stage2 test case description:
    - ingest records from a single database source and store them in ElasticSearch
    """
    def __init__(self, examples_path, *args, **kwargs):
        super(TestExample5s2, self).__init__(source_conn_conf=DEFAULT_JDBC_CONFIG,
                                             source_table_name='observations_view',
                                             es_conn_conf=DEFAULT_ES_CONFIG,
                                             es_index_name='sample_observations_view',
                                             example_path=os.path.join(examples_path, "example5"),
                                             sub_case='docx',
                                             *args, **kwargs)


class TestExample6(TestSingleExampleDb2Es):
    def __init__(self, examples_path, *args, **kwargs):
        """
        Example6 test case description:
        - ingest records from a single database source and store them in ElasticSearch
        - using NGINX as a proxy
        """
        super(TestExample6, self).__init__(source_conn_conf=DEFAULT_JDBC_CONFIG,
                                           source_table_name='observations_view',
                                           es_conn_conf=ElasticConnectorConfig(host='localhost',
                                                                               port='9200',
                                                                               http_auth_pass='test',
                                                                               http_auth_user='test'),
                                           es_index_name='sample_observations_view',
                                           example_path=os.path.join(examples_path, "example6"),
                                           *args, **kwargs)


class TestExample7(TestSingleExampleDb2Es):
    def __init__(self, examples_path, *args, **kwargs):
        """
        Example7 test case description:
        - ingest records from a single database source and store them in ElasticSearch
        - using Fluentd logging driver
        - using NGINX as a proxy
        """
        super(TestExample7, self).__init__(source_conn_conf=DEFAULT_JDBC_CONFIG,
                                           source_table_name='observations_view',
                                           es_conn_conf=ElasticConnectorConfig(host='localhost',
                                                                               port='9200',
                                                                               http_auth_pass='test',
                                                                               http_auth_user='test'),
                                           es_index_name='sample_observations_view',
                                           example_path=os.path.join(examples_path, "example7"),
                                           *args, **kwargs)


class TestExample8(TestSingleExampleDb2Es):
    def __init__(self, examples_path, *args, **kwargs):
        """
        Example8 test case description:
        - ingest records from a single database source, run NLP and store them in ElasticSearch
        - running a custom GATE NLP application to extract annotations
        """
        super(TestExample8, self).__init__(source_conn_conf=DEFAULT_JDBC_CONFIG,
                                           source_table_name='observations_view',
                                           es_conn_conf=DEFAULT_ES_CONFIG,
                                           es_index_name='sample_observations_view',
                                           example_path=os.path.join(examples_path, "example8"),
                                           wait_for_sink_ready_s=180,
                                           image_build_rel_dir="../../../dockerfiles/gate",
                                           *args, **kwargs)


class TestExample9(TestSingleExampleDb2Es):
    def __init__(self, examples_path, *args, **kwargs):
        """
        Example9 test case description:
        - ingest records from a single database source, run Tika+NLP and store them in ElasticSearch
        - running Tika on the binary documents
        - running a custom GATE NLP application to extract annotations
        """
        super(TestExample9, self).__init__(source_conn_conf=DEFAULT_JDBC_CONFIG,
                                           source_table_name='observations_view',
                                           es_conn_conf=DEFAULT_ES_CONFIG,
                                           es_index_name='sample_observations_view',
                                           example_path=os.path.join(examples_path, "example9"),
                                           wait_for_sink_ready_s=180,
                                           image_build_rel_dir="../../../dockerfiles/gate",
                                           *args, **kwargs)
