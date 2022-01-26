CREATE TABLE A
id varchar (addValue),
abc dateTime,
fromAtoC varchar (addValue) NOT NULL
;

CREATE TABLE B AS
SELECT id, abc, fromAtoC
FROM A;

ALTER TABLE B 
ADD bcd double(addValue, addValue) NOT NULL, fromBtoD varchar (addValue), fromBtoDButSomehowDifferent varchar (addValue) NOT NULL
;

CREATE TABLE D 
id varchar (addValue),
def int (addValue)
;

CREATE TABLE E AS
SELECT id, abc
FROM A;

ALTER TABLE E 
ADD fromEtoC varchar (addValue)
;

CREATE TABLE DtoB 
rdfs_domain varchar (addValue) NOT NULL,
rdfs_range varchar (addValue) NOT NULL,
FOREIGN KEY (rdfs_domain) REFERENCES B(id),
FOREIGN KEY (rdfs_range) REFERENCES D(id)
;

