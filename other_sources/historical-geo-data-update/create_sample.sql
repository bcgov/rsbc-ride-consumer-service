

CREATE TABLE master.dbo.geolocations (
	business_program varchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	business_type varchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	business_id varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	long varchar(15) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	lat varchar(15) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	databc_long varchar(15) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	databc_lat varchar(15) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	CONSTRAINT PK_GEOLOCATIONS PRIMARY KEY (business_program,business_type,business_id)
);