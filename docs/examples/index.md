---
layout: default
title: Examples - documentation
output: 
  html_document: 
    highlight: pygments
---



# <a name="intro"></a> Introduction
[//]: # "-------------------------------------------------------------------------------------"
This document describes the available examples of CogStack data processing workflows. The document is divided into following parts:
1. [Getting CogStack](#getting-cogstack).
2. [How are the examples organized](#how-they-are-organized).
3. [How does CogStack work](#how-does-it-work).
4. [Available datasets](#datasets).
5. [Running CogStack](#running-cogstack).
6. Detailed description of examples, which currently are:
* [Example 1](#example-1) -- Processing a simple, structured dataset from a single DB source.
* [Example 2](#example-2) -- Processing a combined structured and free-text dataset from a single DB source (as in Quistart).
* [Example 3](#example-3) -- Processing a combined dataset from multiple DB sources, multiple jobs.
* [Example 4](#example-4) -- Processing a combined dataset with embedded documents from a single DB source.
* [Example 5](#example-5) -- 2-step processing of a combined dataset with embedded documents from a single DB source.
* [Example 6](#example-6) -- Basic security use-case: Example 2 extended with NGINX reverse proxy for secure access.
* [Example 7](#example-7) -- Logging: Example 6 extended with Fluentd logging mechanisms.
* [Example 8](#example-8) -- Simple NLP use-case: drug annotation using GATE and based on Example 2.
* [Example 9](#example-9) -- Defining multi-component pipelines: Example 4 and Example 8 combined.

The main directory with resources used in this tutorial is available in the the CogStack bundle under `examples` directory.

Some parts of this document are also used in [CogStack Quickstart](https://github.com/CogStack/CogStack-Pipeline) tutorial.



# <a name="getting-cogstack"></a> Getting CogStack
[//]: # "-------------------------------------------------------------------------------------"

The most convenient way to get CogStack bundle is to download it directly from the [official github repository](https://github.com/CogStack/CogStack-Pipeline) either by cloning it using git:

```bash
git clone -b dev --single-branch https://github.com/CogStack/CogStack-Pipeline.git
```
or by downloading it from the repository and decompressing it:
```bash
wget 'https://github.com/CogStack/CogStack-Pipeline/archive/dev.zip'
unzip dev.zip
```
The content will be decompressed into `CogStack-Pipeline/` directory.


[//]: # "<span style='color:red'> NOTE: </span>"
**Note: For the moment the CogStack bundle is obtained from the `dev` branch -- soon it will be merged into `master` branch with a version tag for a direct download.**

[//]: # "<span style='color:red'> NOTE: </span>"
**Note: For the moment, the CogStack pipeline Docker image used in this example is `cogstacksystems/cogstack-pipeline:dev-latest` However, once the development branch will be merged to `master` the image names will be updated.**



# <a name="how-they-are-organized"></a> How are the examples organized
[//]: # "-------------------------------------------------------------------------------------"

Each of the examples is organized in a way that it can be deployed and run independently. The directory structure of `examples/` tree is as follows:

```tree
.
├── docker-common
│   ├── elasticsearch
│   │   └── config
│   │       └── elasticsearch.yml
│   ├── fluentd
│   │   └── conf
│   │       └── fluent.conf
│   ├── kibana
│   │   └── config
│   │       └── kibana.yml
│   ├── nginx
│   │   ├── auth
│   │   └── config
│   │       └── nginx.conf
│   ├── pgsamples
│   │   └── init_db.sh
│   ├── pgjobrepo
│   │   └── create_repo.sh
│   └── docker.compose.yml   
│
├── example1
│   ├── cogstack
│   │   └── observations.properties
│   ├── db_dump
│   │   └── db_samples.sql.gz
│   ├── docker
│   │   └── docker-compose.override.yml
│   ├── extra
│   │   ├── db_create_schema.sql
│   │   └── prepare_db.sh
│   └── setup.sh
│
├── example2
│   ├── cogstack
│   │   └── observations.properties
│   ├── db_dump
│   │   └── db_samples.sql.gz
│   ├── docker
│   │   └── docker-compose.override.yml
│   ├── extra
│   │   ├── db_create_schema.sql
│   │   └── prepare_db.sh
│   └── setup.sh
│
├── example3
│   ├── cogstack
│   │   ├── gen_config.sh
│   │   ├── mt.properties
│   │   └── template.properties
│   ├── db_dump
│   │   ├── db_samples-mt.sql.gz
│   │   └── db_samples-syn.sql.gz
│   ├── docker
│   │   └── docker-compose.override.yml
│   ├── extra
│   │   ├── db_create_mt_schema.sql
│   │   ├── db_create_syn_schema.sql
│   │   ├── prepare_mtsamples_db.sh
│   │   └── prepare_synsamples_db.sh
│   └── setup.sh
│
├── example4
│   ├── cogstack
│   │   ├── observations.properties
│   │   └── test2.sh
│   ├── db_dump
│   │   ├── db_samples-docx-small.sql.gz
│   │   ├── db_samples-jpg-small.sql.gz
│   │   ├── db_samples-pdf-img-small.sql.gz
│   │   └── db_samples-pdf-text-small.sql.gz
│   ├── docker
│   │   └── docker-compose.override.yml
│   ├── extra
│   │   ├── db_create_schema.sql
│   │   ├── prepare_db.sh
│   │   └── prepare_single_db.sh
│   └── setup.sh
│
├── example5
│   ├── cogstack
│   │   ├── conf
│   │   │   ├── step-1
│   │   │   │   └── reports.properties
│   │   │   └── step-2
│   │   │       └── observations.properties
│   │   └── test2.sh
│   ├── db_dump
│   │   ├── db_samples-docx-small.sql.gz
│   │   ├── db_samples-jpg-small.sql.gz
│   │   ├── db_samples-pdf-img-small.sql.gz
│   │   └── db_samples-pdf-text-small.sql.gz
│   ├── docker
│   │   └── docker-compose.override.yml
│   ├── extra
│   │   ├── db_create_schema.sql
│   │   ├── prepare_db.sh
│   │   └── prepare_single_db.sh
│   └── setup.sh
│
├── example6
│   ├── cogstack
│   │   └── observations.properties
│   ├── db_dump
│   │   └── db_samples.sql.gz
│   ├── docker
│   │   └── docker-compose.override.yml
│   └── setup.sh
│
├── example7
│   ├── cogstack
│   │   └─── observations.properties
│   ├── db_dump
│   │   └── db_samples.sql.gz
│   ├── docker
│   │   └── docker-compose.override.yml
│   └── setup.sh
│
├── example8
│   ├── cogstack
│   │   └── observations.properties
│   ├── db_dump
│   │   └── db_samples.sql.gz
│   ├── docker
│   │   └── docker-compose.override.yml
│   ├── gate
│   │   └── app
│   │       ├── active.lst
│   │       ├── drug.gapp
│   │       ├── drug.lst
│   │       └── lists.def
│   ├── extra
│   │   └── clean_list.py
│   └── setup.sh
│
├── example9
│   ├── cogstack
│   │   └── observations.properties
│   ├── db_dump
│   │   └── db_samples.sql.gz
│   ├── docker
│   │   └── docker-compose.override.yml
│   ├── gate
│   │   └── app
│   │       ├── active.lst
│   │       ├── drug.gapp
│   │       ├── drug.lst
│   │       └── lists.def
│   └── setup.sh
│
├── rawdata
│   ├── mtsamples-txt-full.tgz
│   ├── mtsamples-txt-small.tgz
│   └── synsamples.tgz
│
├── download_db_dumps.sh
├── prepare_db_dumps.sh
└── prepare_docs.sh
```

**Note: the contents of `db_dump` subdirectories for each example will be created after running `download_db_dumps.sh` script (please see below).**


## Common and reusable components

The directory `docker-common` contains some common components and microservice configuration files that are used within all the examples (see [Running CogStack](#running-cogstack)). These components include:
* PostgreSQL databases: `pgsamples` and `pgjobrepo` directories,
* ElasticSearch node: `elasticsearch` directory,
* Kibana webservice dashboard: `kibana` directory,
* nginx reverse proxy service: `nginx` directory,
* Fluentd logging driver: `fluentd` directory,
* Common microservices Docker Compose base configuration file used across examples: `docker-compose.yml`.



## Examples

The directories `example*` stores the content of the examples, each containing such subdirectories:
* `cogstack` directory containing CogStack configuration files and/or custom pipeline scripts,
* `db_dump` directory containing database dumps used to initialize the samples input database,
* `docker` directory containing configuration files for docker-based deployment,
* `extra` directory containing scripts to generate database dumps locally (optional),
* `setup.sh` script to initialize the example before running it for the first time.

For a detailed description of each example please refer to its appropriate section.


## Raw data

The directory `rawdata` contains the raw EHRs data which will be used to prepare the initial database dumps for running the examples.


## Data preparation scripts

The script `prepare_docs.sh` is used to prepare the document data for Examples 4 and 5 in PDFs, DOCX and image formats.

The script `prepare_db_dumps.sh` is used to prepare locally all the database dumps to initialize the examples, whereas the script `download_db_dumps.sh` is used to automatically download all the database dumps (see [Running CogStack](#running-cogstack)).



# <a name="how-does-it-work"></a> How does CogStack work
[//]: # "-------------------------------------------------------------------------------------"

## Data processing workflow

The data processing workflow of CogStack is based on [Java Spring Batch](https://spring.io/) framework. Not to dwell too much into technical details and just to give a general idea -- the data is being read from a predefined *data source*, later it follows a number of *processing operations* with the final result stored in a predefined *data sink*. CogStack implements variety of data processors, data readers and writers with scalability mechanisms that can be selected in CogStack job configuration.

![cogstack](https://raw.githubusercontent.com/CogStack/CogStack-Pipeline/master/fig/cogstack_pipeline_sm2.png "CogStack data processing workflow")


Each CogStack data processing pipeline is configured using a number of parameters defined in the corresponding [Java *properties* file](https://en.wikipedia.org/wiki/.properties). Moreover, multiple CogStack data processing pipelines can be launched in parallel (see [Example 3](#example-3)) or chained together (see [Example 5](#example-5)), each using its own *properties* configuration file.

The *properties* files are usually stored in `cogstack/` subdirectory of each example. 


## <a name="cogstack-ecosystem"></a> CogStack ecosystem

CogStack ecosystem consists of multiple inter-connected microservices running together. For the ease of use and deployment we use [Docker](https://www.docker.com/) (more specifically, [Docker Compose](https://docs.docker.com/compose/)), and provide Compose files for configuring and running the microservices. The selection of running microservices depends mostly on the specification of EHR data source(s), data extraction and processing requirements.

![cogstack-ecosystem]({{ site.url }}/assets/uservices.png "CogStack ecosystem")

In the provided examples, the CogStack ecosystem is usually composed of the following microservices:
* `*samples` -- PostgreSQL database(s) loaded with a sample dataset under `db_samples` name,
* `cogstack-pipeline` -- CogStack pipeline -- the data processing engine,
* `cogstack-job-repo` -- PostgreSQL database for storing information about CogStack jobs and status,
* `elasticsearch-1` -- ElasticSearch node(s) for storing and querying the processed EHR data,
* `kibana` -- Kibana data visualization tool for querying the data from ElasticSearch.

The common Docker Compose base configuration file for these microservices which is shared among all the examples can be found in `examples/docker-common/docker-compose.yml`. The microservices configuration specific to each example can be found in `docker` subdirectory: `examples/example*/docker/docker-compose.override.yml` of each example.


## CogStack Pipeline configuration

There are multiple configurable parameters available to tailor the CogStack data processing pipeline to the specific data processing needs and available resources. Here we will cover only the most important parameters related with selecting the data processing components, configuring the input source, the output sink and data processing workflow for a given example.

![spring-batch]({{ site.url }}/assets/spring-batch.png "Spring Batch data processing pattern")


All the data processing operations are defined and assembled in `compositeSlaveStep` Bean (defined in `JobConfiguration.java`) according to the active Spring profiles specified in the corresponding *properties* file.


### Spring profiles

CogStack configuration file uses Spring profiles, which enable different components of the data processing pipeline. The available profiles are:

* `jdbc_in` -- a JDBC input database connector used for reading from databases,
* `jdbc_out` -- a JDBC output database connector used for writing to databases,
* `jdbc_out_map` -- a JDBC output database connector used for writing to databases using direct row/document mapping (*not covered by the examples*),
* `elasticsearch` -- using ES Java client API to insert documents to ElasticSearch (*not covered by the examples*),
* `elasticsearchRest` -- using REST API to insert documents to ElasticSearch,
* `jsonFileItemWriter` -- writing documents in JSON format directly to files (*not covered by the examples*),
* `localPartitioning` -- partitioning functionality for running jobs locally,
* `remotePartitioning` -- partitioning functionality for running jobs in master-slave configuration (*not covered by the examples*),
* `deid` -- de-identification process using GATE or ElasticGazetteer service (*not covered by the examples*),
* `tika` -- documents processing service,
* `biolark` -- NLP data processing service (*not covered by the examples*),
* `gate` NLP data processing service,
* `bioyodie` NLP data processing service (*not covered by the examples*),
* `pdfGeneration`, `thumbnailGeneration` -- PDF thumbnail generation service (*not covered by the examples*),
* `dBLineFixer` -- records modification process (*not covered by the examples*),
* `pdfbox` -- documents processing sugin PDFBox (*not covered by the examples*),
* `metadata` -- document metadata generation (*not covered by the examples*),
* `docman` -- mixed (both DB and filesystem) document processing (*not covered by the examples*).


### Item readers

Currently, only a basic database item reader is provided by the CogStack engine -- `documentItemReader` (defined in `JobConfiguration.java`), which uses `jdbc_in` profile. Item reader is of type `JdbcPagingItemReader<Document>` and uses the DB source configuration defined in *properties* file, alongside partitioning and document DB row mapper configuration.


### Item processors

A `compositeItemProcessorr` is used to launch delegate item processors (defined in `JobConfiguration.java`). Each processor extends the functionality of the abstract class `TLItemProcessor` and implements the functionality of Java abstract class `ItemProcessor<Document, Document>`. 

CogStack engine provides the following processors:
* `dBLineFixerItemProcessor` (profile: `dBLineFixer`) -- merging multiple records from a database into a single one (*not covered by the examples*),
* `gateDocumentItemProcessor` (profile: `gate`) -- [GATE](https://gate.ac.uk/) NLP service (*not covered by the examples*),
* `deIdDocumentItemProcessor` (profile: `deid`) -- de-identification of records using GATE (requires: `gate` profile) or custom ElasticGazetteer service (*not covered by the examples*),
* `JSONMakerItemProcessor` -- generating document output in JSON format; always used to meet requirements of composite processor,
* `MetadataItemProcessor` (profile: `metadata`) -- used to generate document metadata; uses [ImageMagick](https://www.imagemagick.org) external application (*not covered by the examples*),
* `PdfBoxItemProcessor` (profile: `pdfbox`) -- document processing using [PDFBox](https://pdfbox.apache.org/) application (*not covered by the examples*),
* `pdfGenerationItemProcessor` (profiles: `pdfGeneration`, `thumbnailGeneration`) -- PDF documents generation service utilizing [LibreOffice](https://www.libreoffice.org) or ImageMagick external applications (*not covered by the examples*),
* `thumbnailGenerationItemProcessor` (profile: `thumbnailGeneration`) -- PDF thumbnail generation service utilizing ImageMagick external applications (*not covered by the examples*),
* `tikaDocumentItemProcessor` (profile: `tika`) -- processing binary documents using [Apache Tika](https://tika.apache.org); uses own PDF parser utilizing ImageMagick (conversion) and [TesseractOCR](https://github.com/tesseract-ocr/tesseract) (OCR) external applications,
* `webserviceDocumentItemProcessor` (profile: `webservice`) -- processing documents using LibreOffice webservice (*not covered by the examples*).


### Item writers

A `compositeItemWriter` is is used to launch delegate item writers (defined in `JobConfiguration.java`). Each writer implements functionality of Java abstract class `ItemWriter<Document>`.

CogStack engine provides following writers:

* `simpleJdbcItemWriter` (profile: `jdbc_out`) -- writes records to a defined target database,
* `mapJdbcItemWriter` (profile: `jdbc_out_map`) -- writes records to a defined target database using direct mapping (*not covered by the examples*), 
* `esDocumentWriter` (profile: `elasticsearch`) -- writes documents to ElasticSearch cluster using ES Java client API (*not covered by the examples*),
* `esRestDocumentWriter` (profile: `elasticsearchRest`) -- writes to ElasticSearch cluster using ES REST API,
* `jsonFileItemWriter` (profile: `jsonFileItemWriter`) -- writes documents as JSON files (*not covered by the examples*).



# <a name="datasets"></a> Available datasets
[//]: # "-------------------------------------------------------------------------------------"

The base dataset used in examples consists of two types of EHR data:
* Synthetic -- structured, synthetic EHRs, generated using [Synthea](https://synthetichealth.github.io/synthea/) application,
* Medial reports -- unstructured, medical health report documents obtained from [MTsamples](https://www.mtsamples.com).

These datasets, although unrelated, are used together to compose a combined dataset.


## <a name="samples-syn"></a> Synthetic -- synthea-based

This dataset consists of synthetic EHRs that were generated using [Synthea](https://synthetichealth.github.io/synthea/) application -- the synthetic patient generator that models the medical history of generated patients. For this tutorial, we generated EHRs for 100 patients and exported them as CSV files. Typed in the main synthea directory, the command line for running it:
```bash
./run_synthea -p 100 \ 
  --generate.append_numbers_to_person_names=false \
    --exporter.csv.export=true
```
However, the pre-generated files are provided in a compressed form as `examples/rawdata/synsamples.tgz` file.


[//]: # "Dataset description"
The generated dataset consists of the following files:
- `allergies.csv` -- Patient allergy data,
- `careplans.csv` -- Patient care plan data, including goals,
- `conditions.csv` -- Patient conditions or diagnoses,
- `encounters.csv` -- Patient encounter data,
- `imaging_studies.csv` -- Patient imaging metadata,
- `immunizations.csv` -- Patient immunization data,
- `medications.csv` -- Patient medication data,
- `observations.csv` -- Patient observations including vital signs and lab reports,
- `patients.csv` -- Patient demographic data,
- `procedures.csv` -- Patient procedure data including surgeries.

For more details about the generated files and the schema definition please refer to the [official synthea wiki page](https://github.com/synthetichealth/synthea/wiki/CSV-File-Data-Dictionary). The sample records are shown while describing [Example 1](#example-1). 


## <a name="samples-mt"></a> Medical reports -- MTSamples

[MTsamples](https://www.mtsamples.com) is a collection of transcribed medical sample reports and examples. The reports are in a free-text format and have been downloaded directly from the official website. 

Each report contain such information as:
* Sample Type,
* Medical Specialty,
* Sample Name,
* Short Description,
* Medical Transcription Sample Report (in free text format).

The collection comprises in total of 4873 documents. The sample document is shown while describing [Example 2](#example-2). 


## Preparing the data

For the ease of use a database dump with predefined schema and preloaded data will be provided in each of the examples `examples/example*/db_dump/` directory. This way, the PostgreSQL database with sample data will be automatically initialized when deployed using Docker. The dabatase dumps can be directly downloaded from [Amazon S3](https://aws.amazon.com/s3) bucket by running in the main `examples/` directory:

```bash
bash download_db_dumps.sh
```

Alternatively, the PostgreSQL database schema definitions are stored in `examples/example*/extra/` directories alongside the scripts to generate the database dumps locally. However, some examples may require pre-processed documents data to be available prior running -- the script `prepare_docs.sh` in the main `examples/` directory takes care of that. The script `prepare_db_dumps.sh` is used to prepare locally all the database dumps to initialize the examples.



# <a name="running-cogstack"></a> Running CogStack
[//]: # "-------------------------------------------------------------------------------------"

## Setup

For the ease of use CogStack is being deployed and run using Docker. However, before starting the CogStack ecosystem for the first time, a setup scripts needs to be run locally to prepare the Docker images and configuration files for CogStack data processing engine. For each of the examples, a script is available in its directory `examples/example*/` path and can be run as:

```bash
bash setup.sh
```
As a result, a temporary directory `__deploy/` will be created containing all the necessary artifacts to deploy CogStack.


## Docker-based deployment

Next, we can proceed to deploy CogStack ecosystem using Docker Compose. It will configure and start microservices based on the provided Compose file: `examples/example*/docker/docker-compose.yml`. Moreover, the PostgreSQL database container comes with pre-initialized database dump ready to be loaded directly into. In order to run CogStack, type in the `examples/example*/__deploy/` directory:
```bash
docker-compose up
```
In the console there will be printed status logs of the currently running microservices. For the moment, however, they may be not very informative (we're working on that).


## Connecting to the microservices

### CogStack ecosystem

The picture below sketches a general idea on how the microservices are running and communicating within a sample CogStack ecosystem used in this tutorial.

![alt text]({{ site.url }}/assets/uservices.png "CogStack data processing workflow")

Assuming that everything is working fine, we should be able to connect to the running microservices.


### Kibana and ElasticSearch

Kibana dashboard used to query the EHRs can be accessed directly in browser via URL: `http://localhost:5601/`. The data can be queried using a number of ElasticSearch indices, e.g. `sample_observations_view`. Usually, each index will correspond to the database view in `db_samples` (`samples-db` PostgreSQL database) from which the data was ingested. However, when entering Kibana dashboard for the first time, an index pattern needs to be configured in the Kibana management panel -- for more information about its creation, please refer to the official [Kibana documentation](https://www.elastic.co/guide/en/kibana/current/tutorial-define-index.html).

In addition, ElasticSearch REST end-point can be accessed via URL `http://localhost:9200/`. It can be used to perform manual queries or to be used by other external services -- for example, one can list the available indices:
```bash
curl 'http://localhost:9200/_cat/indices'
```
or query one of the available indices -- `sample_observations_view`:
```bash
curl 'http://localhost:9200/sample_observations_view'
```

For more information about possible documents querying or modification operations, please refer to the official [ElasticSearch documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started.html).

### PostgreSQL sample database

Moreover, the access PostgreSQL database with the input sample data is exposed directly at `localhost:5555`. The database name is `db_sample` with user *test* and password *test*. To connect, one can run:
```bash
psql -U 'test' -W -d 'db_samples' -h localhost -p 5555
```

As a side note, the name for ElasticSearch node in the Docker Compose has been set as `elasticsearch-1`. The `-1` ending emphasizes that for larger-scale deployments, multiple ElasticSearch nodes can be used -- typically, minimum of 3.


# <a name="examples-general"></a> General information about examples

## Database schema

In the current implementation, CogStack can only ingest EHR data from a specified input database. This is why, in order to process the sample patient data covered in this tutorial, one needs to create an appropriate database schema and load the EHR data into a preferred data sink. The data can be stored either as records in a database, documents in an ElasticSearch cluster or as files in a filesystem. 

The most commonly used data sink is going to be ElasticSearch cluster (in our examples -- just a single node). However, as relational join statements have a high performance burden for ElasticSearch, the EHR data is best to be stored denormalized in ElasticSearch. This is why, for the moment, we rely on ingesting the data from additional view(s) created in the sample database.


## Properties file

### Database source

When using PostgreSQL database as a data source, please note that the `source.poolSize` property defines the maximum size of the connection pool available for performing queries by CogStack engine. A PostgreSQL database, by default, has a maximum connection limit set to `100`, hence exceeding the limit (either by a single job or multiple parallel ones) may lead to termination of the data pipeline.

One of the solutions to overcome this issue can be to run the PostgreSQL container with additional options specified in Docker-compose file:
```yml
command: "-c 'shared_buffers=256MB' -c 'max_connections=1000'"
```
extending the connection limit with the available RAM for connection buffers.

### ElasticSearch sink

As an additional feature, security and ssl encryption can be enabled for communication with ElasticSearch. However, it uses the [ElasticSearch X-Pack bundle](https://www.elastic.co/guide/en/x-pack/current/xpack-introduction.html) and requires license for commercial deployments, hence it is disabled by default.

### Partitioner

In the current implementation, CogStack Pipeline can only partition the data using the records' primary key (`partitioner.pkColumnName` property, containing unique values) and records' update time (`partitioner.timeStampColumnName` property) as defined in created views. This is specified by `PKTimeStamp` partitioning method types:
```properties
partitioner.partitionType = PKTimeStamp
```



# <a name="example-1"></a> Example 1
[//]: # "-------------------------------------------------------------------------------------"

## General information

This is a very basic example covering only processing and ingestion of structured synthetic data into ElasticSearch. However, it forms a good base for starting to work with CogStack data processing pipelines.


## Database schema

### Patients table

The first 5 records of patient data (file: `patients.csv` from [Synthea-based samples](#samples-syn)) in CSV format is presented below:
```csv
ID,BIRTHDATE,DEATHDATE,SSN,DRIVERS,PASSPORT,PREFIX,FIRST,LAST,SUFFIX,MAIDEN,MARITAL,RACE,ETHNICITY,GENDER,BIRTHPLACE,ADDRESS,CITY,STATE,ZIP
b9f5a11b-211d-4ced-b3ba-12012c83b937,1939-08-04,1996-03-15,999-11-9633,S99999830,X106007X,Mr.,Brady,Lynch,,,M,white,polish,M,Worcester,701 Schiller Esplanade,Fitchburg,Massachusetts,01420
fab43860-c3be-4808-b7b4-00423c02816b,1962-06-21,2011-03-10,999-67-8307,S99958025,X26840237X,Mrs.,Antonia,Benavides,,Padrón,M,hispanic,mexican,F,Rockland,643 Hand Bay,Boston,Massachusetts,02108
84dd6378-2ddc-44b6-9292-2a4461bcef53,1998-12-01,,999-50-5147,S99987241,,Mr.,Keith,Conn,,,,white,english,M,Rockland,461 Spinka Extension Suite 69,Framingham,Massachusetts,01701
9929044f-1f43-4453-b2c0-a2f45dcdd4be,2014-09-23,,999-64-4171,,,,Derrick,Lakin,,,,white,irish,M,Tewksbury,577 Hessel Lane,Hampden,Massachusetts,
```

The `patients` table definition in PostgreSQL according to the [specification](https://github.com/synthetichealth/synthea/wiki/CSV-File-Data-Dictionary): 
```sql
CREATE TABLE patients (
	id UUID PRIMARY KEY,
	birthdate DATE NOT NULL, 
	deathdate DATE, 
	ssn VARCHAR(64) NOT NULL, 
	drivers VARCHAR(64),
	passport VARCHAR(64),
	prefix VARCHAR(8),
	first VARCHAR(64) NOT NULL,
	last VARCHAR(64) NOT NULL,
	suffix VARCHAR(8),
	maiden VARCHAR(64),
	marital CHAR(1),
	race VARCHAR(64) NOT NULL, 
	ethnicity VARCHAR(64) NOT NULL,
	gender CHAR(1) NOT NULL,
	birthplace VARCHAR(64) NOT NULL,
	address VARCHAR(64) NOT NULL,
	city VARCHAR(64) NOT NULL,
	state VARCHAR(64) NOT NULL,
	zip VARCHAR(64)
) ;
```

### Encounters table

Similarly, the first 5 records of patient encounters data (file: `encounters.csv`)
```csv
ID,START,STOP,PATIENT,CODE,DESCRIPTION,COST,REASONCODE,REASONDESCRIPTION
123ffd84-618e-47cd-abca-5fe95b72179a,1955-07-30T07:30Z,1955-07-30T07:45Z,b9f5a11b-211d-4ced-b3ba-12012c83b937,185345009,Encounter for symptom,129.16,36971009,Sinusitis (disorder)
66b016e9-e797-446a-8f2b-e5534acbbb04,1962-03-09T07:30Z,1962-03-09T07:45Z,b9f5a11b-211d-4ced-b3ba-12012c83b937,185349003,Encounter for check up (procedure),129.16,,
d517437d-50b5-4cab-aca2-9c010c06989e,1983-08-19T07:30Z,1983-08-19T08:00Z,b9f5a11b-211d-4ced-b3ba-12012c83b937,185349003,Encounter for check up (procedure),129.16,,
2452cf09-021b-4586-9e33-59d7d2242f31,1987-08-28T07:30Z,1987-08-28T07:45Z,b9f5a11b-211d-4ced-b3ba-12012c83b937,185349003,Encounter for check up (procedure),129.16,,
f25a828f-ae79-4dd0-b6eb-bca26138421b,1969-09-13T14:02Z,1969-09-13T14:34Z,fab43860-c3be-4808-b7b4-00423c02816b,185345009,Encounter for symptom,129.16,10509002,Acute bronchitis (disorder)
```

with the corresponding `encounters` table definition:
```sql
CREATE TABLE encounters (
	id UUID PRIMARY KEY NOT NULL,
	start TIMESTAMP NOT NULL,
	stop TIMESTAMP,
	patient UUID REFERENCES patients,
	code VARCHAR(64) NOT NULL,
	description TEXT_TYPE NOT NULL,
	cost REAL NOT NULL,
	reasoncode VARCHAR(64),
	reasondescription VARCHAR(256)
) ;
```

### Observations table

The next table is `observations`, where the first 5 rows of `observations.csv` file are:
```csv
DATE,PATIENT,ENCOUNTER,CODE,DESCRIPTION,VALUE,UNITS,TYPE
1987-08-28,b9f5a11b-211d-4ced-b3ba-12012c83b937,2452cf09-021b-4586-9e33-59d7d2242f31,8302-2,Body Height,180.3,cm,numeric
2002-06-27,fab43860-c3be-4808-b7b4-00423c02816b,fd113dfd-5e2c-40f2-98f3-e153665c3f53,8302-2,Body Height,165.2,cm,numeric
2009-04-07,84dd6378-2ddc-44b6-9292-2a4461bcef53,2058da3b-f7f3-4ebc-8c1e-360cd256cdcb,8302-2,Body Height,138.3,cm,numeric
2002-06-27,fab43860-c3be-4808-b7b4-00423c02816b,fd113dfd-5e2c-40f2-98f3-e153665c3f53,29463-7,Body Weight,81.8,kg,numeric
2009-04-07,84dd6378-2ddc-44b6-9292-2a4461bcef53,2058da3b-f7f3-4ebc-8c1e-360cd256cdcb,29463-7,Body Weight,24.6,kg,numeric
```

and the corresponding table definition:
```sql
CREATE TABLE observations (
	cid SERIAL PRIMARY KEY,                     -- (*)
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP, --(*)
	date DATE NOT NULL, 
	patient UUID REFERENCES patients,
	encounter UUID REFERENCES encounters,
	code VARCHAR(64) NOT NULL,
	description TEXT_TYPE NOT NULL,
	value VARCHAR(64) NOT NULL,
	units VARCHAR(64),
	type VARCHAR(64) NOT NULL
) ;
```

Here, with `-- (*)` have been marked additional fields with auto-generated values. These are: `CID` -- an automatically generated primary key and `DCT` -- a document creation timestamp. They will be later used by CogStack engine for data partitioning when processing the records. The `patient` and `encounters` tables have their primary keys (`ID` field) already defined (of `uuid` type) and are included in the input CSV files.


### Database views

Next, we define a `observations_view` that will be used by CogStack data processing engine to ingest the records from input database:
```sql
CREATE VIEW observations_view AS
	 SELECT
		p.id AS patient_id, 
		p.birthdate AS patient_birth_date,
		p.deathdate AS patient_death_date,
		p.ssn AS patient_ssn,
		p.drivers AS patient_drivers,
		p.passport AS patient_passport,
		p.prefix AS patient_prefix,
		p.first AS patient_first_name,
		p.last AS patient_last_name,
		p.suffix AS patient_suffix,
		p.maiden AS patient_maiden,
		p.marital AS patient_marital,
		p.race AS patient_race,
		p.ethnicity AS patient_ethnicity,
		p.gender AS patient_gender,
		p.birthplace AS patient_birthplace,
		p.address AS patient_addr,
		p.city AS patient_city,
		p.state AS patient_state,
		p.zip AS patient_zip,
		
		enc.id AS encounter_id,
		enc.start AS encounter_start,
		enc.stop AS encounter_stop,
		enc.code AS encounter_code,
		enc.description AS encounter_desc,
		enc.cost AS encounter_cost,
		enc.reasoncode AS encounter_reason_code,
		enc.reasondescription AS encounter_reason_desc,

		obs.cid AS observation_id,            --(*)
		obs.created AS observation_timestamp, --(*)

		obs.date AS observation_date,
		obs.code AS observation_code,
		obs.description AS observation_desc,
		obs.value AS observation_value,
		obs.units AS observation_units,
		obs.type AS observation_type
	FROM 
		patients p, 
		encounters enc,
		observations obs
	WHERE 
		enc.patient = p.id AND
		obs.patient = p.id AND 
		obs.encounter = enc.id
	;
```
The goal here is to denormalize the database schema for CogStack and ElasticSearch data ingestion, as the `observations` table is referencing both the `patient` and `encounters` tables by their primary key. In the current implementation, CogStack engine cannot yet perform dynamic joins over the relational data from specific database tables.

Some of the crucial fields required for configuring CogStack Pipeline engine with Document data model have been marked with `--(*)` -- these are:
- `observation_id` -- the unique identifier of the observation record (typically, the primary key),
- `observation_timestamp` -- the record creation or last update time.

These fields will be later used when preparing the *properties* configuration file for CogStack data processing workflow.


## Properties file

### General information

Each CogStack data processing pipeline is configured using a number of parameters defined in the corresponding [Java *properties* file](https://en.wikipedia.org/wiki/.properties). In this example we use only one pipeline with configuration specified in `examples/example1/cogstack/observations.properties` file. 


### Spring profiles

CogStack configuration file uses a number of Spring profiles, which enable different components of the data processing pipeline. In this example we have:
```
spring.profiles.active = jdbc_in,elasticsearchRest,localPartitioning
```
which denotes that only such profiles will be active:
* `jdbc_in` for JDBC input database connector, 
* `elasticsearchRest` for using REST API for inserting documents to ElasticSearch,
* local `partitioning` functionality (for data processing) -- this property is optional, as `localPartitioning` will be used by default.


### Data source

The parameters for specifying the data source are defined as follows:
```properties
source.JdbcPath = jdbc:postgresql://samples-db:5432/db_samples
source.Driver = org.postgresql.Driver
source.username = test
source.password = test
```
In this example we are using a PostgreSQL database which driver is defined by `source.Driver` parameter. The PostgreSQL database service is available in the CogStack ecosystem as `samples-db`, has exposed port `5432` for connections and the sample database name is `db_samples` -- all these details need to be included in the `source.JdbcPath` parameter field.


Next, we need to instruct CogStack engine how to query the records from the data source:
```properties
source.selectClause = SELECT *
source.fromClause = FROM observations_view
source.sortKey = observation_id

source.primaryKeyFieldValue = observation_id
source.timeStamp = observation_timestamp

source.dbmsToJavaSqlTimestampType = TIMESTAMP
```
This is where the previously defined `observations_view` with additional CogStack-specific fields are used.


### Data sink

Next, we need to define the data sink -- in our example, and by default, ElasticSearch is being used:
```properties
elasticsearch.cluster.host = elasticsearch-1
elasticsearch.cluster.port = 9200
```
Similarly, as when defining the sample database source, we need to provide the ElasticSearch host and port configuration according to the microservices definition in the corresponding Docker Compose file (see `examples/example1/docker/docker-compose.yml`).


In the next step, we specify the ElasticSearch indexing parameters (optional):
```properties
elasticsearch.index.name = sample_observations_view
elasticsearch.excludeFromIndexing = observation_id
```
We specify the index name which will be used to store the documents processed by CogStack engine. Additionally, we specify which fields should be excluded from the indexing -- by default, we exclude the binary content, the constant-value fields and the primary key from the `observations_view`.



### Jobs and CogStack engine configuration

CogStack engine in order to coordinate the workers needs to keep the information about the current jobs in an additional PostgreSQL database -- `cogstack-job-repo`. Hence, similarly as when defining the source database, this database needs to specified:
```properties
jobRepository.JdbcPath = jdbc:postgresql://cogstack-job-repo:5432/cogstack
jobRepository.Driver = org.postgresql.Driver
jobRepository.username = cogstack
jobRepository.password = mysecretpassword

job.jobName = job_observations_view
```
The last parameter `job.jobName` is a default name for the jobs that will be created (optional).


### Partitioner and scheduler

Another set of useful parameters are related with controlling the job execution and data partitioning:
```properties
partitioner.partitionType = PKTimeStamp
partitioner.tableToPartition = observations_view
partitioner.pkColumnName = observation_id
partitioner.timeStampColumnName = observation_timestamp
```

Apart from data partitioning, although optional, it can be sometimes useful to set up the scheduler -- the following line corresponds to the scheduler configuration:
```properties
scheduler.useScheduling = false
```
In this example we do not use the scheduler, since we ingest EHRs from the data source only once. However, in case when data is being generated in a continuous way, scheduler should be enabled to periodically run CogStack jobs to process the new EHRs.


## Deployment information

This example uses the standard stack of microservices as presented in [CogStack ecosystem](#cogstack-ecosystem) with the Docker Compose file override in `examples/example1/docker/docker-compose.override.yml`.

It also uses a single CogStack *properties* file (see `examples/example1/cogstack/observations.properties`) and hence runs only one instance of CogStack data processing engine.



# <a name="example-2"></a> Example 2
[//]: # "-------------------------------------------------------------------------------------"

## General information

This example is an extension of [Example 1](#example-1). Apart from containing structured synthetic data, it also contains free-text documents data, hence creating a combined dataset. 

This example is also covered as a main part of [CogStack Quickstart](https://github.com/CogStack/CogStack-Pipeline) tutorial.


## Database schema

The database schema is almost the same as the one defined in [Example 1](#example-1). The only difference is an additional column in `encounters` table, as presented below.

### Encounters table

The `encouters` table definition:

```sql
CREATE TABLE encounters (
	id UUID PRIMARY KEY NOT NULL,
	start TIMESTAMP NOT NULL,
	stop TIMESTAMP,
	patient UUID REFERENCES patients,
	code VARCHAR(64) NOT NULL,
	description VARCHAR(256) NOT NULL,
	cost REAL NOT NULL,
	reasoncode VARCHAR(64),
	reasondescription VARCHAR(256),
	document TEXT --(*)
) ;
```
Here, with `-- (*)` has been marked an additional `document` column field. This extra field will be used to store the content of a document from [MTSamples dataset](#samples-mt). 

Just to clarify, [Synthea-based](#samples-syn) and [MTSamples](#samples-mt) datasets are two unrelated datasets. Here, we are extending the synthetic dataset with the clinical documents from the MTSamples to create a combined one, to be able to perform a bit more interesting queries.

A sample document from MTSamples dataset is presented below:
```text
Sample Type / Medical Specialty: Allergy / Immunology
Sample Name: Allergic Rhinitis
Description: A 23-year-old white female presents with complaint of allergies.
(Medical Transcription Sample Report)
SUBJECTIVE: This 23-year-old white female presents with complaint of allergies. She used to have allergies when she lived in Seattle but she thinks they are worse here. In the past, she has tried Claritin, and Zyrtec. Both worked for short time but then seemed to lose effectiveness. She has used Allegra also. She used that last summer and she began using it again two weeks ago. It does not appear to be working very well. She has used over-the-counter sprays but no prescription nasal sprays. She does have asthma but doest not require daily medication for this and does not think it is flaring up.

MEDICATIONS: Her only medication currently is Ortho Tri-Cyclen and the Allegra.

ALLERGIES: She has no known medicine allergies.

OBJECTIVE:
Vitals: Weight was 130 pounds and blood pressure 124/78.
HEENT: Her throat was mildly erythematous without exudate. Nasal mucosa was erythematous and swollen. Only clear drainage was seen. TMs were clear.
Neck: Supple without adenopathy.
Lungs: Clear.

ASSESSMENT: Allergic rhinitis.

PLAN:
1. She will try Zyrtec instead of Allegra again. Another option will be to use loratadine. She does not think she has prescription coverage so that might be cheaper.
2. Samples of Nasonex two sprays in each nostril given for three weeks. A prescription was written as well. 

Keywords: allergy / immunology, allergic rhinitis, allergies, asthma, nasal sprays, rhinitis, nasal, erythematous, allegra, sprays, allergic,

```

### Database views

Analogously, the new `document` column field is included in the `observations_view`, where the view is based on the one defined in [Example 1](#example-1).


## Properties file

The *properties* file used in this example is the same as in [Example 1](#example-1).


## Deployment information

Similarly as in Example 1, this one uses the standard stack of microservices overriden in `examples/example2/docker/docker-compose.override.yml`. 

It also uses a single CogStack *properties* file and hence runs only one instance of CogStack data processing engine (see: `examples/example2/cogstack/observations.properties`).



# <a name="example-3"></a> Example 3
[//]: # "-------------------------------------------------------------------------------------"

## General information

This example covers a case of multiple data sources and multiple CogStack instances scenario. This example is a further extension of both [Example 1](#example-1) and [Example 2](#example-2). It extends Example 1 by defining schema for all the tables for [Synthea-based](#samples-syn) patient data. It also extends Example 2 by defining a separate table for representing and storing [MTSamples](#samples-mt) data.


## Database schema

The database schema is based on the one defined in [Example 1](#example-1) where the same definition logic has been applied to the rest of CSV files available in the synthetic dataset. The complete database schema for the synthetic data is available in `examples/example3/extra/db_create_syn_schema.sql` file. 

The only new table is the one for representing MTSamples data defined in `examples/example3/extra/db_create_mt_schema.sql`.


### Samples table and view

The definition of `mtsamples` table and its corresponding view:

```sql
CREATE TABLE mtsamples (
	cid SERIAL PRIMARY KEY, --(*)
	sample_id INTEGER NOT NULL,
	type VARCHAR(256) NOT NULL,
	type_id INTEGER NOT NULL,
	name VARCHAR(256) NOT NULL,
	description TEXT NOT NULL,
	document TEXT NOT NULL,
	dct TIMESTAMP DEFAULT CURRENT_TIMESTAMP	-- (*)
) ;
```

In contrast to MTSamples data representation used in [Example 2](#example-2) (where the full content of a document was stored in the `document` field in the `encounters` table), in this example we partially parse the document, hence improving the data representation.

Two additional fields have been added to connect with CogStack Document model:
- `cid` -- automatically generated unique id,
- `dct` -- a document creation timestamp.

Please refer to `examples/example3/extra/prepare_synsamples_db.sh` on how the synthetic data is parsed and `examples/example3/extra/prepare_synsamples_db.sh` on how the MTSamples data is parsed.


## Properties files

The *properties* files used in this example are based on the one from [Example 1](#example-1). However, since multiple views are defined, the *properties* files can be automatically generated based on a provided `template.properties` file by running in `examples/example3/cogstack/` directory:
```bash
bash gen_config.sh
```
This way, each generated *properties* file corresponds to an individual view as defined in the database schema. 

Apart from that, a separate `mt.properties` file is provided for processing MTSamples data as defined in `samples_view`.


## Deployment information

This example uses the standard stack of microservices (see: [CogStack ecosystem](#cogstack-ecosystem)), but extended with additional database storing input sample data. It uses 2 separate input databases as the data source: `mtsamples-db` and `samples-db` -- see: `examples/example3/docker/docker-compose.override.yml`.

It also uses multiple CogStack *properties* files, hence multiple instances of CogStack data processing engine are run, one per each *properties* file.



# <a name="example-3"></a> Example 4
[//]: # "-------------------------------------------------------------------------------------"

## General information

This example covers a common use-case of processing and parsing document data that is stored alongside records in the database. This example is based on [Example 2](#example-2) and extends the CogStack data processing workflow by including [Apache Tika](#https://tika.apache.org/) module as one of the core components used for processing documents.


## Data preparation

Apart from structured records data in text format, this example uses documents which are loaded directly into the input database. The script for generating documents `prepare_docs.sh` is provided in the examples main directory.

At the moment, the following use-cases have been prepared with documents generated in the following formats:
- `docx` -- documents in DOCX format of text type,
- `pdf-text` -- documents in PDF format of text type,
- `pdf-img` -- documents in PDF format of image type,
- `jpg` -- documents in JPEG format.

Each use-case can be seen as a standalone example, but using the same database schema, CogStack *properties* file and docker-compose file. Hence, when generating docker-based deployment data (by running `setup.sh` script), a separate directory will be generated.


## Database schema

The database schema is based on the one defined in [Example 2](#example-2). Only some minor modifications were made in `encounters` table and `observations_view` -- see: `examples/example4/extra/db_create_schema.sql` file.


### Encounters table

```sql
CREATE TABLE encounters (
	cid SERIAL,	--(*)
	id UUID PRIMARY KEY NOT NULL,
	start TIMESTAMP NOT NULL,
	stop TIMESTAMP,
	patient UUID REFERENCES patients,
	code VARCHAR(64) NOT NULL,
	description VARCHAR(256) NOT NULL,
	cost REAL NOT NULL,
	reasoncode VARCHAR(64),
	reasondescription VARCHAR(256),
	binarydocument BYTEA --(*)
) ;
```

In this example, the document is stored in column `binarydocument` of `bytea` type -- instead of `document` as raw `TEXT` defined in [Example 2](#example-2).


### Observations view

```sql
CREATE VIEW observations_view AS
	 SELECT
		p.id AS patient_id, 
		
		-- ... 

		enc.binarydocument AS encounter_binary_doc, --(*)

		obs.cid AS observation_id, --(*)
		obs.created AS observation_timestamp, --(*)

		-- ...

	FROM 
		patients p, 
		encounters enc,
		observations obs
	WHERE 
		enc.patient = p.id AND
		obs.patient = p.id AND 
    	obs.encounter = enc.id
	;
```

The important field used in this view is `encounter_binary_doc` which will be used to read the binary document by CogStack engine.


## Properties file

The *properties* file used in this example is based on the one from [Example 2](#example-2), but extended with parts covering Tika documents processor and which are covered below.


### Spring profiles

The spring profile part has been updated with adding a `tika` profile:
```properties
spring.profiles.active = jdbc_in,elasticsearchRest,tika,localPartitioning
```


### Tika configuration

A new part covering Tika processing has been added:
```properties
tika.tikaFieldName = tika_output
tika.binaryContentSource = database
tika.binaryFieldName = encounter_binary_doc
```
The property `tika.tikaFieldName` denotes the name of the key field `tika_output`. This field will be present in the output JSON file where the value will hold the content of the Tika-parsed document. It is an optional value to specify -- by default `outTikaField` name is used. 

The property `tika.binaryContentSource` defines the source where the documents are stored -- in our case: `database`. Following, the property `tika.binaryFieldName` denotes the name of column that contains binary document data -- in our case that is `encounter_binary_doc` field in `observations_view` view. It is an optional property to set, as by default `database` will be used.

It's important to note that the remaining information about mapping and querying of the source database tables and record fields are covered by `source.*` properties, as explained in [Example 1](#example-1).


### Performance tweaking

Processing the documents and extracting the text can be a computationally expensive task. Depending on the documents type and whether the documents contain images, one may try to tweak the performance of the ingestion pipeline according to the available resources.

For example, one may use a larger number of processing threads by specifying:
```properties
step.concurrencyLimit = 8
```

Additionally, one may reduce the default value of the data chunk size, which specifies the batch processing size before committing the results:
```properties
step.chunkSize = 10
```

## Deployment information

When running `setup.sh` script, a number of separate directories will be created, each corresponding to a document format use-case.

Apart from that, this example uses the standard stack of microservices (see: [CogStack ecosystem](#cogstack-ecosystem)) and also uses a single CogStack *properties* file, running only one instance of CogStack data processing engine.



# <a name="example-3"></a> Example 5
[//]: # "-------------------------------------------------------------------------------------"

## General information

This example covers a bit more complex use-case of processing and parsing EHR data where the documents are stored alongside records in the same database. This example is based both on [Example 3](#example-3) and [Example 4](#example-4), with the difference that only a single job is being run and the data processing workflow is divided into two steps:
1. Pre-processing and parsing of the documents data,
2. Processing all the records.

Usually, we would like to perform the steps (1) and (2) in the same workflow as presented in Example 4. However, there may be cases that we may prefer to pre-process the data and store it temporarily prior to ingestion into ElasticSeach (or other data sink). This may be a solution for cases where one would like to have more control on the documents parsing process, e.g., to possibly easily re-launch parsing of the selected or failed documents.


## Database schema

The database schema is based on the one from [Example 3](#example-3) with some minor modifications and with additional tables introduced.


### Encounters table

In the `encounters` table a representation of the document data has been altered:

```sql
CREATE TABLE encounters (
	cid SERIAL NOT NULL,
	id UUID PRIMARY KEY,
	start TIMESTAMP NOT NULL,
	stop TIMESTAMP,
	patient UUID REFERENCES patients,
	code VARCHAR(64) NOT NULL,
	description VARCHAR(256) NOT NULL,
	cost REAL NOT NULL,
	reasoncode VARCHAR(64),
	reasondescription VARCHAR(256),
	documentid INTEGER --(*)
) ;
```

In this example, we only store the ID of the document in `documentid` field.


### Medical reports -- binary documents

Next, we define `medical_reports` table on a similar basis as `samples` table used in Example 3:

```sql
CREATE TABLE medical_reports (
	cid INTEGER PRIMARY KEY, --(*)
	sampleid INTEGER NOT NULL,
	typeid INTEGER NOT NULL,
	dct TIMESTAMP NOT NULL,
	filename VARCHAR(256) NOT NULL,
	binarydoc BYTEA NOT NULL --(*)
) ;
```
In this example, the document is stored in binary format in `binarydoc` field. Similarly, as in Example 4, this column will be used to access the binary content of the document.


### Medical reports -- processed documents

Next, we define `medical_reports_processed` that will be used to store the processed data generated by the fist step of the CogStack pipeline:
```sql

CREATE TABLE medical_reports_processed (
	cid INTEGER REFERENCES medical_reports, --(*)
	dct TIMESTAMP NOT NULL,
	output TEXT
) ;
```
The field `output` will contain the output of Tika documents processor in **JSON** format. It's important to note that `cid` field references the `medical_reports` table so that the document data will remain properly linked.

Following, we define an **optional** `reports_processed_view` which can be used to query the processed documents or to check the documents processing status:
```sql
CREATE VIEW reports_processed_view AS
	SELECT 
		cid,
		dct,
		output::JSON ->> 'X-PDFPREPROC-OCR-APPLIED' AS ocr_status,
		output::JSON ->> 'tika_output' AS tika_output
	FROM
		medical_reports_processed
	;
```
As mentioned previously, the output content is stored in **JSON** format, hence we need to access the information by key-value parsing the JSON content. There are more fields available for querying, however, in this simple example we only focus on:
- `tika_output` -- a field that contains the Tika-parsed document,
- `X-PDFPREPROC-OCR-APPLIED` -- a field that contains the status of OCR processor (if it was applied).

This view is **optional**, but can be used for debugging purposes.


### Observations view

Finally, we define `observations_view` which will be used in the final step to ingest the records data into ElasticSearch:

```sql


CREATE VIEW observations_view AS
	 SELECT

		-- ...

		doc_bin.cid AS document_id,
		doc_bin.sampleid AS document_sample_id,
		doc_bin.typeid AS document_type_id,
		doc_bin.dct AS document_timestamp,
		doc_bin.filename AS document_filename,

		doc_proc.output::JSON ->> 'X-PDFPREPROC-OCR-APPLIED' AS document_ocr_status,
		doc_proc.output::JSON ->> 'tika_output' AS document_tika_output
	FROM 
		patients p, 
		encounters enc,
		observations obs,
		medical_reports doc_bin,
		medical_reports_processed doc_proc
	WHERE 
		enc.patient = p.id AND
		obs.patient = p.id AND 
		obs.encounter = enc.id AND
		enc.documentid = doc_bin.cid AND
		doc_proc.cid = doc_bin.cid
	;
```

The view allows to query the patient data as in the previous examples. The querying of Tika-parsed document content is defied on a similar basis as in `reports_processed_view`. The view also allows to query for the information included with the original document, but skipping the binary content.


## Properties files

For each data processing step, a separate *properties* file is provided.

### Step 1 -- documents pre-processing using Tika

The *properties* files used in this step is based on [Example 4](#example-4) -- see `examples/example5/cogstack/conf/step-1/reports.properties` for the definition. However, as a data sink it is using a PostgreSQL database (actually, the same as the source one).

#### Spring profiles

The spring profiles used in this step are:
```properties
spring.profiles.active = jdbc_in,jdbc_out,tika,localPartitioning
```
In general, this tells us that the documents will be read from an input database (profile: `jdbc_in`), processed using `tika` with `localPartitioning` scheme and stored in an output database (profile: `jdbc_out`).

#### Data source and data sink

The source and target database are specified as follows:
```properties
source.JdbcPath      = jdbc:postgresql://samples-db:5432/db_samples
source.Driver        = org.postgresql.Driver
source.username      = test
source.password      = test

target.JdbcPath      = jdbc:postgresql://samples-db:5432/db_samples
target.Driver        = org.postgresql.Driver
target.username      = test
target.password      = test
```

The data source and target binding for CogStack engine is defined as follows:
```properties
source.primaryKeyFieldValue = cid
source.timeStamp = dct

source.selectClause = SELECT *
source.fromClause = FROM medical_reports
source.sortKey = cid

target.Sql = INSERT INTO medical_reports_processed (cid, dct, output) VALUES ( CAST( :primaryKeyFieldValue AS integer ), :timeStamp, :outputData)
```
In this first data processing step we are going to read the data from `medical_reports` -- as provided for `source.fromClause`. 

Moreover, we also need to define the `INSERT` clause for the property `target.Sql` which tells CogStack engine how to write the processed documents into the target database. This is required when using database as a data sink.


#### Tika configuration

```properties
tika.binaryFieldName = binarydoc
tika.tikaFieldName = tika_output
tika.binaryContentSource = database
```
The property `tika.tikaFieldName` denotes the name of the key field `tika_output` in the output JSON file where the value will contain the content of the Tika-parsed document (optional value). See, e.g., the `reports_processed_view` where the content of `tika_output` is accessed and parsed.

The property `tika.binaryFieldName` denotes the name of the column that contains the binary document data -- in our case it is the `binarydoc` field in `reports_binary_view` view.


### Partitioner configuration

Since in this example we will be processing records from `medical_reports` table, we need to instruct the partitioner adequately:
```properties
partitioner.partitionType = PKTimeStamp
partitioner.tableToPartition = medical_reports
partitioner.pkColumnName = cid
partitioner.timeStampColumnName = dct
```


### Step 2 -- records ingestion into ElasticSearch

The *properties* file used in this example is similar to the one from [Example 1](#example-1) -- see `examples/example5/cogstack/conf/step-2/observations.properties` for the definition. It is a pretty basic one and won't be covered here.


## Deployment information

When running `setup.sh` script, a number of separate directories will be created, each corresponding to a document format use-case.

Apart from that, this example uses a standard stack of microservices and runs only one instance of CogStack data processing engine. However, since this example implements 2-step processing, two CogStack instances are run, but in a sequential manner. Firstly, CogStack pipeline is executed using `reports.properties` configuration file and after that it is run with supplied `observations.properties` file.



# <a name="example-6"></a> Example 6
[//]: # "-------------------------------------------------------------------------------------"

## General information

This example is based on [Example 2](#example-2) which extends it with basic security mechanisms implemented by [Nginx](https://www.nginx.com/) -- a web service that can be used as a load balancer, reverse proxy or HTTP cache. In this example, Nginx will be used as a reverse proxy providing a security layer used to connect to running microservices in CogStack ecosystem.


## Deployment information

To deploy the example (after running `setup.sh` script) just type in `__deploy/` directory:
```bash
docker-compose up
```

Assuming that everything is working fine, we should be able to connect to the running microservices as shown in [Example 2](#example-2). When accessing webservices and when asked for **credentials** the username is *test* with password *test*.


This example uses the standard stack of microservices (see: [CogStack ecosystem](#cogstack-ecosystem)), but extended with Nginx reverse proxy service and internal network. For a better control and isolation of the services, in Docker Compose file (`examples/example6/docker/docker-compose.override.yml`) we defined 2 networks: `esnet` and `public`. The `esnet` network will be used as a internal, private network for the data processing pipeline and services -- the access should be highly restricted. The `public` network will be used as a bridge to connect the services to the outside host. When deployed, Nginx will be running as an additional microservice in the CogStack ecosystem using both `esnet` and `public` networks. It will control the communication between selected running microservices (`elasticsearch-1` and `kibana`) and the outside world. The only one difference here is the `samples-db` database service, which for debugging purposes is using both networks and have ports directly exposed and bound to `localhost:5555`. 

The picture below illustrates such deployment scenario.

![cogstack-ecosystem-nginx]({{ site.url }}/assets/uservices-nginx.png "CogStack ecosystem")


## Nginx

### Security

As mentioned previously, in this example, Nginx is used only as a reverse proxy service providing a simple security layer used to connect the running microservices inside the private network to the outside world. It implements a simple secure HTTP access to `kibana` and `elasticsearch-1` services running at `5601` and `9200` ports respectively. Configured with Docker Compose file, all the HTTP traffic coming to the host on the specified ports will be forwarded to the respective microservices running inside private network through Nginx.


### Configuration file

In this example, Nginx only implements basic HTTP access authentication. The authentication is based on a preconfigured `.htpasswd` file, as specified in Nginx configuration file `docker-cogstack/nginx/conf/nginx.conf`. The `.htpasswd` file with login credentials `test:test` will be automatically created when running `setup.sh` script. Although the security is very basic here, it can be easily extended to handle other protocols and access patterns. For more information about possible Nginx security configurationsm, please refer to the official [Nginx documentation]().


### ElasticSearch X-Pack Security

In this example, Nginx provides just a basic security layer for the running microservices and is free to use. However, for a more advanced functionality related with secure access to ElasticSearach microservices stack, the [X-Pack Security](https://www.elastic.co/guide/en/elastic-stack-overview/current/xpack-security.html) module can be considered. X-Pack Security, although requiring a [commercial license](https://www.elastic.co/subscriptions), offers functionality, such as:

- user authentication inside ElasticSearch and Kibana,
- role-based and attribute-based control for the data access,
- field- and document-level security,
- encryption of the communication between ES nodes.

For a detailed list of features, please refer to the official [ElasticSearch X-Pack documentation](https://www.elastic.co/guide/en/elastic-stack-overview/current/xpack-security.html).



# <a name="example-7"></a> Example 7
[//]: # "-------------------------------------------------------------------------------------"

## General information

This example is an extension of [Example 6](#example-6) providing logging mechanism using [Fluentd](https://www.fluentd.org/) log collector and it only focuses on the logging part.


[//]: # "<span style='color:red'> NOTE: </span>"
**Note: For the moment, the Docker images used in this example are: `cogstacksystems/cogstack-pipeline:dev-latest` and `cogstacksystems/fluentd:dev-latest`. These images are build from the `dev` branch. However, once the development branch will be merged to `master` the image names will be updated.**



## Deployment information

This example uses the stack of microservices used in [Example 6](#example-6), but extending it with Fluentd logging driver. When deployed, Fluend will be running as an additional microservice in order to collect and filter logs from the ones running in CogStack ecosystem. The picture below illustrates such deployment scenario.

![cogstack-ecosystem-fluentd]({{ site.url }}/assets/uservices-fluentd.png "CogStack ecosystem")



Regarding Docker Compose configuration file, for each microservice used an additional section has been added regarding logging -- e.g., in case of CogStack pipeline:
```yml
  cogstack:
    image: cogstacksystems/cogstack-pipeline:dev-latest
    
    ...
    
    logging:
      driver: "fluentd"
      options:
        tag: cog.java.engine
```

`"fluentd"` is used as the logging `driver`. All the messages from the `cogstack` microservice will be forwarded to the fluentd driver using `cog.java.engine` as `tag`. The directory with the output logs from fluentd running container will be mapped to a local path in the deployment directory: `examples/example7/__deploy/__logs`. For the full configuration of running microservices, please refer to `examples/example7/docker/docker-compose.override.yml`.


## Fluentd

### Custom image

In our setup, Fluend needs some additional filter plugins to be installed, hence a custom Fluentd image is used (as specified in the Docker Compose file). This image is available to download directly from CogStack Dockerhub under the name `cogstacksystems/fluentd`. Alternatively, the image can be build locally using the Dockerfile in the directory `docker-cogstack/fluentd` in the CogStack package.

### Configuration file

Fluentd uses configuration files to define the filtering and output rules for messages coming from predefined source(s). In our current setup, the Fluentd driver is listening at `localhost` on port `24224` for the incoming messages, as defined both in the Docker Compose file and Fluentd configuration files. The default configuration file for Fluentd is `docker-cogstack/fluentd/conf/fluent.conf` and the same one is used in this example (`examples/docker-common/fluentd/conf/fluent.conf`). However, here we won't go too much into logging configuration details and just cover the most important bits.


In this example, all the running microservices output the messages to standard output or standard error. However, not all the messages will be displayed to the end-user. They are firstly forwarded to fluentd driver using a separate tag per each microservice as defined in the Docker Compose file. The general rule applied here is that all the messages which have been previously sent to `stderr` will be classified as error messages, where as the ones sent to `stdout` -- as informative ones. 

After filtering and parsing, all the messages are output into files, separate per each service tag. The logs containing only error messages are stored in files starting with `erorr.*` prefix, whereas the full logs are stored in files starting with `full.*` prefix. The log files are managed using log rotating policy, keeping new logs for 24 hours, before being archived. 

As a side note, since the error messages are of high importance, on arrival they are additionally printed to the standard output for the user's instant inspection.


### Output logs format

The logs are output to files in JSON format and they contain such fields:
- `container_id` and `container_name` of the running microservice,
- `log` message,
- `time` of the message arrival.

To parse the logs, one easy way is to use [jq](https://stedolan.github.io/jq/) -- a flexible JSON command-line processor. For example, to parse the `log` message field, one may use:
```bash
jq ".log" example7/__deploy/__logs/<filename>.log
```
where `<filename>` is the filename of a sample log file.



# <a name="example-8"></a> Example 8
[//]: # "-------------------------------------------------------------------------------------"

## General information

This example covers a simple use-case of running NLP applications as one of the components of the data processing pipeline. The example application is using [GATE](https://gate.ac.uk/) suite as the NLP engine. The database schema used in this example is based on [Example 2](#example-2).

[//]: # "<span style='color:red'> NOTE: </span>"
**Note: For the moment, the example application is based on the GATE version < 8.5. The GATE in version >= 8.5 introduced a significant change in resources handling and as CogStack uses the previous version of GATE library it needs to be updated in the upcoming release.**


## Example GATE application

CogStack pipeline allows to include custom user GATE applications as one of the data processing components. These applications can be previously designed in GATE suite (e.g., using GATE Developer GUI application) and exported as a custom GATE application (with *.gapp* or *.xgapp* extension) with the necessary resources. CogStack implements *GateService* which uses GATE API to run these applications using the GATE Embedded version.


### GATE ANNIE Gazetteer

In this example, we developed a simple GATE application to annotate common drugs and medications. The application is using the GATE ANNIE plugin (with default configuration), implementing a custom version of [ANNIE Gazetteer](https://gate.ac.uk/sale/tao/splitch13.html). The application has been created in GATE Developer studio and exported into GATE application format. This application is hence ready to be used by GATE and is stored in `gate/app` directory as `drug.gapp` with the necessary resources.

The list of drugs and medications to annotate is based on a publicly available list of FDA-approved drugs and active ingredients. The data can be downloaded directly from [Drugs@FDA database](https://www.fda.gov/drugs/informationondrugs/ucm079750.htm). The list of used drugs is stored as `drug.lst` and active ingredients as `active.lst`.

Please note that this is a pretty basic example using only ANNIE Gazetteer for extracting drug annotations using names present in the provided resources. The example could be further extended with support for drug ontologies, taxonomies and/or algorithms for performing synonym aggregation. These functionalities, however, would be implemented using different GATE plugins and possibly chained together forming an NLP pipeline.

More information about creating custom GATE applications can be found in the official [GATE documentation](https://gate.ac.uk/documentation.html).

### Data preparation

This example is a basic one as our NLP application is not fully context aware and is looking in the text only for the words found in the provided list of drugs. Moreover, as the list of drugs and active ingredients can contain full names of drugs (or ingredients), the full names of drugs may not be found in the parsed text. Hence, the FDA-approved list of drugs and components needs to be further post-processed and filtered. All this is being performed by script `extra/clean_list.py` in the example directory. It uses as an input the raw data downloaded from Drugs@FDA database and a list of words to filter out (e.g., the most frequent 10000 words in English) to prepare a curated list of drugs and active ingredients.

[//]: # "<span style='color:red'> NOTE: </span>"
**Note: For a more reliable list of drugs one should better refer to [UMLS RxNorm](https://www.nlm.nih.gov/research/umls/rxnorm/) resource, however a special UMLS license applies for the usage and/or redistribution of that resource.**


## Properties file

The *properties* file used in this example is based on the one from [Example 2](#example-2), but extended with parts using GATE documents processor and which are covered below.


### Spring profiles

The spring profile part has been updated with adding a `gate` profile:
```properties
spring.profiles.active = jdbc_in,elasticsearchRest,gate,localPartitioning
```

### GATE configuration

A new part covering documents processing using a custom GATE application has been added:
```properties
gate.gateHome = /gate/home/
gate.gateApp = /gate/app/drug.gapp
gate.fieldsToGate = encounter_document
gate.gateAnnotationTypes = Drug
gate.gateFieldName = gate
```
The property `gate.gateHome` denotes the home directory of GATE application, which should be the same for all GATE applications when using CogStack GATE image from Dockerhub (please see below). `gate.gateApp` denotes the name of the GATE application to be run -- in our example, the application directory (containing the `drug.gapp` with the necessary resources) will be directly mounted into CogStack container into `/gate/app/` directory.

The property `gate.fieldsToGate` specifies the name of the field from the input database table that contains the text to be processed by the GATE application. The property `gate.gateAnnotationTypes` specifies the annotations to be extracted (available in the GATE application). Finally, the property `gate.gateFieldName` defines the custom name of the key in the resulting JSON file (or output table) under which the extracted annotations will be stored.


## Deployment information

When running `setup.sh` script, a number of separate directories will be created. Since NLP components for pipelines require additional applications to be installed in the system, CogStack provides another, extended image containing them. In this example, hence `cogstacksystems/cogstack-pipeline-gate:dev-latest` image is being used.

Apart from that, this example uses the standard stack of microservices (see: [CogStack ecosystem](#cogstack-ecosystem)) and also uses a single CogStack *properties* file, running only one instance of CogStack data processing engine.

[//]: # "<span style='color:red'> NOTE: </span>"
**Note: For the moment, the CogStack Docker image used in this example is `cogstacksystems/cogstack-pipeline-gate:dev-latest`. This image are build from the `dev` branch. However, once the development branch will be merged to `master` the image name will be updated.**



# <a name="example-9"></a> Example 9
[//]: # "-------------------------------------------------------------------------------------"

## General information

This example covers a use-case of running a pipeline of multiple components combined together:
- extraction of records from the input database that additionally contain free-text documents in PDF format,
- text extraction from the PDF document using [Apache Tika](#https://tika.apache.org/),
- annotation extraction using [GATE](https://gate.ac.uk/) suite,
- generating output documents in JSON format and storing them in ElasticSearch sink.

The example is based on [Example 4](#example-4), which uses Apache Tika to extract text from PDF documents and on [Example 8](#example-8) which uses simple GATE NLP application to annotate drugs and active ingredients in medical text.

[//]: # "<span style='color:red'> NOTE: </span>"
**Note: For the moment, the example application is based on the GATE version < 8.5. The GATE in version >= 8.5 introduced a significant change in resources handling and as CogStack uses the previous version of GATE library it needs to be updated in the upcoming release.**


## Properties file

The *properties* file used in this example is based on both [Example 4](#example-4) and [Example 8](#example-8). The Spring active profiles used are both `tika` and `gate`. However, the most important bit is to specify how the document text is passed from Tika processor to GATE document processor:
```properties
## TIKA CONFIGURATION
##
#...
tika.binaryFieldName = encounter_binary_doc
tika.tikaFieldName = tika_output
#...

##### GATE CONFIGURATION
##
# ...
gate.fieldsToGate = tika_output
gate.gateFieldName = gate
# ...
```

Tika item processor will extract the text from the document initially stored in binary form in `encounter_binary_doc` field (property: `tika.binaryFieldName` ; see [Example 4](#example-4) for the DB schema). Then, it will store the extracted text in a `tika_output` field (property: `tika.tikaFieldName`) in the Document model. The GATE application will then read the text from `tika_output` field (property: `gate.fieldsToGate`), process it and store the extracted annotations in `gate` field (property: `gate.gateFieldName`) in the Document model.  At the end of processing of the record, a resulting JSON with all the available fields will be generated and send to ElasticSearch.


## Deployment information

When running `setup.sh` script, a number of separate directories will be created. Since NLP components for pipelines require additional applications to be installed in the system, CogStack provides another, extended image containing them. In this example, hence `cogstacksystems/cogstack-pipeline-gate:dev-latest` image is being used.

Apart from that, this example uses the standard stack of microservices (see: [CogStack ecosystem](#cogstack-ecosystem)) and also uses a single CogStack *properties* file, running only one instance of CogStack data processing engine.

[//]: # "<span style='color:red'> NOTE: </span>"
**Note: For the moment, the CogStack Docker image used in this example is `cogstacksystems/cogstack-pipeline-gate:dev-latest`. This image are build from the `dev` branch. However, once the development branch will be merged to `master` the image name will be updated.**
