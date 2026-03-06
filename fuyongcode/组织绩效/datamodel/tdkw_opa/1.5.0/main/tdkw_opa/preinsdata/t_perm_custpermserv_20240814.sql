delete from T_PERM_CUSTPERMSERV where fid = 2035456005017449472;

insert
into
    T_PERM_CUSTPERMSERV(FID,
                        FSERVFACTORY,
                        FSERVNAME,
                        FISSKIP,
                        FISAND,
                        FAPPID,
                        FSERVAPPNUM)
values(2035456005017449472,
       'kd.hrmp.hrcs.servicehelper.ServiceFactory',
       'IHRCSDataPermissionService',
       '0',
       '1',
       '4A6IGSR0AFR3',
       'hrcs');


