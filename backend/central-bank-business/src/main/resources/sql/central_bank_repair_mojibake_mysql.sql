SET NAMES utf8mb4;

-- Repair local data that was imported before init scripts declared utf8mb4 as
-- the client charset. This is intentionally idempotent for mojibake-like rows.

update sys_menu
set menu_name = convert(binary convert(menu_name using latin1) using utf8mb4)
where menu_name regexp '[ÃÂèåçéæä]';

update sys_menu
set remark = convert(binary convert(remark using latin1) using utf8mb4)
where remark is not null and remark regexp '[ÃÂèåçéæä]';

update sys_dept
set dept_name = convert(binary convert(dept_name using latin1) using utf8mb4)
where dept_name regexp '[ÃÂèåçéæä]';

update sys_user
set nick_name = convert(binary convert(nick_name using latin1) using utf8mb4)
where nick_name regexp '[ÃÂèåçéæä]';

update sys_user
set remark = convert(binary convert(remark using latin1) using utf8mb4)
where remark is not null and remark regexp '[ÃÂèåçéæä]';

update sys_role
set role_name = convert(binary convert(role_name using latin1) using utf8mb4)
where role_name regexp '[ÃÂèåçéæä]';

update sys_role
set remark = convert(binary convert(remark using latin1) using utf8mb4)
where remark is not null and remark regexp '[ÃÂèåçéæä]';

update sys_dict_type
set dict_name = convert(binary convert(dict_name using latin1) using utf8mb4)
where dict_name regexp '[ÃÂèåçéæä]';

update sys_dict_type
set remark = convert(binary convert(remark using latin1) using utf8mb4)
where remark is not null and remark regexp '[ÃÂèåçéæä]';

update sys_dict_data
set dict_label = convert(binary convert(dict_label using latin1) using utf8mb4)
where dict_label regexp '[ÃÂèåçéæä]';

update sys_dict_data
set remark = convert(binary convert(remark using latin1) using utf8mb4)
where remark is not null and remark regexp '[ÃÂèåçéæä]';

update sys_config
set config_name = convert(binary convert(config_name using latin1) using utf8mb4)
where config_name regexp '[ÃÂèåçéæä]';

update sys_config
set remark = convert(binary convert(remark using latin1) using utf8mb4)
where remark is not null and remark regexp '[ÃÂèåçéæä]';

update sys_notice
set notice_title = convert(binary convert(notice_title using latin1) using utf8mb4)
where notice_title regexp '[ÃÂèåçéæä]';

update cb_content
set title = convert(binary convert(title using latin1) using utf8mb4)
where title regexp '[ÃÂèåçéæä]';

update cb_content
set office_name = convert(binary convert(office_name using latin1) using utf8mb4)
where office_name regexp '[ÃÂèåçéæä]';

update cb_content
set rich_text_html = convert(binary convert(rich_text_html using latin1) using utf8mb4)
where rich_text_html regexp '[ÃÂèåçéæä]';

update cb_attachment
set file_name = convert(binary convert(file_name using latin1) using utf8mb4)
where file_name regexp '[ÃÂèåçéæä]';

update cb_financial_product
set bank_name = convert(binary convert(bank_name using latin1) using utf8mb4)
where bank_name regexp '[ÃÂèåçéæä]';

update cb_financial_product
set product_name = convert(binary convert(product_name using latin1) using utf8mb4)
where product_name regexp '[ÃÂèåçéæä]';

update cb_financial_product
set admission_conditions = convert(binary convert(admission_conditions using latin1) using utf8mb4)
where admission_conditions regexp '[ÃÂèåçéæä]';

update cb_financial_product
set product_intro = convert(binary convert(product_intro using latin1) using utf8mb4)
where product_intro regexp '[ÃÂèåçéæä]';

update cb_financial_product
set business_manager = convert(binary convert(business_manager using latin1) using utf8mb4)
where business_manager regexp '[ÃÂèåçéæä]';

update cb_financial_product
set contact_info = convert(binary convert(contact_info using latin1) using utf8mb4)
where contact_info regexp '[ÃÂèåçéæä]';

update cb_account_extension
set office_name = convert(binary convert(office_name using latin1) using utf8mb4)
where office_name is not null and office_name regexp '[ÃÂèåçéæä]';
