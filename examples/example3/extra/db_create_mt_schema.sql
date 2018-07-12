/* 
	creates schema of the DB for CogStack
*/

create table samples (
CID serial primary key,
SAMPLE_ID integer not null,
TYPE varchar(256) not null,
TYPE_ID integer not null,
NAME varchar(256) not null,
DESCRIPTION text not null,
DOCUMENT text not null,
DCT timestamp not null
) ;

create view samples_view as 
	select 
		samples.*,
		'src_field_name'::text as cog_src_field_name,		-- for CogStack compatibility
		'samples_view'::text as cog_src_table_name,			-- (*)
		samples.CID as cog_pk,								-- (*)
		'cog_pk'::text as cog_pk_field_name,				-- (*)
		DCT as cog_update_time								-- (*)
	from 
		samples 
	;