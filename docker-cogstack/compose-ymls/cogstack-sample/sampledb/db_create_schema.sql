/* 
	creates schema of the DB for CogStack

Uses schema specified by:
    https://github.com/synthetichealth/synthea/wiki/CSV-File-Data-Dictionary

*/

create domain key_type as uuid not null;
create domain text_type as varchar(256); 

create table patients (
ID key_type primary key,
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
CITY varchar(64), -- not null,						-- not matching with the specs
STATE varchar(64), -- not null,						--||--
ZIP varchar(64) -- not null							--||--
) ;

create table encounters (
ID key_type primary key not null,
START timestamp not null,
STOP timestamp,
PATIENT key_type references patients,
CODE varchar(64) not null,
DESCRIPTION text_type not null,
COST real not null,
REASONCODE varchar(64),
REASONDESCRIPTION text_type
) ;

create table observations (
CID serial primary key,								-- for CogStack compatibility
DATE date not null, 
PATIENT key_type references patients,
ENCOUNTER key_type references encounters,
CODE varchar(64) not null,
DESCRIPTION text_type not null,
VALUE varchar(64) not null,
UNITS varchar(64),
TYPE varchar(64) not null--,
) ;

create table procedures (
CID serial primary key,								-- auto-generated, for CogStack compatibility
DATE date not null,
PATIENT key_type references patients,
ENCOUNTER key_type references encounters,
CODE varchar(64) not null,
DESCRIPTION	text_type not null,
COST real not null,
REASONCODE varchar(64),
REASONDESCRIPTION text_type
) ;

create table medications (
CID serial primary key,								-- auto-generated, for CogStack compatibility
START date not null,
STOP date,
PATIENT key_type references patients,
ENCOUNTER key_type references encounters,
CODE varchar(64) not null,
DESCRIPTION text_type not null,
COST numeric not null,
DISPENSES numeric not null,
TOTALCOST numeric not null,
REASONCODE varchar(64),
REASONDESCRIPTION text_type
) ;

create table immunizations (
CID serial primary key,								-- auto-generated, for CogStack compatibility
DATE date not null,
PATIENT key_type references patients,
ENCOUNTER key_type references encounters,
CODE varchar(64) not null,
DESCRIPTION text_type not null,
COST numeric not null
) ;

create table imaging_studies (
CID serial,-- primary key,							-- auto-generated, TODO: check if ID field will be sufficient
ID key_type primary key,
DATE date not null,
PATIENT key_type references patients,
ENCOUNTER key_type references encounters,
BODYSITE_CODE varchar(64) not null,
BODYSITE_DESCRIPTION varchar(64) not null,
MODALITY_CODE varchar(64) not null,
MODALITY_DESCRIPTION varchar(64) not null,
SOP_CODE varchar(64) not null,
SOP_DESCRIPTION varchar(64) not null
) ;

create table careplans (
CID serial,-- primary key,							-- auto-generated, TODO: check if ID field will be sufficient
ID key_type primary key,
START date not null,
STOP date,
PATIENT key_type references patients,
ENCOUNTER key_type references encounters,
CODE varchar(64) not null,
DESCRIPTION text_type not null,
REASONCODE varchar(64), --not null, 				-- not matching with the specs
REASONDESCRIPTION text_type --not null, 			-- not matching with the specs
) ;

create table allergies (
CID serial primary key,								-- auto-generated, for CogStack compatibility
START date not null,
STOP date,
PATIENT key_type references patients,
ENCOUNTER key_type references encounters,
CODE varchar(64) not null,
DESCRIPTION text_type not null
) ;

create table conditions (
CID serial primary key,								-- auto-generated, for CogStack compatibility
START date not null,
STOP date,
PATIENT key_type references patients,
ENCOUNTER key_type references encounters,
CODE varchar(64) not null,
DESCRIPTION text_type not null
) ;


/*

Create views for CogStack

*/


/* Main patient-encounters view
*/
create view patient_encounters_view as
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
		enc.REASONDESCRIPTION as encounter_reason_desc
	from 
		patients p, 
		encounters enc
	where 
		enc.PATIENT = p.ID
	;


/* Auxiliary views created as joins with patients and encounters
*/


/*
create view observations_view as 
	select 
		patient_encounters_view.*, 
		observations.*,
		'src_field_name'::text as cog_src_field_name,		-- for CogStack compatibility
		'observations_view'::text as cog_src_table_name,	-- (*)
		observations.CID as cog_pk,							-- (*)
		'cog_pk'::text as cog_pk_field_name,				-- (*)
		coalesce(patient_encounters_view.encounter_stop, 
					 patient_encounters_view.encounter_start) 
					 as cog_update_time						-- (*)
	from 
		patient_encounters_view 
	join 
		observations 
	on 
		observations.PATIENT = patient_encounters_view.patient_id and 
		observations.ENCOUNTER = patient_encounters_view.encounter_id 
	;
*/


/* Function to create views joined with patients_encounters view
*/
create or replace function create_paenc_view(table_name varchar(32)) returns void 
	as $func$
declare
	view_name varchar(64);
begin
	view_name = table_name || '_view';
	execute format(E'
		create or replace view %I as 
		select 
			patient_encounters_view.*, 
			%I.*,

			\'src_field_name\'::text as cog_src_field_name,
			''%I''::text as cog_src_table_name,
			%I.CID as cog_pk,
			\'cog_pk\'::text as cog_pk_field_name,
			coalesce(patient_encounters_view.encounter_stop, 
					 patient_encounters_view.encounter_start) 
					 as cog_update_time
		from 
			patient_encounters_view 
		join 
			%I
		on 
			%I.PATIENT = patient_encounters_view.patient_id and 
			%I.ENCOUNTER = patient_encounters_view.encounter_id 
		', 
		view_name, table_name, view_name, table_name, table_name, table_name, table_name, table_name);
end
$func$ language plpgsql;


/* Create the rest of views
*/
select create_paenc_view('observations');
select create_paenc_view('procedures');
select create_paenc_view('medications');
select create_paenc_view('immunizations');
select create_paenc_view('imaging_studies');
select create_paenc_view('careplans');
select create_paenc_view('allergies');
select create_paenc_view('conditions');
