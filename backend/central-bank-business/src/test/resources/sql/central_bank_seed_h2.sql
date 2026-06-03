merge into cb_content key(id) values (
    101, '[Seed] 肇州县金融服务便民联系指南', 'SERVICE_GUIDE', 'RURAL', 'ZHAOZHOU',
    'ZHAOZHOU', '肇州县', '<h2>办理说明</h2><p>县域服务指引用于验证基础数据层。</p>',
    timestamp '2026-05-29 09:30:00', current_timestamp, current_timestamp
);

merge into cb_attachment key(id) values (
    9001, 101, '县域征信服务网点信息表.xlsx', 'EXCEL', 20480,
    'seed/attachments/9001.xlsx', current_timestamp
);

merge into cb_financial_product key(id) values (
    2001, 'ABC', '农业银行', '[Seed] 惠农e贷', 'AGRICULTURAL',
    '面向涉农经营主体。', '用于验证 7 字段产品数据层。',
    '张经理', '0459-0002001', current_timestamp, current_timestamp
);

merge into cb_account_extension key(id) values (
    1, 1, 'ADMIN', null, null, true, current_timestamp, current_timestamp
);
