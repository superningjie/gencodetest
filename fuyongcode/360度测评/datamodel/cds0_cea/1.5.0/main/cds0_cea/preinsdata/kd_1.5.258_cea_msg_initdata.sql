DELETE FROM t_msg_tplscene WHERE FID = 1982088695624240128;
INSERT INTO t_msg_tplscene(FID,FMODIFYDATE,FISPREINSDATA,FCREATEDATE,FENTITYNUMBER,FNUMBER,FNAME) VALUES (1982088695624240128,{ts'2024-06-27  00:00:00'},'0',{ts'2024-06-27  00:00:00'},'cea_assessobj','assesstaskstart','360测评_测评任务发送'); 
DELETE FROM t_msg_tplscene WHERE FID = 1982089246780950528;
INSERT INTO t_msg_tplscene(FID,FMODIFYDATE,FISPREINSDATA,FCREATEDATE,FENTITYNUMBER,FNUMBER,FNAME) VALUES (1982089246780950528,{ts'2024-06-27  00:00:00'},'0',{ts'2024-06-27  00:00:00'},'cea_assessobj','assesstaskurg','360测评_测评任务催办'); 

DELETE FROM t_msg_tplscene_l WHERE FPKID = '47G+REBI/ML4';
INSERT INTO t_msg_tplscene_l(FID,FPKID,FLOCALEID,FNAME) VALUES (1982088695624240128,'47G+REBI/ML4','zh_CN','360测评_测评任务发送'); 
DELETE FROM t_msg_tplscene_l WHERE FPKID = '47G+REBI/ML5';
INSERT INTO t_msg_tplscene_l(FID,FPKID,FLOCALEID,FNAME) VALUES (1982088695624240128,'47G+REBI/ML5','zh_TW','360測評_測評任務發送'); 
DELETE FROM t_msg_tplscene_l WHERE FPKID = '47G+VF+93918';
INSERT INTO t_msg_tplscene_l(FID,FPKID,FLOCALEID,FNAME) VALUES (1982089246780950528,'47G+VF+93918','zh_CN','360测评_测评任务催办'); 
DELETE FROM t_msg_tplscene_l WHERE FPKID = '47G+VF+93919';
INSERT INTO t_msg_tplscene_l(FID,FPKID,FLOCALEID,FNAME) VALUES (1982089246780950528,'47G+VF+93919','zh_TW','360測評_測評任務催辦'); 


DELETE FROM t_msg_template WHERE FID = 1982090803773704192;
INSERT INTO t_msg_template(FID,FMODIFYDATE,FMSGSCENENAME,FCREATEDATE,FCOMMONLANG,FMSGENTITY,FNUMBER,FNAME,FMSGTYPE,FMSGCHANNEL,FBIZPLUGIN,FMSGTEMPLATE,FMSGSCENE) VALUES (1982090803773704192,{ts'2024-06-27  00:00:00'},'360测评_测评任务发送',{ts'2024-06-27  00:00:00'},'{"title":"{model.perffile.name}等人的绩效测评任务","content":"您好，{model.perffile.name}等人的绩效测评任务需您评价，请及时处理。点击查看详情跳转至对应评估详情\n快速处理（点击可处理）"}','cea_assessobj','assesstaskstart','测评任务启动','message','mcenter',' ',NULL,'assesstaskstart'); 
DELETE FROM t_msg_template WHERE FID = 1982091809190317056;
INSERT INTO t_msg_template(FID,FMODIFYDATE,FMSGSCENENAME,FCREATEDATE,FCOMMONLANG,FMSGENTITY,FNUMBER,FNAME,FMSGTYPE,FMSGCHANNEL,FBIZPLUGIN,FMSGTEMPLATE,FMSGSCENE) VALUES (1982091809190317056,{ts'2024-06-27  00:00:00'},'360测评_测评任务催办',{ts'2024-06-27  00:00:00'},'{"title":"{model.perffile.name}等人的绩效测评任务","content":"您好，{model.perffile.name}等人的绩效测评任务需您评价，请及时处理。\n快速处理（点击可处理）"}','cea_assessobj','assesstaskurg','测评任务催办','message','mcenter',' ',NULL,'assesstaskurg'); 

DELETE FROM t_msg_template_l WHERE FPKID = '47G/4SG6DX8L';
INSERT INTO t_msg_template_l(FID,FMSGSCENENAME,FPKID,FLOCALEID,FMSGTEMPLATE,FNAME) VALUES (1982090803773704192,'360测评_测评任务发送','47G/4SG6DX8L','zh_CN','{"title":"{model.perffile.name}等人的绩效测评任务","content":"您好，{model.perffile.name}等人的绩效测评任务需您评价，请及时处理。点击查看详情跳转至对应评估详情\n快速处理（点击可处理）"}','测评任务启动'); 
DELETE FROM t_msg_template_l WHERE FPKID = '47G/4SG6DX8M';
INSERT INTO t_msg_template_l(FID,FMSGSCENENAME,FPKID,FLOCALEID,FMSGTEMPLATE,FNAME) VALUES (1982090803773704192,'360測評_測評任務發送','47G/4SG6DX8M','zh_TW',' ','測評任務啟動'); 
DELETE FROM t_msg_template_l WHERE FPKID = '47G/B36QA7BZ';
INSERT INTO t_msg_template_l(FID,FMSGSCENENAME,FPKID,FLOCALEID,FMSGTEMPLATE,FNAME) VALUES (1982091809190317056,'360测评_测评任务催办','47G/B36QA7BZ','zh_CN','{"title":"{model.perffile.name}等人的绩效测评任务","content":"您好，{model.perffile.name}等人的绩效测评任务需您评价，请及时处理。\n快速处理（点击可处理）"}','测评任务催办'); 
DELETE FROM t_msg_template_l WHERE FPKID = '47G/B36QA7C+';
INSERT INTO t_msg_template_l(FID,FMSGSCENENAME,FPKID,FLOCALEID,FMSGTEMPLATE,FNAME) VALUES (1982091809190317056,'360測評_測評任務催辦','47G/B36QA7C+','zh_TW',' ','測評任務催辦'); 




