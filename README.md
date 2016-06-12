# **WELCOME TO TURBO-LASER**


## Introduction

Turbo-laser is a distributed, fault tolerant database processing architecture for Tika, GATE, Biolark and text deidentification, with JDBC and Elasticsearch export options. It makes use of the Spring Batch framework. In the parlance of the batch processing domain language (http://docs.spring.io/spring-batch/reference/html/domain.html), it uses the partitioning concept to create 'partition step' metadata for a DB table. This metadata is persisted in the Spring database schema, whereafter each partition can then be executed locally or farmed out remotely via a JMS middleware server (only ActiveMQ is suported at this time). Remote worker JVMs then retrieve metadata descriptions of work units. The outcome of processing is then persisted in the database, allowing robust tracking and simple restart of failed partitions.

## Why does this project exist/ why is batch processing difficult?

This project was developed as the central 'glue' to combine a variety of processes from a wider architecture known as the 'CogStack'. The CogStack is a range of technologies designed to to support modern, open source healthcare analytics within the NHS, and is chiefly comprised of the Elastic stack (elasticsearch, kibana etc), GATE and Biolark (clinical natural language processing for entity extraction), OCR, clinical text de-identification (Manchester De-ID and the ElasticGazetteer), and Apache Tika for MS Office to text conversion. When processing very large datasets (10s - 100s of millions rows of data), it is likely that some problems will present certain difficulties for different processes. These problems are typically hard to predict - for example, some documents may have very long sentences, an unusual sequence of characters, or machine only content. Such circumstances can create a range of problems for NLP algorithms, and thus a fault tolerant batch frameworks are required to ensure robust, consistent processing.

## Example usage

The entire process is run through the command line, taking a path to a directory containing config files as a single argument. These config files selectively activate Spring profiles as required to perform required data selection, processing and output writing steps.

Examples of config file are in the exampleConfigs dir. Most are relatively self explanatory, or should be annotated to explain their meaning.


example configs can be generated from the gradle task:

```
gradle writeExampleConfig
```

The behaviour of turbo-laser is configured by activating a variety of spring profiles as required. The current profiles are



processes
1. tika - process JDBC input with Tika. Extended with a custom PDF preprocessor to perform OCR on scanned PDF document. Extended with a custom PDF preprocessor to perform OCR on scanned PDF documents. (requires ImageMagick and Tesseract
2. gate - process JDBC input with a generic GATE app.
3. dBLineFixer - process JDBC input with dBLineFixer (concatenates multi-row documents)
4. basic - a job without a processing step, for simply writing JDBC input to elasticsearch
5. deid - deidentify text with a GATE application or using teh ElasticGazetteer
6. biolark - specify the endpoint for the Biolark application, Tudor Groza's awesome HPO term extraction project.

scaling
1. localPartitioning - run all processes within the launching JVM
2. remotePartitioning - send partitions to JMS middleware, to be picked up by remote hosts (see below)

outputs
1. elasticsearch - write to an elasticsearch cluster
2. jdbc - write to a JDBC endpoint



## Scheduling
Turbo-laser also offers a built in scheduler, to process changes in a database between job runs (requires a timestamp in the source database)

> set useScheduling to true

run intervals are handled with the following CRON like syntax
```
scheduler.rate = "*/5 * * * * *"
```


## Logging support

Turbo-laser uses the SLF4J abstraction for logging, with logback as the concrete implementation. To name a logfile, simply add the -DLOG_FILE_NAME system flag when launching the JVM

e.g.


```
java -DLOG_FILE_NAME=aTestLog -DLOG_LEVEL=debug -jar turbo-laser-0.3.0.jar /my/path/to/configs
```


Turbo-laser assumes the job repository schema is already in place in the DB implementation of your choice (see spring batch docs for more details)


## Scaling

To add additional JVM processes, whether locally or remotely (via the magic of Spring Integration), just launch an instance with the same config files but with useScheduling = slave. You'll need an ActiveMQ server to co-ordinate the nodes (see config example for details)

That's it! If a job fails, any uncompleted partitions will be picked up by the next run. If a Job ends up in an unknown state (e.g. due to hardware failure), the next run will mark it as abandonded and recommence from the last successful job it can find in the repository.

Questions? Want to help? Drop me a message!
