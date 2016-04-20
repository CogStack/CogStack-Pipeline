# **WELCOME TO TURBO-LASER**


## Introduction

Turbo-laser is a distributed, fault tolerant batch processing architecture for GATE, using the Spring Batch framework. In the parlance of the batch processing domain language (http://docs.spring.io/spring-batch/reference/html/domain.html), it uses the remote partitioning method to create 'slave step' metadata for a DB table of documents. This metadata is persisted in the Spring database schema, and farmed out via a JMS middleware server. Remote worker JVM slaves then retrieve metadata descriptions of work units. The outcome of processing is then persisted in the database, allowing robust tracking of failed steps.

## Why is batch processing in NLP difficult?

When processing very large natural corpora (10s - 100s of millions of documents), it is likely that some documents will present certain difficulties for NLP processes that are hard to predict. For example, some documents may have very long sentences, an unusual sequence of characters, or machine only content. Such circumstances can create a range of problems for NLP algorithms, and thus fault tolerant batch frameworks are required to handle such edge cases.

## Example useage

The entire process is configured via a single config file, which, in order for Spring Batch to pick up correctly, is determined by the java environment variable TURBO_LASER. This should point to a directory, containing the following files (note: not all are necessary, only the ones for the jobs you want to run)

Here, you will need to configure input and output database connection details, the activeMQ server and other particulars specific to the job you want to run (for example, the GATE home directory, and the GATE application .xgapp)

> tika.conf
> gate.conf
> dBLineFixer.conf


Examples of config file are in the src/test/resourcespackages. Note, these test configurations are split into multiple configs, to ease integration testing, but they could just as easily all be places into one of the above named files in production. Required properties for all jobs are

> concurrency.properties - set the thread pool size for vertical scaling
> <db_type>DB.properties - set various JDBC connection settings
> step.properties - set the chunk (commit) interval for each job 'step' and the skipLimit = number of exceptions before the step fails


The parameters of other configuration files are Job specific (e.g. tika.properties has keepTags for specifying whether to output in XHTML or plaintext). The details of each are described in the comments of the respective example files

Turbo-laser is run with the standard Spring Batch CommandLineJobRunner, specifying the job type and appropriate Spring profiles, and key/value pairs that uniquely identify a job (which can be more or less anything - see Spring Batch documentation for details)

For example
```
java  -Dspring.profiles.active=dBLineFixer -jar turbo-laser-0.1.0.jar uk.ac.kcl.batch.JobConfiguration dBLineFixerJob date=test1
```

Alternatively, Turbo-laser can be run using cron type scheduling. Just set the following parameter in the .conf for e.g. a new job to be created every 5 seconds - it will wait for the previous batch to finish before commencing a new one

```
scheduler.rate = "*/5 * * * * *"
```

To Run in Scheduling mode: For example
```
java  -Dspring.profiles.active=tika,master,slave -jar turbo-laser-0.1.0.jar scheduled
```

To add additional JVM processes, via the wonders of Spring Integration, just launch an instance as follows
```
java  -Dspring.profiles.active=tika,slave -jar turbo-laser-0.1.0.jar scheduled
```

(Obviously replacing the spring profile as appropriate)


Turbo-laser assumes the job repository schema is already in place in the DB implementation of your choice (see spring batch docs for more details)


The following types of job/profiles are currently available

dBLineFixer = fixes a bizarre but somehow frequent occurance in databases where strings of text from a single document are spread across multiple rows

gate = run a generic GATE app. Specify which annotationSets to keep in the config file, or none to keep them all.

tika = the excellent 'can opener' apache project for all types of files. Extracts text. I've included a custom PDFPreprocessor class, which allows scanned PDF's to undergo Object Character Recognition. This requires ImageMagick and Tesseract to be installed and available on the system path



