merge into cb_content key(id) values (
    101, '[Seed] 肇州县金融服务便民联系指南', 'SERVICE_GUIDE', 'RURAL', 'ZHAOZHOU',
    'ZHAOZHOU', '肇州县', '<h2>办理说明</h2><p>县域服务指引用于验证基础数据层。</p>',
    timestamp '2026-05-29 09:30:00', current_timestamp, current_timestamp
);

update cb_content
set title = '肇州县金融服务便民联系指南',
    rich_text_html = '<h2>办理说明</h2><p>县域服务指引展示县域服务网点、咨询渠道和办理提示。</p>'
where id = 101;

merge into cb_content key(id) values (
    102, '大庆市征信代理查询网点地址及电话', 'SERVICE_GUIDE', 'FINANCIAL', null,
    'CREDIT', '征信管理科', '<h2>办理说明</h2><p>申请人可携带有效身份证件前往就近服务网点办理。</p>',
    timestamp '2026-05-30 09:30:00', current_timestamp, current_timestamp
);

merge into cb_content key(id) values (
    103, '大庆市金融支持小微企业政策提示', 'POLICY_PROMOTION', 'FINANCIAL', null,
    'MONETARY_CREDIT', '货币信贷政策管理科', '<h2>政策要点</h2><p>金融机构持续优化小微企业融资服务，提升政策直达效率。</p>',
    timestamp '2026-05-31 09:30:00', current_timestamp, current_timestamp
);

merge into cb_content key(id) values (
    104, '肇州县涉农金融服务办理提示', 'SERVICE_GUIDE', 'RURAL', 'ZHAOZHOU',
    'ZHAOZHOU', '肇州县', '<h2>服务提示</h2><p>涉农主体可联系县域服务队获取金融产品和政策咨询。</p>',
    timestamp '2026-06-01 09:30:00', current_timestamp, current_timestamp
);

merge into cb_attachment key(id) values (
    9001, 101, '县域征信服务网点信息表.xlsx', 'EXCEL', 20480,
    'seed/attachments/9001.xlsx', current_timestamp
);

merge into cb_attachment key(id) values (
    9002, 101, '个人信用报告查询指引.pdf', 'PDF', 51200,
    'seed/attachments/9002.pdf', current_timestamp
);

merge into cb_attachment key(id) values (
    9003, 101, '征信业务申请材料清单.docx', 'WORD', 36864,
    'seed/attachments/9003.docx', current_timestamp
);

merge into cb_attachment key(id) values (
    9004, 101, '补充办理说明.pdf', 'PDF', 10240,
    'seed/attachments/9004.pdf', current_timestamp
);

merge into cb_financial_product key(id) values (
    2001, 'ABC', '农业银行', '[Seed] 惠农e贷', 'AGRICULTURAL',
    '面向涉农经营主体。', '用于验证 7 字段产品数据层。',
    '张经理', '0459-0002001', current_timestamp, current_timestamp
);

merge into cb_account_extension key(id) values (
    1, 1, 'ADMIN', null, null, true, current_timestamp, current_timestamp
);
