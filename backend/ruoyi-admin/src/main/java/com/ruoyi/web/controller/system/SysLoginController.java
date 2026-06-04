package com.ruoyi.web.controller.system;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.centralbank.eplatform.domain.CbAccountExtension;
import com.centralbank.eplatform.mapper.CbAccountExtensionMapper;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.framework.web.service.SysPermissionService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysMenuService;

/**
 * 登录验证
 * 
 * @author ruoyi
 */
@RestController
public class SysLoginController
{
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_OFFICE_USER = "OFFICE_USER";
    private static final String MONETARY_CREDIT = "MONETARY_CREDIT";
    private static final Set<Long> CENTRAL_BANK_ADMIN_MENUS = Set.of(2100L, 2200L, 2206L, 2300L, 2400L);
    private static final Set<Long> CENTRAL_BANK_OFFICE_MENUS = Set.of(2100L);
    private static final Set<Long> CENTRAL_BANK_PRODUCT_MENUS = Set.of(2100L, 2200L, 2206L);
    private static final Set<String> CONTENT_PERMISSIONS = Set.of(
            "centralbank:content:list", "centralbank:content:query", "centralbank:content:add",
            "centralbank:content:edit", "centralbank:content:remove");
    private static final Set<String> PRODUCT_PERMISSIONS = Set.of(
            "centralbank:product:list", "centralbank:product:query", "centralbank:product:add",
            "centralbank:product:edit", "centralbank:product:remove", "centralbank:product:template",
            "centralbank:product:import");
    private static final Set<String> ACCOUNT_PERMISSIONS = Set.of(
            "centralbank:account:list", "centralbank:account:query", "centralbank:account:add",
            "centralbank:account:edit", "centralbank:account:reset", "centralbank:account:remove");
    private static final Set<String> AUDIT_PERMISSIONS = Set.of(
            "centralbank:audit-log:list", "centralbank:audit-log:query");

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private CbAccountExtensionMapper accountExtensionMapper;

