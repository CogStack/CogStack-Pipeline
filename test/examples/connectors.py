#!/usr/bin/python

import psycopg2
import elasticsearch


class JdbcConnectorConfig:
    """
    JDBC PostgreSQL connector configuration
    """
    def __init__(self, host, port, db_name, user_name="", user_pass=""):
        """
        :param host: the host to connect to
        :param port: the host's port
        :param db_name: the name of the database to connect to
        :param user_name: the user name in case of required authentication
        :param user_pass: the user's password
        """
        self.host = host
        self.port = port
        self.db_name = db_name
        self.user_name = user_name
        self.user_pass = user_pass


class JdbcConnector:
    """
    JDBC PostgreSQL connector
    """
    def __init__(self, conn_conf):
        """
        :param conn_conf: JDBC configuration :class:`~JdbcConnectorConfig`
        """
        if len(conn_conf.user_name) > 0:
            conn_string = "host='{host}' port='{port}' dbname='{db_name}' user='{user_name}' password='{user_pass}'"
            self.connector = psycopg2.connect(conn_string.format(host=conn_conf.host,
                                                                 port=conn_conf.port,
                                                                 db_name=conn_conf.db_name,
                                                                 user_name=conn_conf.user_name,
                                                                 user_pass=conn_conf.user_pass))
        else:
            conn_string = "host='{host}' port='{port}' dbname='{db_name}'"
            self.connector = psycopg2.connect(conn_string.format(host=conn_conf.host,
                                                                 port=conn_conf.port,
                                                                 db_name=conn_conf.db_name))


class ElasticConnectorConfig:
    """
    ElasticSearch connector configuration
    At the moment supports only single-node clusters
    """
    def __init__(self, host, port, http_auth_user="", http_auth_pass=""):
        """
        :param host: the ElasticSearch host name
        :param port: the host's port
        :param http_auth_user: the user name in case of required HTTP/HTTPS authentication
        :param http_auth_pass: the user's password
        """
        self.host = host
        self.port = port
        self.http_user = http_auth_user
        self.http_pass = http_auth_pass


class ElasticConnector:
    """
    ElasticSearch connector
    At the moment supports only single-node clusters
    """
    def __init__(self, elastic_conf):
        """
        :param elastic_conf: ElasticSearch configuration :class:`~ElasticConnectorConfig`
        """
        if len(elastic_conf.http_user) > 0:
            http_auth = "{user}:{password}".format(user=elastic_conf.http_user, password=elastic_conf.http_pass)
            self.connector = elasticsearch.Elasticsearch(hosts=[{'host': elastic_conf.host, 'port': elastic_conf.port}],
                                                         http_auth=http_auth)
        else:
            self.connector = elasticsearch.Elasticsearch(hosts=[{'host': elastic_conf.host, 'port': elastic_conf.port}])
