insert into cb_content (
    id, title, category, scope, county_code, office_code, office_name, rich_text_html,
    published_at, created_at, updated_at
) values (
    101, '[Seed] 肇州县金融服务便民联系指南', 'SERVICE_GUIDE', 'RURAL', 'ZHAOZHOU',
    'ZHAOZHOU', '肇州县', '<h2>办理说明</h2><p>县域服务指引用于验证基础数据层。</p>',
    '2026-05-29 09:30:00', current_timestamp, current_timestamp
) on duplicate key update title = values(title);

insert into cb_financial_product (
    id, bank_code, bank_name, product_name, product_type, admission_conditions,
    product_intro, business_manager, contact_info, created_at, updated_at
) values (
    2001, 'ABC', '农业银行', '[Seed] 惠农e贷', 'AGRICULTURAL',
    '面向涉农经营主体。', '用于验证 7 字段产品数据层。', '张经理', '0459-0002001',
    current_timestamp, current_timestamp
) on duplicate key update product_name = values(product_name);

insert into cb_account_extension (
    id, user_id, role, office_code, office_name, enabled, created_at, updated_at
) values (
    1, 1, 'ADMIN', null, null, 1, current_timestamp, current_timestamp
) on duplicate key update role = values(role);
