/* 
	creates schema of the DB for CogStack
*/

create table samples (
CID serial primary key, 								-- for CogStack compatibility
SAMPLE_ID integer not null,
TYPE varchar(256) not null,
TYPE_ID integer not null,
NAME varchar(256) not null,
DESCRIPTION text not null,
DOCUMENT text not null,
DCT timestamp default current_timestamp					-- (*)
) ;
