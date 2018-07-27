/* 
	creates schema of the DB for CogStack

Uses schema specified by:
    https://github.com/synthetichealth/synthea/wiki/CSV-File-Data-Dictionary

*/

create table patients (
ID uuid primary key,
BIRTHDATE date not null, 
DEATHDATE date, 
SSN varchar(64) not null, 
DRIVERS varchar(64),
PASSPORT varchar(64),
PREFIX varchar(8),
FIRST varchar(64) not null,
LAST varchar(64) not null,
SUFFIX varchar(8),
MAIDEN varchar(64),
MARITAL char(1),
RACE varchar(64) not null, 
ETHNICITY varchar(64) not null,
GENDER char(1) not null,
BIRTHPLACE varchar(64) not null,
ADDRESS varchar(64) not null,
CITY varchar(64) not null,
STATE varchar(64) not null,
ZIP varchar(64)
) ;

create table encounters (
CID serial,											-- for CogStack compatibility
ID uuid primary key not null,
START timestamp not null,
STOP timestamp,
PATIENT uuid references patients,
CODE varchar(64) not null,
DESCRIPTION varchar(256) not null,
COST real not null,
REASONCODE varchar(64),
REASONDESCRIPTION varchar(256),
BINARYDOCUMENT bytea								-- MTSamples document content
) ;

create table observations (
CID serial primary key,								-- for CogStack compatibility
DCT timestamp default current_timestamp,			-- (*)
DATE date not null, 
PATIENT uuid references patients,
ENCOUNTER uuid references encounters,
CODE varchar(64) not null,
DESCRIPTION varchar(256) not null,
VALUE varchar(64) not null,
UNITS varchar(64),
TYPE varchar(64) not null
) ;


/*

Create view for CogStack

*/
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
		obs.DCT as cog_update_time,                       -- (e)
		enc.BINARYDOCUMENT as cog_binary_doc              -- (f)
	from 
		patients p, 
		encounters enc,
		observations obs
	where 
		enc.PATIENT = p.ID and
		obs.PATIENT = p.ID and 
    	obs.ENCOUNTER = enc.ID
	;