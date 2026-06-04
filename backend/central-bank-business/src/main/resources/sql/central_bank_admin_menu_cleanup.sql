SET NAMES utf8mb4;

-- Keep the RuoYi shell, but hide default system/monitor/tool menus from the
-- admin route tree. The business UI must expose only project-approved entries.
update sys_menu
set status = '1', visible = '1'
where menu_id < 2000;

update sys_menu
set status = '0', visible = '0'
where menu_id in (2100, 2200, 2300, 2400);

update sys_menu
set status = '0', visible = '1'
where menu_id = 2206;

update sys_menu
set status = '0', visible = '0'
where menu_id in (
    2101, 2102, 2103, 2104,
    2201, 2202, 2203, 2204, 2205,
    2301, 2302, 2303, 2304, 2305,
    2401
);

delete from sys_role_menu
where role_id = 2;

insert into sys_role_menu(role_id, menu_id) values
    (2, 2100), (2, 2101), (2, 2102), (2, 2103), (2, 2104),
    (2, 2200), (2, 2201), (2, 2202), (2, 2203), (2, 2204), (2, 2205), (2, 2206),
    (2, 2300), (2, 2301), (2, 2302), (2, 2303), (2, 2304), (2, 2305),
    (2, 2400), (2, 2401);

insert into sys_user_role(user_id, role_id)
select e.user_id, 2
from cb_account_extension e
where e.user_id <> 1
  and not exists (
      select 1
      from sys_user_role ur
      where ur.user_id = e.user_id and ur.role_id = 2
  );
