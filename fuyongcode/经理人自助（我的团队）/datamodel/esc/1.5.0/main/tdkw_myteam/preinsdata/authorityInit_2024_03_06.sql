-- 中间表建表语句

DROP TABLE IF EXISTS tk_tdkw_myteam_branch_tem;

CREATE TABLE tk_tdkw_myteam_branch_tem
(
    fid                     int8        NOT NULL,
    fnumber                 varchar(30) NOT NULL DEFAULT ' ':: character varying,
    fname                   varchar(50) NOT NULL DEFAULT ' ':: character varying,
    fstatus                 varchar(50) NOT NULL DEFAULT ' ':: character varying,
    fcreatorid              int8 NULL,
    fmodifierid             int8 NULL,
    fenable                 varchar(50) NOT NULL DEFAULT ' ':: character varying,
    fcreatetime             timestamp NULL,
    fmodifytime             timestamp NULL,
    fmasterid               int8 NULL,
    fk_tdkw_position        int8 NULL,
    fk_tdkw_reportcoreltype int8 NULL,
    CONSTRAINT pk__tdkw_myteam_branch_tem PRIMARY KEY (fid)
);

DROP TABLE IF EXISTS tk_tdkw_myteam_branch_tem_l;

CREATE TABLE tk_tdkw_myteam_branch_tem_l
(
    fpkid     varchar(36) NOT NULL,
    fid       int8        NOT NULL DEFAULT 0,
    flocaleid varchar(10) NOT NULL DEFAULT '':: character varying,
    fname     varchar(50) NOT NULL DEFAULT ' ':: character varying,
    CONSTRAINT pk__tdkw_myteam_branch_tem_l PRIMARY KEY (fpkid)
);
CREATE
INDEX idx__tdkw_myteam_branch_tem_l_0 ON tk_tdkw_myteam_branch_tem_l USING btree (fid, flocaleid);


DROP TABLE IF EXISTS tk_tdkw_myteam_detail_tem;

CREATE TABLE tk_tdkw_myteam_detail_tem
(
    fid                    int8 NOT NULL,
    fentryid               int8 NOT NULL,
    fseq                   int4 NOT NULL DEFAULT 0,
    fk_tdkw_branchposition int8 NULL,
    CONSTRAINT pk__tdkw_myteam_detail_tem PRIMARY KEY (fentryid)
);
CREATE
INDEX idx__tdkw_myteam_detail_tem_fk ON tk_tdkw_myteam_detail_tem USING btree (fid);


DROP TABLE IF EXISTS tk_tdkw_businessunit_temp;

CREATE TABLE tk_tdkw_businessunit_temp
(
    fid                       int8        NOT NULL,
    fnumber                   varchar(30) NOT NULL DEFAULT ' ':: character varying,
    fname                     varchar(50) NOT NULL DEFAULT ' ':: character varying,
    fstatus                   varchar(50) NOT NULL DEFAULT ' ':: character varying,
    fcreatorid                int8 NULL,
    fmodifierid               int8 NULL,
    fenable                   varchar(50) NOT NULL DEFAULT ' ':: character varying,
    fcreatetime               timestamp NULL,
    fmodifytime               timestamp NULL,
    fmasterid                 int8 NULL,
    fk_tdkw_manager           int8 NULL,
    fk_tdkw_position          int8 NULL,
    fk_tdkw_reportcoreltype   int8 NULL,
    fk_tdkw_checkboxfield     bpchar(1) NOT NULL DEFAULT '0'::bpchar,
    fk_tdkw_subordinates      bpchar(1) NOT NULL DEFAULT '1'::bpchar,
    fk_tdkw_my_post           varchar(50) NOT NULL DEFAULT ' ':: character varying,
    fk_tdkw_affiliateadminorg int8 NULL,
    fk_tdkw_ranks             int8 NULL,
    fk_tdkw_trank_name        varchar(50) NOT NULL DEFAULT ' ':: character varying,
    fk_tdkw_target_exec_name  varchar(50) NOT NULL DEFAULT ' ':: character varying,
    fk_tdkw_operate           varchar(50) NOT NULL DEFAULT ' ':: character varying,
    CONSTRAINT pk__tdkw_businessunit_temp PRIMARY KEY (fid)
);


DROP TABLE IF EXISTS tk_tdkw_businessunit_temp_l;

CREATE TABLE tk_tdkw_businessunit_temp_l
(
    fpkid     varchar(36) NOT NULL,
    fid       int8        NOT NULL DEFAULT 0,
    flocaleid varchar(10) NOT NULL DEFAULT '':: character varying,
    fname     varchar(50) NOT NULL DEFAULT ' ':: character varying,
    CONSTRAINT pk__tdkw_businessunit_temp_l PRIMARY KEY (fpkid)
);
CREATE
INDEX idx__tdkw_businessunit_temp_l_0 ON tk_tdkw_businessunit_temp_l USING btree (fid, flocaleid);


DROP TABLE IF EXISTS tk_tdkw_subordinate_temp;

CREATE TABLE tk_tdkw_subordinate_temp
(
    fid                    int8         NOT NULL,
    fentryid               int8         NOT NULL,
    fseq                   int4         NOT NULL DEFAULT 0,
    fk_tdkw_subordinate    int8 NULL,
    fk_tdkw_headsculpture  varchar(255) NOT NULL DEFAULT ' ':: character varying,
    fk_tdkw_phone          varchar(50)  NOT NULL DEFAULT ' ':: character varying,
    fk_tdkw_peremail       varchar(50)  NOT NULL DEFAULT ' ':: character varying,
    fk_tdkw_adminorg       int8 NULL,
    fk_tdkw_position1      int8 NULL,
    fk_tdkw_company        int8 NULL,
    fk_tdkw_checkboxfield1 bpchar(1) NOT NULL DEFAULT '0'::bpchar,
    CONSTRAINT pk__tdkw_subordinate_temp PRIMARY KEY (fentryid)
);
CREATE
INDEX idx__tdkw_subordinate_temp_fk ON tk_tdkw_subordinate_temp USING btree (fid);

