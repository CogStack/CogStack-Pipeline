# **WELCOME TO Cogstack**


## Introduction

Cogstack is a distributed, fault tolerant database processing architecture for Tika, GATE, Biolark and text deidentification,
 with JDBC and Elasticsearch export options. It makes use of the Spring Batch framework in order to provide a fully configurable
 pipeline with the goal of generating a JSON that can be readily indexed into elasticsearch.
 In the parlance of the batch processing [domain language](http://docs.spring.io/spring-batch/reference/html/domain.html),
 it uses the partitioning concept to create 'partition step' metadata for a DB table. This metadata is persisted in the
 Spring database schema, whereafter each partition can then be executed locally or farmed out remotely via a JMS middleware
 server (only ActiveMQ is suported at this time). Remote worker JVMs then retrieve metadata descriptions of work units.
 The outcome of processing is then persisted in the database, allowing robust tracking and simple restart of failed partitions.

## Why does this project exist/ why is batch processing difficult?

This project was developed as the central 'glue' to combine a variety of processes from a wider architecture known as the 'CogStack'.
The CogStack is a range of technologies designed to to support modern, open source healthcare analytics within the NHS, and is
chiefly comprised of the Elastic stack (elasticsearch, kibana etc), GATE and Biolark (clinical natural language processing for
entity extraction), OCR, clinical text de-identification (Manchester De-ID and the ElasticGazetteer), and Apache Tika for MS
Office to text conversion. When processing very large datasets (10s - 100s of millions rows of data), it is likely that some
problems will present certain difficulties for different processes. These problems are typically hard to predict - for example,
some documents may have very long sentences, an unusual sequence of characters, or machine only content. Such circumstances can
create a range of problems for NLP algorithms, and thus a fault tolerant batch frameworks are required to ensure robust, consistent
processing.

## Installation

We're not quite at a regular release cycle yet, so if you want a stable version, I suggest downloading v 1.0.0 from the release
 page. However, if you want more features and (potentially) fewer bugs, it's best to build from source on the master branch.

To build from source:

 1. Install [Tesseract](https://github.com/tesseract-ocr/tesseract) and [Imagemagick](https://github.com/ImageMagick/ImageMagick)
 (can be installed but apt-get on Ubuntu)
 2. Run the following:

```
gradlew clean build
```

## Quick Start Guide

The absolute easiest way to get up and running with CogStack is to use [Docker](https://www.docker.com/). Docker can provide
lightweight virtualisation of a variety of microservices that CogStack makes use of. When coupled with the microservice orchestration
[docker compose](https://docs.docker.com/compose/) technology, all of the components required to use CogStack can be set up with a few
simple commands.

First, ensure you have docker v1.13 or above installed.

Elasticsearch in docker requires the following to be set on the host:

```
sudo sysctl -w vm.max_map_count=262144
```


Now you need to build the required docker containers. Fortunately, the gradle build file can do this for you.

From the CogStack top level directory:

```
  gradlew buildSimpleContainers
```

Assuming the containers have been built successfully, simply navigate to
```
cd docker-cogstack/compose-ymls/simple/
```


And type
```
docker-compose up
```

All of the docker containers should be up and communicating with each other. You can view their status with
```
docker ps -a
```


That's it!

"But that's what?", I hear you ask?

The high level workflow of CogStack is as follows:

* Read a row of the table into the CogStack software
* Process the columns of the row with inbuilt Processors
* Construct a JSON that represents the table row and new data arising from the webservice
* Index the JSON into an elasticsearch cluster
* Visualise the results with Kibana

To understand what's going on, we need to delve into what each of the components is doing. Let's start with the container called
'some-postgres'. Let's assume this is a database that contains a table that we want to process somehow. In fact this example database already
contains some example data. If you have some database browsing software, you should be able to connect to it with the following JDBC confguration

```
source.JdbcPath      = jdbc:postgresql://localhost:5432/cogstack
source.Driver        = org.postgresql.Driver
source.username      = cogstack
source.password      = mysecretpassword
```

You should see a table called 'tblinputdocs' in the 'cogstack' database with four lines of dummy data. This table is now constantly
 being scanned and indexed into elasticsearch. If you know how to use the [Kibana](https://www.elastic.co/products/kibana) tool,
 you can visualise the data in the cluster.

Now bring the compose configuration down with from the same compose directory as before:

```
docker-compose down
```


This is the most basic configuration, and really doesn't do too much other than convert a database table/view into an elasticsearch index.
For more advanced use cases/configurations, check out the integration test below.



## Integration Tests

Although cogstack has unit tests where appropriate, the nature of the project is such that the real value fo testing comes
 from the integration tests. Consequently, cogstack has an extensive suite.

To run the integration tests, ensure the required external services are available
 (which also give a good idea of how cogstack is configured). These services are Postgresql, Biolark, Bioyodie and Elasticsearch.  The easiest
 way to get these going is with [Docker](https://www.docker.com/). Once you have docker installed, cogstack handily will
 build the containers you need for you (apart from elasticsearch, where the official image will suffice). To build the containers:

From the CogStack top level directory:

```
  gradlew buildAllContainers
```



Note, Biolark and Bioyodie are external applications. Building their containers (and subsequently running their integration tests) may require you to
  meet their licencing conditions. Please check with [Tudor Groza](t.groza@garvan.org.au) (Biolark) and [Angus Roberts](angus.roberts@sheffield.ac.uk)/[Genevieve Gorrell](g.gorrell@sheffield.ac.uk) if in doubt.

Assuming the containers have been built successfully, navigate to
```
cd docker-cogstack/compose-ymls/nlp/
```
And type
```
docker-compose up
```

to launch all of the external services.


All being well, you should now be able to run the integration tests. Each of these demonstrate a different facet of cogstack's functionality.
Each integration test follows the same pattern:

* Generate some dummy data for processing, by using an integration test execution listener
* Activate a configuration appropriate for the data and run cogstack
* Verify results

All integration tests can be run by using:

```
gradlew integTest
```

Although if you're new to cogstack, you might find it more informative to run them individually, and inspect the results after each one. For example,
  to run a single test:
```
gradlew  -DintegTest.single=<integration test class name> -i integTest
```
Available classes for integration tests are in the package
```
src/integration-test/java/uk/ac/kcl/it
```

For example, to load the postgres database with some dummy word files into a database table called <tblInputDocs>, process them with Tika, and load them into ElasticSearch index called <test_index2> and a postgres table called <tblOutputDocs>

```
gradlew  -DintegTest.single=TikaPKPartitionWithoutScheduling -i integTest
```

then point your browser to localhost:5601

### A note on GATE

Applications that require GATE generally need to be configured to point to the GATE installation directory (or they would need to include a rather large amount of plugins on their classpath). To do this in cogstack, set the appropriate properties as detailed in gate.* .

## Example usage in real world deployments

The entire process is run through the command line, taking a path to a directory as a single argument. This directory should contain configuration files, (one complete one per spring batch job that you want to run simultaneously). These config files selectively activate Spring profiles as required to perform required data selection, processing and output writing steps.

Examples of config file are in the exampleConfigs dir. Most are (hopefully) relatively self explanatory, or should be annotated to explain their meaning.

example configs can be generated from the gradle task:

```
gradlew writeExampleConfig
```

The behaviour of cogstack is configured by activating a variety of spring profiles (again, in the config files - see examples) as required. Currently. the available profiles are

inputs
 1. jdbc_in - Spring Batch's JdbcPagingItemReader for reading from a database table or view. Also requires a partitioning profile to be activated, to set a partitioning strategy. If you don't know what you're doing, just use the primaryKeyPartition profile.
 2. docmanReader - a custom reader for system that stores files in a file system, but holds their path in a database. Weird...

processes

 1. tika - process JDBC input with Tika. Extended with a custom PDF preprocessor to perform OCR on scanned PDF document.  (requires ImageMagick and Tesseract on the PATH)
 2. gate - process JDBC input with a generic GATE app.
 3. dBLineFixer - process JDBC input with dBLineFixer (concatenates multi-row documents)
 4. basic - a job without a processing step, for simply writing JDBC input to elasticsearch
 5. deid - deidentify text with a GATE application (such as the [Healtex texscrubber](https://github.com/healtex/texscrubber)) or using the Cognition algorithm, which queries a database for identifiers and mask them in free text using Levenstein distance.
 6. webservice - send a document to a webservice (such as an NLP REST service, like bioyodie/biolark) for annotation. The response should be a JSON, so it can be mapped to Elasticsearch's 'nested' type.

scaling
 1. localPartitioning - run all processes within the launching JVM
 2. remotePartitioning - send partitions to JMS middleware, to be picked up by remote hosts (see below)

outputs
 1. elasticsearch - write to an elasticsearch cluster
 2. jdbc_out - write the generated JSON to a JDBC endpoint. Useful if the selected processes are particularly heavy (e.g. biolark), so that data can be reindexed without the need for reprocessing

partitioning
 1. primaryKeyPartition - process all records based upon partitioning of the primary key
 2. primaryKeyAndTimeStampPartition - process all records based upon partitioning of the primary key and the timestamp, for finer control/ smaller batch sizes per job. Use the processingPeriod property to specify the number of milliseconds to 'scan' ahead for each job run

## Scheduling
cogstack also offers a built in scheduler, to process changes in a database between job runs (requires a timestamp in the source database)
```
useScheduling = true
```
run intervals are handled with the following CRON like syntax
```
scheduler.rate = "*/5 * * * * *"
```


## Logging support

cogstack uses the SLF4J abstraction for logging, with logback as the concrete implementation. To name a logfile, simply add the -DLOG_FILE_NAME system flag when launching the JVM

e.g.

```
java -DLOG_FILE_NAME=aTestLog -DLOG_LEVEL=debug -jar cogstack-0.3.0.jar /my/path/to/configs
```

cogstack assumes the 'job repository' schema is already in place in the DB implementation of your choice (see spring batch docs for more details). The scripts to set this up for various vendors can be found [here](https://github.com/spring-projects/spring-batch/tree/master/spring-batch-core/src/main/resources/org/springframework/batch/core)

## Scaling

To add additional JVM processes, whether locally or remotely (via the magic of Spring Integration), just launch an instance with the same config files but with useScheduling = slave. You'll need an ActiveMQ server to co-ordinate the nodes (see config example for details)

If a job fails, any uncompleted partitions will be picked up by the next run. If a Job ends up in an unknown state (e.g. due to hardware failure), the next run will mark it as abandonded and recommence from the last successful job it can find in the repository.

## JDBC output/reindexing

Using the JDBC output profile, it is possible to generate a column of JSON strings back into a database. This is useful for reindexing large quantities of data without the need to re-process with the more computationally expensive item processors (e.g. OCR, biolark). To reindex, simply use the reindexColumn in the configuration file. Note, if you include other profiles, these will still run, but will not contribute to the final JSON, and are thus pointless. Therefore, only the 'basic' profile should be used when reindexing data.
```
reindex = true

#select the column name of jsons in the db table
reindexField = sometext
```
## History

This project is an ‘evolution’ of an earlier KHP-Informatics project I was involved with called [Cognition](https://github.com/KHP-Informatics/Cognition-DNC). Although Cognition had an excellent implementation of Levenstein distance for string substitution (thanks [iemre](https://github.com/iemre)!), the architecture of the code suffered some design flaws, such as an overly complex domain model and configuration, and lack of fault tolerance/job stop/start/retry logic. As such, it was somewhat difficult to work with in production, and hard to extend with new features. It was clear that there was the need for a proper batch processing framework. Enter Spring Batch and a completely rebuilt codebase, save a couple of classes from the original Cognition project. cogstack is used at King's College Hospital and the South London and Maudsley Hospital to feed Elasticsearch clusters for business intelligence and research use cases

Some of the advancements in cogstack:

 1. A simple <String,Object> map, with a few pieces of database metadata for its [domain model](https://github.com/RichJackson/cogstack/blob/master/src/main/groovy/uk/ac/kcl/model/Document.groovy) (essentially mapping a database row to a elasticsearch document, with the ability to embed [nested types](https://www.elastic.co/guide/en/elasticsearch/reference/2.3/nested.html)
 2. A composite [item processor configuration](https://github.com/RichJackson/cogstack/blob/master/src/main/java/uk/ac/kcl/itemHandlers/ItemHandlers.java), that can be easily extended and combined with other processing use case
 3. Complete, sensible coverage of stop, start, retry, abandon logic
 4. A custom socket timeout factory, to manage network failures, which can cause JDBC driver implementations to lock up, when the standard isn't fully implemented. Check out [this blog post](https://social.msdn.microsoft.com/Forums/office/en-US/3373d40a-2a0b-4fe4-b6e8-46f2988debf8/any-plans-to-add-socket-timeout-option-in-jdbc-driver?forum=sqldataaccess) for info.
 5. The ability to run multiple batch jobs (i.e. process multiple database tables within a single JVM, each having its own Spring container
 6. Remote partitioning via an ActiveMQ JMS server, for complete scalability
 7. Built in job scheduler to enable near real time synchronisation with a database

Questions? Want to help? Drop me a [message](mailto:richgjackson@gmail.com)!
