-- 银行机构名称清理：统一改为大庆分行，并删除阳光惠农贷垃圾数据。
-- 线上已有数据库需要手动执行；重新上传 jar 不会自动修改历史数据。

delete from cb_financial_product
where bank_code = 'SUNSHINE_AGRICULTURE'
   or bank_name = '阳光惠农贷'
   or product_name = '阳光惠农贷';

update cb_financial_product
set bank_name = case bank_code
    when 'ICBC' then '中国工商银行大庆分行'
    when 'ABC' then '农业银行大庆分行'
    when 'BOC' then '中国银行大庆分行'
    when 'CCB' then '建设银行大庆分行'
    when 'CGB' then '广发银行大庆分行'
    when 'CIB' then '兴业银行大庆分行'
    when 'CMB' then '招商银行大庆分行'
    when 'CITIC' then '中信银行大庆分行'
    when 'HARBIN_BANK' then '哈尔滨银行大庆分行'
    when 'KUNLUN_BANK' then '昆仑银行大庆分行'
    when 'LONGJIANG_BANK' then '龙江银行大庆分行'
    when 'CEB' then '光大银行大庆分行'
    else bank_name
end
where bank_code in (
    'ICBC',
    'ABC',
    'BOC',
    'CCB',
    'CGB',
    'CIB',
    'CMB',
    'CITIC',
    'HARBIN_BANK',
    'KUNLUN_BANK',
    'LONGJIANG_BANK',
    'CEB'
);
