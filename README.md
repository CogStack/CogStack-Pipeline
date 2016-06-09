# **WELCOME TO TURBO-LASER**


## Introduction

Turbo-laser is a distributed, fault tolerant batch processing architecture for Tika, GATE, Biolark, text deidentification and Elasticsearch using the Spring Batch framework. In the parlance of the batch processing domain language (http://docs.spring.io/spring-batch/reference/html/domain.html), it uses the remote partitioning method to create 'slave step' metadata for a DB table of documents. This metadata is persisted in the Spring database schema, and farmed out via a JMS middleware server. Remote worker JVM slaves then retrieve metadata descriptions of work units. The outcome of processing is then persisted in the database, allowing robust tracking of failed partitions.

## Why is batch processing in NLP difficult?

When processing very large natural corpora (10s - 100s of millions of documents), it is likely that some documents will present certain difficulties for NLP processes that are hard to predict. For example, some documents may have very long sentences, an unusual sequence of characters, or machine only content. Such circumstances can create a range of problems for NLP algorithms, and thus fault tolerant batch frameworks are required to handle such edge cases.

## Example usage

The entire process is configured a directory containing config files, which is supplied as a single command line argument. These config files selectively activate Spring profiles as required to perform required processing and output writing steps.

Examples of config file are in the exampleConfigs dir. Most are relatively self explanatory, or should be annotated to explain their meaning.


example configs can be generated from the gradle task:

```
gradle writeExampleConfig
```

The easiest way to run turbo-laser is to activate the various spring profiles available as required. These are

1. elasticsearch - write to an elasticsearch cluster
2. jdbc - write to a JDBC endpoint
3. tika - process JDBC input with Tika. Extended with a custom PDF preprocessor to perform OCR on scanned PDF document. Extended with a custom PDF preprocessor to perform OCR on scanned PDF documents. (requires ImageMagick and Tesseract
4. gate - process JDBC input with a generic GATE app.
5. dBLineFixer - process JDBC input with dBLineFixer (concatenates multi-row documents)
6. basic - a job without a processing step, for simply writing JDBC input to elasticsearch
7. master - designates the JVM instance as a master, allowing it to create new jobs and update the job repository
8. slave - designates the JVM as a slave allowing it to execute processing steps retrieved from the activeMQ endpoint
9. deid - deidentify text with a GATE application or using teh ElasticGazetteer
10. biolark - specify the endpoint for the Biolark application, Tudor Groza's awesome HPO term extraction project.


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

To add additional JVM processes, whether locally or remotely (via the wonders of Spring Integration), just launch an instance with the same config files but with useScheduling = slave. You'll need an ActiveMQ server to co-ordinate the nodes (see config example for details)

That's it! If a job fails, any uncompleted partitions will be picked up by the next run. If a Job ends up in an unknown state (e.g. due to hardware failure), the next run will mark it as abandonded and recommence from the last successful job it can find in the repository.

Questions? Want to help? Drop me a message!
