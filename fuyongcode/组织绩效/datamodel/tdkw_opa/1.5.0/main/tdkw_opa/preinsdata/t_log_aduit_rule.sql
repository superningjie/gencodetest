DELETE FROM t_log_aduit_rule WHERE FID = 2025329294502275072;
INSERT INTO t_log_aduit_rule(FID,FCREATETIME,FENABLE,FMODIFIERID,FMODIFYTIME,FBIZOBJ,FMODIFYFIELDS,FDESCRIPTION,FMASTERID,FSTATUS,FCREATORID,FNUMBER) VALUES (2025329294502275072,{ts'2024-08-26  00:00:00'},'1',2005735093842332672,{ts'2024-08-29  00:00:00'},'tdkw_org_perf_metrics',',状态(billstatus),组织评估结果(tdkw_eval_res_list),区域类型.区域类型(tdkw_hideentry.tdkw_area_type),指标.目标值(tdkw_subentryentity.tdkw_target_value),指标.权重(tdkw_subentryentity.tdkw_weight),指标.单位(tdkw_subentryentity.tdkw_unit),指标.Q4季度完成情况(tdkw_subentryentity.tdkw_q4_completion),指标.Q1季度完成情况(tdkw_subentryentity.tdkw_q1_completion),指标.Q2季度完成情况(tdkw_subentryentity.tdkw_q2_completion),指标.Q3季度完成情况(tdkw_subentryentity.tdkw_q3_completion),指标.年度完成值(tdkw_subentryentity.tdkw_annual_val),指标.评估结果(tdkw_subentryentity.tdkw_eval_res),指标.上级组织(tdkw_subentryentity.tdkw_superorg),指标.年度完成情况(tdkw_subentryentity.tdkw_ctr_annual_val),指标.指标名称日志字段(tdkw_subentryentity.tdkw_metric_log),指标.指标名称(tdkw_subentryentity.tdkw_metric_str),',' ',2025329294502275072,'A',2005735093842332672,'org_perf_metrics_rule');
DELETE FROM t_log_aduit_rule_L WHERE FPKID = '4BS51Y18BK9M';
INSERT INTO t_log_aduit_rule_L(FID,FPKID,FLOCALEID,FDESCRIPTION,FNAME) VALUES (2025329294502275072,'4BS51Y18BK9M','zh_CN',' ','组织绩效指标规则');
DELETE FROM t_log_aduit_rule_L WHERE FPKID = '4BS51Y18BK9N';
INSERT INTO t_log_aduit_rule_L(FID,FPKID,FLOCALEID,FDESCRIPTION,FNAME) VALUES (2025329294502275072,'4BS51Y18BK9N','zh_TW',' ','組織績效指標規則');


