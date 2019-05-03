/* 
	creates schema of the DB for CogStack

Uses schema specified by:
    https://github.com/synthetichealth/synthea/wiki/CSV-File-Data-Dictionary

*/

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

CREATE TABLE medical_reports (
	cid INTEGER PRIMARY KEY,
	sampleid INTEGER NOT NULL,
	typeid INTEGER NOT NULL,
	dct TIMESTAMP NOT NULL,
	filename VARCHAR(256) NOT NULL,
	binarydoc BYTEA NOT NULL
) ;

CREATE TABLE medical_reports_processed (
	cid INTEGER REFERENCES medical_reports,
	dct TIMESTAMP NOT NULL,
	output TEXT
) ;

CREATE TABLE encounters (
	cid SERIAL NOT NULL,										-- for CogStack compatibility
	id UUID PRIMARY KEY,
	start TIMESTAMP NOT NULL,
	stop TIMESTAMP,
	patient UUID REFERENCES patients,
	code VARCHAR(64) NOT NULL,
	description VARCHAR(256) NOT NULL,
	cost REAL NOT NULL,
	reasoncode VARCHAR(64),
	reasondescription VARCHAR(256),
	documentid INTEGER											-- MTSamples document content
) ;

CREATE TABLE observations (
	cid SERIAL PRIMARY KEY,										-- for CogStack compatibility
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP + RANDOM() * INTERVAL '5 years',				-- (*)
	date DATE NOT NULL, 
	patient UUID REFERENCES patients,
	encounter UUID REFERENCES encounters,
	code VARCHAR(64) NOT NULL,
	description VARCHAR(256) NOT NULL,
	value VARCHAR(64) NOT NULL,
	units VARCHAR(64),
	type VARCHAR(64) NOT NULL
) ;



/*

  Create views for CogStack

*/
CREATE VIEW reports_processed_view AS
	SELECT 
		cid,
		dct,
		output::JSON ->> 'X-PDFPREPROC-OCR-APPLIED' AS ocr_status,
		output::JSON ->> 'tika_output' AS tika_output
	FROM
		medical_reports_processed
	;

CREATE VIEW observations_view AS
	 SELECT
		p.id AS patient_id, 
		p.birthdate AS patient_birth_date,
		p.deathdate AS death_date,
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

		obs.cid AS observation_id,
		obs.created AS observation_timestamp,
		obs.date AS observation_date,
		obs.code AS observation_code,
		obs.description AS observation_desc,
		obs.value AS observation_value,
		obs.units AS observation_units,
		obs.type AS observation_type,

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
