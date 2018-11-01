--案例执行前参数准备
-- 商户维护，内部商户：2001(正常),2002(正常),2003(正常),2004(正常),2005(失效)
insert when (not exists (select merchant_id from merchant where merchant_id = '2001')) then into merchant
values ('2001', '商户-正常', 1, to_date('06-06-2017', 'dd-mm-yyyy'), to_date('06-06-2017', 'dd-mm-yyyy')) select '2001' from dual;

insert when (not exists (select merchant_id from merchant where merchant_id = '2001')) then into merchant
values ('2005', '商户-失效', 2, to_date('06-06-2017', 'dd-mm-yyyy'), to_date('06-06-2017', 'dd-mm-yyyy')) select '2001' from dual;