    /**
     * 登录方法
     * 
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 获取用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = centralBankPermissions(user);
        if (roles.isEmpty())
        {
            roles.addAll(centralBankRoles(user));
        }
        if (!loginUser.getPermissions().equals(permissions))
        {
            loginUser.setPermissions(permissions);
            tokenService.refreshToken(loginUser);
        }
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("account_extension", centralBankAccountExtension(user));
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        ajax.put("pwdChrtype", getSysAccountChrtype());
        ajax.put("isDefaultModifyPwd", initPasswordIsModify(user.getPwdUpdateDate()));
        ajax.put("isPasswordExpired", passwordIsExpiration(user.getPwdUpdateDate()));
        return ajax;
    }

    /**
     * 获取路由信息
     * 
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        CentralBankAccess access = centralBankAccess(SecurityUtils.getLoginUser().getUser());
        if (!access.menuIds().isEmpty())
        {
            if (menus.isEmpty())
            {
                menus = menuService.selectMenuTreeByUserId(1L);
            }
            menus = filterCentralBankMenus(menus, access.menuIds());
        }
        return AjaxResult.success(menuService.buildMenus(menus));
    }

    private Set<String> centralBankRoles(SysUser user)
    {
        CentralBankAccess access = centralBankAccess(user);
        if (access.adminAccess())
        {
            return Set.of("central_bank_admin");
        }
        if (!access.menuIds().isEmpty())
        {
            return Set.of("central_bank_office_user");
        }
        return Set.of();
    }

    private Set<String> centralBankPermissions(SysUser user)
    {
        CentralBankAccess access = centralBankAccess(user);
        Set<String> permissions = new HashSet<>();
        permissions.addAll(CONTENT_PERMISSIONS);
        if (access.productAccess())
        {
            permissions.addAll(PRODUCT_PERMISSIONS);
        }
        if (access.adminAccess())
        {
            permissions.addAll(PRODUCT_PERMISSIONS);
            permissions.addAll(ACCOUNT_PERMISSIONS);
            permissions.addAll(AUDIT_PERMISSIONS);
        }
        if (access.menuIds().isEmpty())
        {
            return Set.of();
        }
        return permissions;
    }

    private Map<String, Object> centralBankAccountExtension(SysUser user)
    {
        Map<String, Object> accountExtension = new LinkedHashMap<>();
        if (user == null)
        {
            return accountExtension;
        }
        if (user.isAdmin())
        {
            accountExtension.put("role", ROLE_ADMIN);
            accountExtension.put("office_code", null);
            accountExtension.put("office_name", null);
            accountExtension.put("enabled", true);
            return accountExtension;
        }
        CbAccountExtension extension = accountExtensionMapper.selectByUserId(user.getUserId());
        if (extension == null)
        {
            return accountExtension;
        }
        accountExtension.put("role", extension.getRole());
        accountExtension.put("office_code", extension.getOfficeCode());
        accountExtension.put("office_name", extension.getOfficeName());
        accountExtension.put("enabled", extension.getEnabled());
        return accountExtension;
    }

    private CentralBankAccess centralBankAccess(SysUser user)
    {
        if (user == null)
        {
            return CentralBankAccess.none();
        }
        if (user.isAdmin())
        {
            return CentralBankAccess.admin();
        }
        CbAccountExtension extension = accountExtensionMapper.selectByUserId(user.getUserId());
        if (extension == null || !Boolean.TRUE.equals(extension.getEnabled()))
        {
            return CentralBankAccess.none();
        }
        if (ROLE_ADMIN.equals(extension.getRole()))
        {
            return CentralBankAccess.admin();
        }
        if (ROLE_OFFICE_USER.equals(extension.getRole()) && MONETARY_CREDIT.equals(extension.getOfficeCode()))
        {
            return CentralBankAccess.productOffice();
        }
        if (ROLE_OFFICE_USER.equals(extension.getRole()))
        {
            return CentralBankAccess.office();
        }
        return CentralBankAccess.none();
    }

    private List<SysMenu> filterCentralBankMenus(List<SysMenu> menus, Set<Long> allowedMenuIds)
    {
        List<SysMenu> filtered = new ArrayList<>();
        if (menus == null)
        {
            return filtered;
        }
        for (SysMenu menu : menus)
        {
            if (allowedMenuIds.contains(menu.getMenuId()))
            {
                menu.setChildren(filterCentralBankMenus(menu.getChildren(), allowedMenuIds));
                filtered.add(menu);
            }
        }
        return filtered;
    }

    private record CentralBankAccess(Set<Long> menuIds, boolean adminAccess, boolean productAccess)
    {
        static CentralBankAccess admin()
        {
            return new CentralBankAccess(CENTRAL_BANK_ADMIN_MENUS, true, true);
        }

        static CentralBankAccess productOffice()
        {
            return new CentralBankAccess(CENTRAL_BANK_PRODUCT_MENUS, false, true);
        }

        static CentralBankAccess office()
        {
            return new CentralBankAccess(CENTRAL_BANK_OFFICE_MENUS, false, false);
        }

        static CentralBankAccess none()
        {
            return new CentralBankAccess(Set.of(), false, false);
        }
    }

    // 获取用户密码自定义配置规则
    public String getSysAccountChrtype()
    {
        return Convert.toStr(configService.selectConfigByKey("sys.account.chrtype"), "0");
    }

    // 检查初始密码是否提醒修改
    public boolean initPasswordIsModify(Date pwdUpdateDate)
    {
        Integer initPasswordModify = Convert.toInt(configService.selectConfigByKey("sys.account.initPasswordModify"));
        return initPasswordModify != null && initPasswordModify == 1 && pwdUpdateDate == null;
    }

    // 检查密码是否过期
    public boolean passwordIsExpiration(Date pwdUpdateDate)
    {
        Integer passwordValidateDays = Convert.toInt(configService.selectConfigByKey("sys.account.passwordValidateDays"));
        if (passwordValidateDays != null && passwordValidateDays > 0)
        {
            if (StringUtils.isNull(pwdUpdateDate))
            {
                // 如果从未修改过初始密码，直接提醒过期
                return true;
            }
            Date nowDate = DateUtils.getNowDate();
            return DateUtils.differentDaysByMillisecond(nowDate, pwdUpdateDate) > passwordValidateDays;
        }
        return false;
    }
}
