/* 
	creates schema of the DB for CogStack

Uses schema specified by:
    https://github.com/synthetichealth/synthea/wiki/CSV-File-Data-Dictionary

*/

CREATE DOMAIN KEY_TYPE AS UUID NOT NULL;
CREATE DOMAIN TEXT_TYPE AS VARCHAR(256); 

CREATE TABLE patients (
	id KEY_TYPE PRIMARY KEY,
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

CREATE TABLE encounters (
	id KEY_TYPE PRIMARY KEY NOT NULL,
	start TIMESTAMP NOT NULL,
	stop TIMESTAMP,
	patient KEY_TYPE REFERENCES patients,
	code VARCHAR(64) NOT NULL,
	description TEXT_TYPE NOT NULL,
	cost REAL NOT NULL,
	reasoncode VARCHAR(64),
	reasondescription TEXT_TYPE
) ;

CREATE TABLE observations (
	cid SERIAL PRIMARY KEY,							-- primary key for CogStack Document model
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,	-- document timestamp for CogStack Document model
	date DATE NOT NULL, 
	patient KEY_TYPE REFERENCES patients,
	encounter KEY_TYPE REFERENCES encounters,
	code VARCHAR(64) NOT NULL,
	description TEXT_TYPE NOT NULL,
	value VARCHAR(64) NOT NULL,
	units VARCHAR(64),
	type VARCHAR(64) NOT NULL
) ;


/*

Create view for CogStack

*/
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