/* 
	creates schema of the DB for CogStack
*/

create table samples (
CID serial primary key, 								-- for CogStack compatibility
DCT timestamp default current_timestamp,				-- (*)
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
		'src_field_name'::text as cog_src_field_name,		-- for CogStack compatibility
		'samples_view'::text as cog_src_table_name,			-- (*)
		samples.CID as cog_pk,								-- (*)
		'cog_pk'::text as cog_pk_field_name,				-- (*)
		samples.DCT as cog_update_time						-- (*)
	from 
		samples 
	;