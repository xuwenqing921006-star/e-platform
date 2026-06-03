-- 中央银行 E 平台：后台账号管理菜单与按钮权限
-- 适用于若依 sys_menu，执行前请确认 menu_id 未与现有数据冲突。

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2300, '账号管理', 0, 23, 'centralbank/account', 'centralbank/account/index', '',
    'CentralBankAccount', 1, 0, 'C', '0', '0', 'centralbank:account:list', 'peoples',
    'admin', current_timestamp, '', null, '中央银行 E 平台后台账号管理菜单'
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2301, '账号查询', 2300, 1, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:account:query', '#',
    'admin', current_timestamp, '', null, ''
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2302, '账号新增', 2300, 2, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:account:add', '#',
    'admin', current_timestamp, '', null, ''
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2303, '账号修改', 2300, 3, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:account:edit', '#',
    'admin', current_timestamp, '', null, ''
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2304, '密码重置', 2300, 4, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:account:reset', '#',
    'admin', current_timestamp, '', null, ''
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2305, '账号删除', 2300, 5, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:account:remove', '#',
    'admin', current_timestamp, '', null, ''
);
