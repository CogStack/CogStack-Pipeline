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
1. [Getting CogStack](#getting-cogstack)
2. [How are the examples organized](#how-they-are-organized)
2. [How does CogStack work](#how-does-it-work)
3. [Available datasets](#datasets)
4. [Running CogStack](#running-cogstack)
5. Detailed description of examples, which currently are:
* [Example 1](#example-1) -- processing a simple, structured dataset from a single DB source.
* [Example 2](#example-2) -- processing a semi-structured dataset from a single DB source (as in [CogStack Quickstart](https://github.com/CogStack/CogStack-Pipeline)).
* [Example 3](#example-3) -- processing a semi-structured dataset from multiple DB sources, multiple jobs.
* [Example 4](#example-4) -- processing a semi-structured dataset with embedded documents from a single DB source.
* [Example 5](#example-5) -- 2-step processing of a semi-structured dataset with embedded documents from a single DB source.
* [Example 6](#example-6) -- Example 2 extended with logging mechanisms.

The main directory with resources used in this tutorial is available in the the CogStack bundle under `examples` directory.

Some parts of this document are also used in [CogStack Quickstart](https://github.com/CogStack/CogStack-Pipeline) tutorial.



# <a name="getting-cogstack"></a> Getting CogStack
[//]: # "-------------------------------------------------------------------------------------"

The most convenient way to get CogStack bundle is to download it directly from the [official github repository](https://github.com/CogStack/CogStack-Pipeline) either by cloning it using git:

```bash
git clone -b sample_data --single-branch https://github.com/CogStack/CogStack-Pipeline.git
```
or by downloading it from the repository and decompressing it:
```bash
curl 'https://github.com/CogStack/CogStack-Pipeline/archive/sample_data.zip'
unzip sample_data.zip
```

[//]: # "<span style='color:red'> NOTE: </span>"
**Note: For the moment the CogStack bundle is obtained from the `sample_data` branch -- soon it will be merged into `master` branch with a version tag for a direct download.**



# <a name="how-they-are-organized"></a> How are the examples organized
[//]: # "-------------------------------------------------------------------------------------"

Each of the examples is organized in a way that it can be deployed and run independently. The directory structure of `examples` tree is as follows:
```tree
.
├── docker-common
│   ├── elasticsearch
│   │   └── config
│   │       └── elasticsearch.yml
│   ├── kibana
│   │   └── config
│   │       └── kibana.yml
│   ├── nginx
│   │   ├── auth
│   │   └── config
│   │       └── nginx.conf
│   ├── pgsamples
│   │   └── init_db.sh
│   └── postgres
│       └── create_repo.sh
│
├── example1
│   ├── cogstack
│   │   └── observations.properties
│   ├── db_dump
│   │   └── db_samples.sql.gz
│   ├── docker
│   │   └── docker-compose.yml
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
│   │   └── docker-compose.yml
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
│   │   └── docker-compose.yml
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
│   │   └── docker-compose.yml
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
│   │   └── docker-compose.yml
│   ├── extra
│   │   ├── db_create_schema.sql
│   │   ├── prepare_db.sh
│   │   └── prepare_single_db.sh
│   └── setup.sh
│
├── example6
│   ├── cogstack
│   │   ├── observations.properties
│   │   └── test2.sh
│   ├── db_dump
│   │   └── db_samples.sql.gz
│   ├── docker
│   │   └── docker-compose.yml
│   ├── extra
│   │   ├── db_create_schema.sql
│   │   └── prepare_db.sh
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

## Common and reusable components

The directory `docker-common` contains some common components and microservice configuration files that are used within all the examples (see [Running CogStack](#running-cogstack)). These components include:
* ElasticSearch node,
* Kibana webservice dashboard,
* nginx reverse proxy,
* PostgreSQL databases.


## Examples

The directories `example*` stores the content of the examples, each containing such subdirectories:
* `cogstack` directory containing CogStack configuration files and/or custom pipeline scripts,
* `db_dump` directory containing database dumps used to initialize the samples input database,
* `docker` directory containing configuration files for docker-based deployment,
* `extra` directory containing scripts to generate database dumps locally,
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
* `cogengine` -- CogStack data processing engine,
* `postgres` -- PostgreSQL database for storing information about CogStack jobs and status,
* `elasticsearch` -- ElasticSearch node(s) for storing and querying the processed EHR data,
* `kibana` -- Kibana data visualization tool for querying the data from ElasticSearch,
* `nginx` -- [nginx](https://www.nginx.com/) serving as reverse proxy for providing secure access to the services.

The Docker Compose file with configuration of these microservices can be found in each of the examples `docker` subdirectory: `examples/example*/docker/docker-compose.yml`.


## CogStack engine configuration

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
* `gate` NLP data processing service (*not covered by the examples*),
* `bioyodie` NLP data processing service (*not covered by the examples*),
* `pdfGeneration`, `thumbnailGeneration` -- PDF thumbnail generation service (*not covered by the examples*),
* `dBLineFixer` -- records modification process (*not covered by the examples*),
* `pdfbox` -- documents processing sugin PDFBox (*not covered by the examples*),
* `metadata` -- document metadata generation (*not covered by the examples*).


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

These datasets, although unrelated, are used together to compose a semi-structured dataset.


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

For the ease of use a database dump with predefined schema and preloaded data will be provided in each of the examples `examples/example*/db_dump/` directory. This way, the PostgreSQL database with sample data will be automatically initialized when deployed using Docker. The dabatase dumps can be directly downloaded from [Amazon S3](https://aws.amazon.com/s3) bucket by running in the main examples directory:
```bash
bash download_db_dumps.sh
```

Alternatively, the PostgreSQL database schema definitions are stored in `examples/example*/extra/` directories alongside the scripts to generate the database dumps locally. However, some examples may require pre-processed documents data to be available prior running -- the script `prepare_docs.sh` in the main examples takes care of that. The script `prepare_db_dumps.sh` is used to prepare locally all the database dumps to initialize the examples.



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

[//]: # "Connecting to ES, Kibana and PostgreSQL"
Assuming that everything is working fine, we should be able to connect to the running microservices. For the ease of access, selected running services (`elasticsearch` and `kibana`) have their port connections forwarded to `localhost` via `nginx` proxy. When accessing webservices and when asked for **credentials** the username is *test* with password *test*. 

### Kibana and ElasticSearch

Kibana dashboard used to query the EHRs can be accessed directly in browser via URL: `http://localhost:5601/`. The data can be queried using a number of ElasticSearch indices, e.g. `sample_observations_view`. Usually, each index will correspond to the database view in `db_samples` (`pgsamples` PostgreSQL database) from which the data was ingested.

In addition, ElasticSearch REST end-point can be accessed via URL `http://localhost:9200/`. It can be used to perform manual queries or to be used by other external services -- for example, one can list the available indices:
```bash
curl 'http://localhost:9200/_cat/indices'
```
or query one of the available indices -- `sample_observations_view`:
```bash
curl 'http://localhost:9200/sample_observations_view'
```

### PostgreSQL sample database

Moreover, the access PostgreSQL database with the input sample data is exposed directly at `localhost:5555` (skipping the `nginx` proxy). The database name is `db_sample` with user *test* and password *test*. To connect, one can run:
```bash
psql -U 'test' -W -d 'db_samples' -h localhost -p 5555
```


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

In the current implementation, CogStack engine can only partition the data using the records' primary key (`cog_pk` field, containing unique values) and records' update time (`cog_update_time` field) as defined in created views. This is specified by `PKTimeStamp` partitioning method types:
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
create table patients (
  ID uuid primary key,
  BIRTHDATE date, 
  DEATHDATE date, 
  SSN varchar(64), 
  DRIVERS varchar(64),
  PASSPORT varchar(64),
  PREFIX varchar(8),
  FIRST varchar(64),
  LAST varchar(64),
  SUFFIX varchar(8),
  MAIDEN varchar(64),
  MARITAL char(1),
  RACE varchar(64), 
  ETHNICITY varchar(64),
  GENDER char(1),
  BIRTHPLACE varchar(64),
  ADDRESS varchar(64),
  CITY varchar(64),
  STATE varchar(64),
  ZIP varchar(64)
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
create table encounters (
  ID uuid primary key,
  START timestamp,
  STOP timestamp,
  PATIENT uuid references patients,
  CODE varchar(64),
  DESCRIPTION text,
  COST real,
  REASONCODE varchar(64),
  REASONDESCRIPTION text
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
  create table observations (
  CID serial primary key,                   -- (*)
  DCT timestamp default current_timestamp,  -- (*)
  DATE date, 
  PATIENT uuid references patients,
  ENCOUNTER uuid references encounters,
  CODE varchar(64),
  DESCRIPTION text,
  VALUE varchar(64),
  UNITS varchar(64),
  TYPE varchar(64),
) ;
```

Here, with `-- (*)` have been marked additional fields with auto-generated values. These are: `CID` -- an automatically generated primary key and `DCT` -- a document creation timestamp. They will be later used by CogStack engine for data partitioning when processing the records. The `patient` and `encounters` tables have their primary keys (`ID` field) already defined (of `uuid` type) and are included in the input CSV files.


### Database views

Next, we define a `observations_view` that will be used by CogStack data processing engine to ingest the records from input database:
```sql
create view observations_view as
   select
    p.ID as patient_id, 
    p.BIRTHDATE as patient_birth_date,
    p.DEATHDATE as death_date,
    p.SSN as patient_SSN,
    p.DRIVERS as patient_drivers,
    p.PASSPORT as patient_passport,
    p.PREFIX as patient_prefix,
    p.FIRST as patient_first_name,
    p.LAST as patient_last_name,
    p.SUFFIX as patient_suffix,
    p.MAIDEN as patient_maiden,
    p.MARITAL as patient_marital,
    p.RACE as patient_race,
    p.ETHNICITY as patient_ethnicity,
    p.GENDER as patient_gender,
    p.BIRTHPLACE as patient_birthplace,
    p.ADDRESS as patient_addr,
    p.CITY as patient_city,
    p.STATE as patient_state,
    p.ZIP as patient_zip,

    enc.ID as encounter_id,
    enc.START as encounter_start,
    enc.STOP as encounter_stop,
    enc.CODE as encounter_code,
    enc.DESCRIPTION as encounter_desc,
    enc.COST as encounter_cost,
    enc.REASONCODE as encounter_reason_code,
    enc.REASONDESCRIPTION as encounter_reason_desc,

    obs.DATE as observation_date,
    obs.CODE as observation_code,
    obs.DESCRIPTION as observation_desc,
    obs.VALUE as observation_value,
    obs.UNITS as observation_units,
    obs.TYPE as observation_type,

    -- for CogStack compatibility
    'src_field_name'::text as cog_src_field_name,     -- (a)
    'observations_view'::text as cog_src_table_name,  -- (b)
    obs.CID as cog_pk,                                -- (c)
    'cog_pk'::text as cog_pk_field_name,              -- (d)
    obs.DCT as cog_update_time                        -- (e)
  from 
    patients p, 
    encounters enc,
    observations obs
  where 
    enc.PATIENT = p.ID and
    obs.PATIENT = p.ID and 
    obs.ENCOUNTER = enc.ID
  ;
```
The goal here is to denormalize the database schema for CogStack and ElasticSearch data ingestion, as the `observations` table is referencing both the `patient` and `encounters` tables by their primary key. In the current implementation, CogStack engine cannot yet perform dynamic joins over the relational data from specific database tables.

Apart from exposing the fields from the previously defined tables, some extra fields `cog_*` have been added. They are required for compatibility with CogStack data processing engine, but they may be possibly removed or modified in the upcoming version of CogStack. However, in the current implementation, these fields are required to properly configure the CogStack database reader, and to properly schedule and partition the data of the running CogStack data processing workers.

These additional fields are:
* `cog_src_field_name` -- related with processing the text documents (not used in this example),
* `cog_src_table_name` -- the name of the table (or view) containing records to process,
* `cog_pk` -- primary key value (or any unique) used for partitioning the data into batches (for the moment, needs to be of numeric type),
* `cog_pk_field_name` -- the name of the field in the current table/view containing the value of primary key values,
* `cog_update_time` -- the last update/modification time of the record, used for checking for new records and for partitioning.

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
* `localPartitioning` functionality.


### Data source

The parameters for specifying the data source are defined as follows:
```properties
source.JdbcPath = jdbc:postgresql://pgsamples:5432/db_samples
source.Driver = org.postgresql.Driver
source.username = test
source.password = test
source.poolSize = 10
```
In this example we are using a PostgreSQL database which driver is defined by `source.Driver` parameter. The PostgreSQL database service is available in the CogStack ecosystem as `pgsamples`, has exposed port `5432` for connections and the sample database name is `db_samples` -- all these details need to be included in the `source.JdbcPath` parameter field.


Next, we need to instruct CogStack engine how to query the records from the data source:
```properties
source.selectClause = SELECT *
source.sortKey = cog_pk
source.fromClause = FROM observations_view

source.srcTableName = cog_src_table_name
source.srcColumnFieldName = cog_src_field_name
source.primaryKeyFieldName = cog_pk_field_name
source.primaryKeyFieldValue = cog_pk
source.timeStamp = cog_update_time

source.dbmsToJavaSqlTimestampType = TIMESTAMP
```
This is where the previously defined `observations_view` with additional CogStack-specific fields are used.


### Data sink

Next, we need to define the data sink -- in our example, and by default, ElasticSearch is being used:
```properties
elasticsearch.cluster.name = elasticsearch
elasticsearch.cluster.host = elasticsearch
elasticsearch.cluster.port = 9200

elasticsearch.security.enabled = false
elasticsearch.ssl.enabled = false
```
Similarly, as when defining the sample database source, we need to provide the ElasticSearch host and port configuration according to the microservices definition in the corresponding Docker Compose file (see `examples/example1/docker/docker-compose.yml`).


In the next step, we specify the ElasticSearch indexing parameters:
```properties
elasticsearch.index.name = sample_observations_view
elasticsearch.type = doc
elasticsearch.excludeFromIndexing = cog_pk,cog_pk_field_name,cog_src_field_name,cog_src_table_name
elasticsearch.datePattern = yyyy-MM-dd'T'HH:mm:ss.SSS
```
We specify the index name which will be used to store the documents processed by CogStack engine. Additionally, we specify which fields should be excluded from the indexing -- by default, we exclude the binary content, the constant-value fields and the primary key from the `observations_view`.


As a side note, although it's mostly for testing purposes at the moment, the same information regarding the source database host and credentials needs to be provided for defining the target (in our case it is ElasticSearch anyway):
```properties
target.JdbcPath = jdbc:postgresql://pgsamples:5432/db_samples
target.Driver = org.postgresql.Driver
target.username = test
target.password = test
```
This is a concept that will be possibly redesigned in future versions of CogStack.


### Jobs and CogStack engine configuration

CogStack engine in order to coordinate the workers needs to keep the information about the current jobs in an additional PostgreSQL database -- `postgres`. Hence, similarly as when defining the source database, this database needs to specified:
```properties
jobRepository.JdbcPath = jdbc:postgresql://postgres:5432/cogstack
jobRepository.Driver = org.postgresql.Driver
jobRepository.username = cogstack
jobRepository.password = mysecretpassword

job.jobName = job_observations_view
```
The last parameter `job.jobName` is a default name for the jobs that will be created.


### Partitioner and scheduler

Another set of useful parameters are related with controlling the job execution and data partitioning:
```properties
partitioner.partitionType = PKTimeStamp
partitioner.tableToPartition = observations_view
partitioner.pkColumnName = cog_pk
partitioner.timeStampColumnName = cog_update_time
```

Apart from data partitioning, it can be useful to set up the scheduler -- the following line corresponds to the scheduler configuration:
```properties
scheduler.useScheduling = false
```
In this example we do not use the scheduler, since we ingest EHRs from the data source only once. However, in case when data is being generated in a continuous way, scheduler should be enabled to periodically run CogStack jobs to process the new EHRs.


## Deployment information

This example uses the standard stack of microservices as presented in [CogStack ecosystem](#cogstack-ecosystem) with the Docker Compose file `examples/example1/docker/docker-compose.yml`.

It also uses a single CogStack *properties* file (see `examples/example1/cogstack/observations.properties`) and hence runs only one instance of CogStack data processing engine.



# <a name="example-2"></a> Example 2
[//]: # "-------------------------------------------------------------------------------------"

## General information

This example is an extension of [Example 1](#example-1). Apart from containing structured synthetic data, it also contains free-text documents data, hence creating a semi-structured dataset. 

This example is also covered as a main part of [CogStack Quickstart](https://github.com/CogStack/CogStack-Pipeline) tutorial.


## Database schema

The database schema is almost the same as the one defined in [Example 1](#example-1). The only difference is an additional column in `encounters` table, as presented below.

### Encounters table

The `encouters` table definition:

```sql
create table encounters (
  ID uuid primary key,
  START timestamp,
  STOP timestamp,
  PATIENT uuid references patients,
  CODE varchar(64),
  DESCRIPTION text,
  COST real,
  REASONCODE varchar(64),
  REASONDESCRIPTION text,
  DOCUMENT text -- (*)
) ;
```
Here, with `-- (*)` has been marked an additional `DOCUMENT` column field. This extra field will be used to store the content of a document from [MTSamples dataset](#samples-mt). 

Just to clarify, [Synthea-based](#samples-syn) and [MTSamples](#samples-mt) datasets are two unrelated datasets. Here, we are extending the synthetic dataset with the clinical documents from the MTSamples to create a semi-structural one, to be able to perform a bit more interesting queries.

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

Analogously, the new `DOCUMENT` column field is included in the `observations_view`, where the view is based on the one defined in [Example 1](#example-1).


## Properties file

The *properties* file used in this example is the same as in [Example 1](#example-1).


## Deployment information

Similarly as in Example 1, this one uses the standard stack of microservices defined in `examples/example2/docker/docker-compose.yml`. 

It also uses a single CogStack *properties* file and hence runs only one instance of CogStack data processing engine (see: `examples/example2/cogstack/observations.properties`).



# <a name="example-3"></a> Example 3
[//]: # "-------------------------------------------------------------------------------------"

## General information

This example covers a case of multiple data sources and multiple CogStack instances scenario. This example is a further extension of both [Example 1](#example-1) and [Example 2](#example-2). It extends Example 1 by defining schema for all the tables for [Synthea-based](#samples-syn) patient data. It also extends Example 2 by defining a separate table for representing and storing [MTSamples](#samples-mt) data.


## Database schema

The database schema is based on the one defined in [Example 1](#example-1) where the same definition logic has been applied to the rest of CSV files available in the synthetic dataset. The complete database schema for the synthetic data is available in `examples/example3/extra/db_create_syn_schema.sql` file. 

The only new table is the one for representing MTSamples data defined in `examples/example3/extra/db_create_mt_schema.sql`.


### Samples table and view

The definition of `samples` table and its corresponding view:

```sql
create table samples (
  CID serial primary key,                   -- for CogStack compatibility
  DCT timestamp default current_timestamp,  -- (*)
  SAMPLE_ID integer not null,
  TYPE varchar(256) not null,
  TYPE_ID integer not null,
  NAME varchar(256) not null,
  DESCRIPTION text not null,
  DOCUMENT text not null
) ;

create view samples_view as 
  select 
    samples.*,
    'src_field_name'::text as cog_src_field_name,   -- for CogStack compatibility
    'samples_view'::text as cog_src_table_name,     -- (*)
    samples.CID as cog_pk,                          -- (*)
    'cog_pk'::text as cog_pk_field_name,            -- (*)
    samples.DCT as cog_update_time                  -- (*)
  from 
    samples 
  ;
```

In contrast to MTSamples data representation used in [Example 2](#example-2) (where the full content of a document was stored in the `DOCUMENT` field in the `encounters` table), in this example we partially parse the document, hence improving the data representation. 

Please refer to `examples/example3/extra/prepare_synsamples_db.sh` on how the synthetic data is parsed and `examples/example3/extra/prepare_synsamples_db.sh` on how the MTSamples data is parsed.


## Properties files

The *properties* files used in this example are based on the one from [Example 1](#example-1). However, since multiple views are defined, the *properties* files can be automatically generated based on a provided `template.properties` file by running in `examples/example3/cogstack/` directory:
```bash
bash gen_config.sh
```
This way, each generated *properties* file corresponds to an individual view as defined in the database schema. 

Apart from that, a separate `mt.properties` file is provided for processing MTSamples data as defined in `samples_view`.


## Deployment information

This example uses the standard stack of microservices (see: [CogStack ecosystem](#cogstack-ecosystem)), but extended with additional database storing input sample data. It uses 2 separate input databases as the data source: `pgmtsamples` and `pgsynsamples` -- see: `examples/example3/docker/docker-compose.yml`.

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
create table encounters (
  ID uuid primary key,
  START timestamp,
  STOP timestamp,
  PATIENT uuid references patients,
  CODE varchar(64),
  DESCRIPTION text,
  COST real,
  REASONCODE varchar(64),
  REASONDESCRIPTION text,
  BINARYDOCUMENT bytea -- (*)
) ;
```

In this example, the document is stored in column `BINARYDOCUMENT` of `bytea` type -- instead of `DOCUMENT` as raw `text` defined in [Example 2](#example-2).


### Observations view

```sql
create view observations_view as
   select

    ...

    -- for CogStack compatibility
    'src_field_name'::text as cog_src_field_name,
    'observations_view'::text as cog_src_table_name,
    obs.CID as cog_pk,
    'cog_pk'::text as cog_pk_field_name,
    obs.DCT as cog_update_time,
    enc.BINARYDOCUMENT as cog_binary_doc              -- (*)
  from 
    patients p, 
    encounters enc,
    observations obs
  where 
    enc.PATIENT = p.ID and
    obs.PATIENT = p.ID and 
    obs.ENCOUNTER = enc.ID
  ;
```

The additional field added in this view is `cog_binary_doc` which will be used to read the binary document by CogStack engine.


## Properties file

The *properties* file used in this example is based on the one from [Example 2](#example-2), but extended with parts covering Tika documents processor and which are covered below.


### Spring profiles

The spring profile part has been updated with adding a `tika` profile:
```properties
spring.profiles.active=jdbc_in,elasticsearchRest,localPartitioning,tika
```


### Tika configuration

A new part covering Tika processing has been added:
```properties
tika.tikaFieldName = tika_output
tika.binaryContentSource = database
tika.binaryFieldName = cog_binary_doc
```
The property `tika.tikaFieldName` denotes the name of the key field `tika_output`. This field will be present in the output JSON file where the value will hold the content of the Tika-parsed document.

The property `tika.binaryContentSource` defines the source where the documents are stored -- in our case: `database`. Following, the property `tika.binaryFieldName` denotes the name of column that contains binary document data -- in our case that is `cog_binary_doc` field in `observations_view` view.

It's important to note that the remaining information about mapping and querying of the source database tables and record fields are covered by `source.*` properties, as explained in [Example 1](#example-1).


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
create table encounters (
  CID serial,
  ID uuid primary key,
  START timestamp,
  STOP timestamp,
  PATIENT uuid references patients,
  CODE varchar(64),
  DESCRIPTION varchar(256),
  COST real,
  REASONCODE varchar(64),
  REASONDESCRIPTION varchar(256),
  DOCUMENTID integer                      -- (*)
) ;
```

In this example, we only store the ID of the document in `DOCUMENTID` field.


### Medical reports -- binary documents

Next, we define `medical_reports` table on a similar basis as `samples` table used in Example 3:

```sql
create table medical_reports (
  CID integer primary key,
  SAMPLEID integer,
  TYPEID integer,
  DCT timestamp,
  FILENAME varchar(256),
  BINARYDOC bytea
) ;
```
In this example, the document is stored in binary format in `BINARYDOC` field. 


Following, we define the `reports_binary_view` view to query the data:
```sql
create view reports_binary_view as 
  select 
    CID,
    SAMPLEID,
    TYPEID,
    DCT,
    FILENAME,

    -- for CogStack compatibility -- meta-data
    'BINARYDOC'::text as cog_src_field_name,            -- (*)
    'reports_binary_view'::text as cog_src_table_name,  -- (*)
    CID as cog_pk,                                      -- (*)
    'cog_pk'::text as cog_pk_field_name,                -- (*)
    DCT as cog_update_time,                             -- (*)
    BINARYDOC as cog_binary_doc                         -- (*)
  from 
    medical_reports 
  ;
```
Similarly, as in Example 4, the column `cog_binary_doc` will be used to access the binary content of the document. However, here we are only interested in processing the `medical_reports` table, as in Example 3.



### Medical reports -- processed documents

Next, we define `medical_reports_processed` that will be used to store the processed data generated by the fist step of the CogStack pipeline:
```sql
create table medical_reports_processed (
  CID integer references medical_reports,
  DCT timestamp,
  OUTPUT text
) ;
```
The field `OUTPUT` will contain the output of Tika documents processor in **JSON** format. It's important to note that `CID` field references the `medical_reports` table so that the document data will remain properly linked.

Following, we define an **optional** `reports_processed_view` which can be used to query the processed documents or to check the documents processing status:
```sql
create view reports_processed_view as
  select 
    CID,
    DCT,
    OUTPUT::json ->> 'X-PDFPREPROC-OCR-APPLIED' as OCR_STATUS,
    OUTPUT::json ->> 'tika_output' as TIKA_OUTPUT
  from
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
create view observations_view as
   select

    ...

    doc_bin.CID as document_id,
    doc_bin.SAMPLEID as document_sample_id,
    doc_bin.TYPEID as document_type_id,
    doc_bin.DCT as document_dct,
    doc_bin.FILENAME as document_filename,

    doc_proc.OUTPUT::json ->> 'X-PDFPREPROC-OCR-APPLIED' as document_ocr_status,
    doc_proc.OUTPUT::json ->> 'tika_output' as document_tika_output,

    -- for CogStack compatibility
    'document_tika_output'::text as cog_src_field_name,     -- (*)
    'observations_view'::text as cog_src_table_name,        -- (*)
    obs.CID as cog_pk,                                      -- (*)
    'cog_pk'::text as cog_pk_field_name,                    -- (*)
    obs.DCT as cog_update_time                              -- (*)
  from 
    patients p, 
    encounters enc,
    observations obs,
    medical_reports doc_bin,
    medical_reports_processed doc_proc
  where 
    enc.PATIENT = p.ID and
    obs.PATIENT = p.ID and 
    obs.ENCOUNTER = enc.ID and
    enc.DOCUMENTID = doc_bin.CID and
    doc_proc.CID = doc_bin.CID
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
spring.profiles.active=jdbc_in,jdbc_out,localPartitioning,tika
```
In general, this tells us that the documents will be read from an input database (profile: `jdbc_in`), processed using `tika` with `localPartitioning` scheme and stored in an output database (profile: `jdbc_out`).

#### Data source and data sink

The source and target database are specified as follows:
```properties
source.JdbcPath      = jdbc:postgresql://pgsamples:5432/db_samples
source.Driver        = org.postgresql.Driver
source.username      = test
source.password      = test

target.JdbcPath      = jdbc:postgresql://pgsamples:5432/db_samples
target.Driver        = org.postgresql.Driver
target.username      = test
target.password      = test
```

The data source and target binding for CogStack engine is defined as follows:
```properties
source.srcTableName = cog_src_table_name
source.srcColumnFieldName = cog_src_field_name
source.primaryKeyFieldName = cog_pk_field_name
source.primaryKeyFieldValue = cog_pk
source.timeStamp = cog_update_time

source.selectClause = SELECT *
source.sortKey = cog_pk
source.fromClause = FROM reports_binary_view

target.Sql = INSERT INTO medical_reports_processed (cid, dct, output) VALUES ( CAST( :primaryKeyFieldValue AS integer ), :timeStamp, :outputData)
```
In this first data processing step we are going to read the data from `reports_binary_view` -- as provided for `source.fromClause`. 

Moreover, we also need to define the `INSERT` clause for the property `target.Sql` which tells CogStack engine how to write the processed documents into the target database. This is required when using database as a data sink.


#### Tika configuration

```properties
tika.binaryFieldName = cog_binary_doc
tika.tikaFieldName = tika_output
tika.binaryContentSource = database
```
The property `tika.tikaFieldName` denotes the name of the key field `tika_output` in the output JSON file where the value will contain the content of the Tika-parsed document. See, e.g., the `reports_processed_view` where the content of `tika_output` is accessed and parsed.

The property `tika.binaryFieldName` denotes the name of the column that contains the binary document data -- in our case it is the `cog_binary_doc` field in `reports_binary_view` view.



### Step 2 -- records ingestion into ElasticSearch

The *properties* file used in this example is similar to the one from [Example 1](#example-1) -- see `examples/example5/cogstack/conf/step-2/observations.properties` for the definition. It is a pretty basic one and won't be covered here.


## Deployment information

When running `setup.sh` script, a number of separate directories will be created, each corresponding to a document format use-case.

Apart from that, this example uses a standard stack of microservices and runs only one instance of CogStack data processing engine. However, since this example implements 2-step processing, two CogStack instances are run, but in a sequential manner. Firstly, CogStack pipeline is executed using `reports.properties` configuration file and after that it is run with supplied `observations.properties` file.





# <a name="example-6"></a> Example 6
[//]: # "-------------------------------------------------------------------------------------"

## General information

This example is an extension of [Example 2](#example-2) providing logging mechanism using [Fluentd](https://www.fluentd.org/) log collector and it only focuses on the logging part.


[//]: # "<span style='color:red'> NOTE: </span>"
**Note: For the moment, the Docker containers used in this example: `cogstack-pipeline` and `fluentd` are not yet available to pull the official CogStack Dockerhub repository. These images need to be build locally. The scripts to build these images are available in the main CogStack bundle in the directory `localBuild/`.**



## Deployment information

This example uses the standard stack of microservices (see: [CogStack ecosystem](#cogstack-ecosystem)), but extended with Fluentd logging driver. When deployed, Fluend will be running as an additional microservice in order to collect and filter logs from the ones running in CogStack ecosystem.

For each microservice used an additional section has been added regarding logging -- e.g., in case of CogStack engine:
```yml
  cogstack:
    image: cogstacksystems/cogstack-pipeline:latest
    
    ...
    
    logging:
      driver: "fluentd"
      options:
        tag: cog.java.engine
```

`"fluentd"` is used as the logging `driver`. All the messages from the `cogstack` microservice will be forwarded to the fluentd driver using `cog.java.engine` as `tag`. The directory with the output logs from fluentd running container will be mapped to a local path in the deployment directory: `examples/example6/__deploy/__logs`. For the full configuration of running microservices, please refer to `examples/example6/docker/docker-compose.yml`.


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
jq ".log" example6/__deploy/__logs/<filename>.log
```
where `<filename>` is the filename of a sample log file.