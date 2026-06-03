-- 中央银行 E 平台：后台操作日志菜单与查询权限
-- 适用于若依 sys_menu，执行前请确认 menu_id 未与现有数据冲突。

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2400, '操作日志', 0, 24, 'centralbank/audit-log', 'centralbank/audit-log/index', '',
    'CentralBankAuditLog', 1, 0, 'C', '0', '0', 'centralbank:audit-log:list', 'log',
    'admin', current_timestamp, '', null, '中央银行 E 平台后台操作日志菜单'
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2401, '日志查询', 2400, 1, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:audit-log:query', '#',
    'admin', current_timestamp, '', null, ''
);
