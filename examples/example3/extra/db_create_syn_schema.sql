/* 
	creates schema of the DB for CogStack

Uses schema specified by:
    https://github.com/synthetichealth/synthea/wiki/CSV-File-Data-Dictionary

*/

-- postgres-specific syntax; for mysql use 'create type ...'
--create domain key_type as char(36) not null;
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
	zip VARCHAR(64) -- NOT NULL 						-- not matching with the specs
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
	cid SERIAL PRIMARY KEY,								-- for CogStack compatibility
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,		-- (*)
	date DATE NOT NULL, 
	patient KEY_TYPE REFERENCES patients,
	encounter KEY_TYPE REFERENCES encounters,
	code VARCHAR(64) NOT NULL,
	description TEXT_TYPE NOT NULL,
	value VARCHAR(64) NOT NULL,
	units VARCHAR(64),
	type VARCHAR(64) NOT NULL
) ;

CREATE TABLE procedures (
	cid SERIAL PRIMARY KEY,								-- for CogStack compatibility
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,		-- (*)
	date DATE NOT NULL,
	patient KEY_TYPE REFERENCES patients,
	encounter KEY_TYPE REFERENCES encounters,
	code VARCHAR(64) NOT NULL,
	description	TEXT_TYPE NOT NULL,
	cost REAL NOT NULL,
	reasoncode VARCHAR(64),
	reasondescription TEXT_TYPE
) ;

CREATE TABLE medications (
	cid SERIAL PRIMARY KEY,								-- for CogStack compatibility
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,		-- (*)
	start DATE NOT NULL,
	stop DATE,
	patient KEY_TYPE REFERENCES patients,
	encounter KEY_TYPE REFERENCES encounters,
	code VARCHAR(64) NOT NULL,
	description TEXT_TYPE NOT NULL,
	cost NUMERIC NOT NULL,
	dispenses NUMERIC NOT NULL,
	totalcost NUMERIC NOT NULL,
	reasoncode VARCHAR(64),
	reasondescription TEXT_TYPE
) ;

CREATE TABLE immunizations (
	cid SERIAL PRIMARY KEY,								-- for CogStack compatibility
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,		-- (*)
	date DATE NOT NULL,
	patient KEY_TYPE REFERENCES patients,
	encounter KEY_TYPE REFERENCES encounters,
	code VARCHAR(64) NOT NULL,
	description TEXT_TYPE NOT NULL,
	cost NUMERIC NOT NULL
) ;

CREATE TABLE imaging_studies (
	cid SERIAL,-- PRIMARY KEY,							-- TODO: CHECK IF ID FIELD WILL BE SUFFICIENT
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,		-- (*)
	id KEY_TYPE PRIMARY KEY,
	date DATE NOT NULL,
	patient KEY_TYPE REFERENCES patients,
	encounter KEY_TYPE REFERENCES encounters,
	bodysite_code VARCHAR(64) NOT NULL,
	bodysite_description VARCHAR(64) NOT NULL,
	modality_code VARCHAR(64) NOT NULL,
	modality_description VARCHAR(64) NOT NULL,
	sop_code VARCHAR(64) NOT NULL,
	sop_description VARCHAR(64) NOT NULL
) ;

CREATE TABLE careplans (
	cid SERIAL,-- PRIMARY KEY,							-- TODO: CHECK IF ID FIELD WILL BE SUFFICIENT
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,		-- (*)
	id KEY_TYPE PRIMARY KEY,
	start DATE NOT NULL,
	stop DATE,
	patient KEY_TYPE REFERENCES patients,
	encounter KEY_TYPE REFERENCES encounters,
	code VARCHAR(64) NOT NULL,
	description TEXT_TYPE NOT NULL,
	reasoncode VARCHAR(64), -- NOT NULL, 				-- not matching with the specs
	reasondescription TEXT_TYPE -- NOT NULL, 			-- not matching with the specs
) ;

CREATE TABLE allergies (
	cid SERIAL PRIMARY KEY,								-- for CogStack compatibility
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,		-- (*)
	start DATE NOT NULL,
	stop DATE,
	patient KEY_TYPE REFERENCES patients,
	encounter KEY_TYPE REFERENCES encounters,
	code VARCHAR(64) NOT NULL,
	description TEXT_TYPE NOT NULL
) ;

CREATE TABLE conditions (
	cid SERIAL PRIMARY KEY,								-- for CogStack compatibility
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,		-- (*)
	start DATE NOT NULL,
	stop DATE,
	patient KEY_TYPE REFERENCES patients,
	encounter KEY_TYPE REFERENCES encounters,
	code VARCHAR(64) NOT NULL,
	description TEXT_TYPE NOT NULL
) ;


/*

Create views for CogStack

*/


/* Main patient-encounters view
*/
CREATE VIEW patient_encounters_view AS
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
		enc.reasondescription AS encounter_reason_desc
	FROM 
		patients p, 
		encounters enc
	WHERE 
		enc.patient = p.id
	;



/* Auxiliary views created as joins with patients and encounters
*/


/* Function to create views joined with patients_encounters view
*/
CREATE OR REPLACE FUNCTION create_paenc_view(table_name varchar(32)) RETURNS VOID 
	AS $func$
DECLARE
	view_name varchar(64);
BEGIN
	view_name = table_name || '_view';
	EXECUTE FORMAT(E'
		CREATE OR REPLACE VIEW %I AS 
		SELECT 
			patient_encounters_view.*, 
			%I.*,
			%I.cid AS cog_pk,
			%I.created as cog_timestamp
		FROM 
			patient_encounters_view 
		JOIN 
			%I
		on 
			%I.patient = patient_encounters_view.patient_id AND 
			%I.encounter = patient_encounters_view.encounter_id 
		', 
		view_name, table_name, table_name, table_name, table_name, table_name, table_name, table_name);
END
$func$ LANGUAGE PLPGSQL;


/* Create the rest of views
*/
SELECT create_paenc_view('observations');
SELECT create_paenc_view('procedures');
SELECT create_paenc_view('medications');
SELECT create_paenc_view('immunizations');
SELECT create_paenc_view('imaging_studies');
SELECT create_paenc_view('careplans');
SELECT create_paenc_view('allergies');
SELECT create_paenc_view('conditions');
