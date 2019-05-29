-- Author: Gergely Zahoranszky-Kohalmi, PhD
--
-- Aim: Initialize data structure and load data into for bioactivity data derived from ChEMBL forSmrtGraph.
--
-- Use: Load it from the directory the input files are stored in.

--create database chembl_derivative;
--grant all privileges on database chembl_derivative to user;



--
-- Ref: https://www.postgresql.org/docs/8.2/sql-droptable.html
--

drop table if exists c2p;
drop table if exists compound;
drop table if exists pattern;
drop table if exists canonical_activity;
drop table if exists target_activity_cutoff;


create table c2p (molregno integer,
				pattern_id varchar(100),
				ptype varchar(100),
				hash varchar(100),
				component_id integer primary key,
				islargest boolean,
				pattern_overlap_ratio numeric);


create index c2p_idx1 on c2p(pattern_id);
create index c2p_idx2 on c2p(molregno);

\copy c2p from PUB_KNIME_PrepChEMBL_Workflow_Generated_C2P_Table.tab.csv with (format csv, header true, delimiter E'\t')

--COPY 840842



create table canonical_activity (inchi_key varchar(100),
				uniprot_id varchar(100),
				can_activity_pm numeric);

create index canonical_activity_idx1 on canonical_activity(inchi_key);
create index canonical_activity_idx2 on canonical_activity(uniprot_id);


\copy canonical_activity from PUB_KNIME_PrepChEMBL_Workflow_Generated_canonical_activities.tab.csv with (format csv, header true, delimiter E'\t')

--COPY 541016


create table pattern (pattern_id varchar(100) primary key,
					structure varchar(2000),
					ptype varchar(100),
					hash varchar(100));


\copy pattern from PUB_KNIME_PrepChEMBL_Workflow_Generated_Scaffold_Table.tab.csv with (format csv, header true, delimiter E'\t')

--COPY 64280


create table compound (inchi_key varchar(100) primary key,
			smiles varchar(5000),
			compound_id integer,
			molregno integer);
						
						
create index compound_idx on compound(molregno);
create unique index compound_uidx on compound(compound_id);


\copy compound from PUB_unique_chembl_compounds.smiles with (format csv, header true, delimiter E'\t')

--COPY 1819577


create table target_activity_cutoff (uniprot_id varchar(100) primary key,
					percentile80_cutoff numeric,
					nr_tested_compounds integer,
					actual_cutoff numeric);



\copy target_activity_cutoff from PUB_KNIME_PrepChEMBL_Workflow_Generated_target_activity_cutoffs.tab.csv with (format csv, header true, delimiter E'\t')

--COPY 4047


