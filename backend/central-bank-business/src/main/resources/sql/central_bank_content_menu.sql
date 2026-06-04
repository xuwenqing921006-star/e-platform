-- 中央银行 E 平台：后台内容管理菜单与按钮权限
-- 适用于若依 sys_menu，执行前请确认 menu_id 未与现有数据冲突。
SET NAMES utf8mb4;

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2100, '内容管理', 0, 20, 'centralbank/content', 'centralbank/content/index', '',
    'CentralBankContent', 1, 0, 'C', '0', '0', 'centralbank:content:list', 'documentation',
    'admin', current_timestamp, '', null, '中央银行 E 平台内容管理菜单'
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2101, '内容查询', 2100, 1, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:content:query', '#',
    'admin', current_timestamp, '', null, ''
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2102, '内容新增', 2100, 2, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:content:add', '#',
    'admin', current_timestamp, '', null, ''
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2103, '内容修改', 2100, 3, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:content:edit', '#',
    'admin', current_timestamp, '', null, ''
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2104, '内容删除', 2100, 4, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:content:remove', '#',
    'admin', current_timestamp, '', null, ''
);
