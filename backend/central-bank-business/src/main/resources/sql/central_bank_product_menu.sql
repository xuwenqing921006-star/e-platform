-- 中央银行 E 平台：后台金融产品管理菜单与按钮权限
-- 适用于若依 sys_menu，执行前请确认 menu_id 未与现有数据冲突。
SET NAMES utf8mb4;

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2200, '金融产品', 0, 21, 'centralbank/product', 'centralbank/product/index', '',
    'CentralBankProduct', 1, 0, 'C', '0', '0', 'centralbank:product:list', 'money',
    'admin', current_timestamp, '', null, '中央银行 E 平台金融产品管理菜单'
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2201, '产品查询', 2200, 1, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:product:query', '#',
    'admin', current_timestamp, '', null, ''
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2202, '产品新增', 2200, 2, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:product:add', '#',
    'admin', current_timestamp, '', null, ''
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2203, '产品修改', 2200, 3, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:product:edit', '#',
    'admin', current_timestamp, '', null, ''
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2204, '产品删除', 2200, 4, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:product:remove', '#',
    'admin', current_timestamp, '', null, ''
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2205, '模板下载', 2200, 5, '', '', '',
    '', 1, 0, 'F', '0', '0', 'centralbank:product:template', '#',
    'admin', current_timestamp, '', null, ''
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query,
    route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) values (
    2206, 'Excel导入', 0, 22, 'centralbank/product/import', 'centralbank/product/import', '',
    'CentralBankProductImport', 1, 0, 'C', '1', '0', 'centralbank:product:import', '#',
    'admin', current_timestamp, '', null, '中央银行 E 平台金融产品 Excel 导入隐藏路由'
);
