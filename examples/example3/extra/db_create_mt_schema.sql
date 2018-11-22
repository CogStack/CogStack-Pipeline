/* 
	Creates schema of the DB for CogStack
*/

CREATE TABLE mtsamples (
	cid SERIAL PRIMARY KEY, 								-- for CogStack Document data model
	sample_id INTEGER NOT NULL,
	type VARCHAR(256) NOT NULL,
	type_id INTEGER NOT NULL,
	name VARCHAR(256) NOT NULL,
	description TEXT NOT NULL,
	document TEXT NOT NULL,
	dct TIMESTAMP DEFAULT CURRENT_TIMESTAMP					-- (*)
) ;
