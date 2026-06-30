package com.ruoyi.web.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.framework.security.handle.AuthenticationEntryPointImpl;
import com.ruoyi.framework.security.handle.ContractAccessDeniedHandler;
import com.ruoyi.framework.web.exception.GlobalExceptionHandler;

class SecurityContractTest
{
    @Test
    void ajaxResultUsesUnifiedContractKeys()
    {
        AjaxResult result = AjaxResult.error(HttpStatus.UNAUTHORIZED, "登录状态已失效");

        assertThat(result).containsEntry("code", HttpStatus.UNAUTHORIZED);
        assertThat(result).containsEntry("message", "登录状态已失效");
        assertThat(result).containsKey("data");
        assertThat(result.get("data")).isNull();
        assertThat(result).doesNotContainKey("msg");
    }

    @Test
    void unauthenticatedRequestsReturn401WithUnifiedBody() throws IOException, ServletException
    {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new AuthenticationEntryPointImpl().commence(
                new MockHttpServletRequest("GET", "/api/admin/articles"),
                response,
                new BadCredentialsException("missing token"));

        JSONObject body = JSON.parseObject(response.getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_UNAUTHORIZED);
        assertThat(body.getIntValue("code")).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(body.getString("message")).isEqualTo("登录状态已失效");
        assertThat(body.containsKey("data")).isTrue();
        assertThat(body.get("data")).isNull();
        assertThat(body.containsKey("msg")).isFalse();
    }

    @Test
    void forbiddenRequestsReturn403WithUnifiedBody() throws IOException, ServletException
    {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ContractAccessDeniedHandler().handle(
                new MockHttpServletRequest("GET", "/api/admin/articles"),
                response,
                new AccessDeniedException("forbidden"));

        JSONObject body = JSON.parseObject(response.getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_FORBIDDEN);
        assertThat(body.getIntValue("code")).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(body.getString("message")).isEqualTo("无权限访问该资源");
        assertThat(body.containsKey("data")).isTrue();
        assertThat(body.get("data")).isNull();
        assertThat(body.containsKey("msg")).isFalse();
    }

    @Test
    void methodLevelAccessDeniedUsesSame403Contract()
    {
        AjaxResult result = new GlobalExceptionHandler().handleAccessDeniedException(
                new AccessDeniedException("forbidden"),
                new MockHttpServletRequest("GET", "/api/admin/articles"));

        assertThat(result).containsEntry("code", HttpStatus.FORBIDDEN);
        assertThat(result).containsEntry("message", "无权限访问该资源");
        assertThat(result).containsKey("data");
        assertThat(result.get("data")).isNull();
    }

    @Test
    void securityConfigDeclaresPublicAndProtectedApiRoutes() throws IOException
    {
        String securityConfig = readBackendFile("ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java");
        String resourcesConfig = readBackendFile("ruoyi-framework/src/main/java/com/ruoyi/framework/config/ResourcesConfig.java");

        assertThat(securityConfig).contains("\"/api/public/**\"");
        assertThat(securityConfig).contains("\"/api/admin/**\"");
        assertThat(securityConfig).contains("accessDeniedHandler(accessDeniedHandler)");
        assertThat(resourcesConfig).contains("http://localhost:5180");
        assertThat(resourcesConfig).contains("http://127.0.0.1:5180");
        assertThat(resourcesConfig).contains("http://localhost:5199");
        assertThat(resourcesConfig).contains("http://127.0.0.1:5199");
        assertThat(resourcesConfig).contains("http://localhost:5175");
        assertThat(resourcesConfig).contains("http://127.0.0.1:5175");
        assertThat(resourcesConfig).contains("http://localhost:5176");
        assertThat(resourcesConfig).contains("http://127.0.0.1:5176");
        assertThat(resourcesConfig).contains("${APP_CORS_ALLOWED_ORIGINS:}");
        assertThat(resourcesConfig).doesNotContain("addAllowedOriginPattern(\"*\")");
    }

    @Test
    void runtimeSecretsAndStorageRootsComeFromLocalConfiguration() throws IOException
    {
        String application = readBackendFile("ruoyi-admin/src/main/resources/application.yml");
        String druid = readBackendFile("ruoyi-admin/src/main/resources/application-druid.yml");

        assertThat(application).contains("${APP_STORAGE_ROOT:./data/uploadPath}");
        assertThat(application).contains("${APP_JWT_SECRET:central-bank-dev-placeholder}");
        assertThat(druid).contains("${DB_URL:jdbc:mysql://localhost:3306/central_bank_e_platform");
        assertThat(druid).contains("${DB_USERNAME:root}");
        assertThat(druid).contains("${DB_PASSWORD:}");
        assertThat(druid).contains("${DRUID_LOGIN_PASSWORD:}");
        assertThat(application + druid).doesNotContain("abcdefghijklmnopqrstuvwxyz");
        assertThat(application + druid).doesNotContain("D:/ruoyi/uploadPath");
        assertThat(application + druid).doesNotContain("password: password");
        assertThat(application + druid).doesNotContain("login-password: 123456");
    }

    @Test
    void backendLoginUsesOnlyUsernameAndPasswordWithoutCaptcha() throws IOException
    {
        String loginService = readBackendFile("ruoyi-framework/src/main/java/com/ruoyi/framework/web/service/SysLoginService.java");
        String loginPage = readProjectFile("ruoyi-ui/src/views/login.vue");
        String seedSql = readBackendFile("sql/ry_20260417.sql");

        assertThat(loginService).doesNotContain("validateCaptcha(username");
        assertThat(loginService).doesNotContain("selectCaptchaEnabled");
        assertThat(loginPage).doesNotContain("getCodeImg");
        assertThat(loginPage).doesNotContain("captchaEnabled");
        assertThat(loginPage).doesNotContain("验证码");
        assertThat(seedSql).contains("'sys.account.captchaEnabled',       'false'");
    }

    @Test
    void jacksonRuntimeAndCommonModuleDeclareCoreArtifacts() throws Exception
    {
        String commonPom = readBackendFile("ruoyi-common/pom.xml");

        Class.forName("com.fasterxml.jackson.core.util.InternalJacksonUtil");
        assertThat(commonPom).contains("<artifactId>jackson-databind</artifactId>");
        assertThat(commonPom).contains("<artifactId>jackson-core</artifactId>");
        assertThat(commonPom).contains("<artifactId>jackson-annotations</artifactId>");
    }

    @Test
    void centralBankAccountsBridgeBusinessRolesToRuoYiRoutes() throws IOException
    {
        String accountService = readBackendFile(
                "central-bank-business/src/main/java/com/centralbank/eplatform/service/AdminAccountService.java");
        String accountMapper = readBackendFile(
                "central-bank-business/src/main/resources/mapper/centralbank/CbAdminAccountMapper.xml");
        String loginController = readBackendFile("ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysLoginController.java");

        assertThat(accountService).contains("CENTRAL_BANK_COMMON_ROLE_ID");
        assertThat(accountService).contains("insertUserRole(account.getId(), CENTRAL_BANK_COMMON_ROLE_ID)");
        assertThat(accountMapper).contains("insert into sys_user_role(user_id, role_id)");
        assertThat(loginController).contains("filterCentralBankMenus");
        assertThat(loginController).contains("centralBankPermissions");
        assertThat(loginController).contains("MONETARY_CREDIT");
    }

    @Test
    void currentUserNicknameRefreshesAfterAccountManagementEdit() throws IOException
    {
        String loginController = readBackendFile("ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysLoginController.java");
        String accountForm = readProjectFile("ruoyi-ui/src/views/centralbank/account/form.vue");

        assertThat(loginController).contains("private ISysUserService userService");
        assertThat(loginController).contains("userService.selectUserById(loginUser.getUserId())");
        assertThat(loginController).contains("loginUser.setUser(user)");
        assertThat(loginController).contains("tokenService.refreshToken(loginUser)");
        assertThat(accountForm).contains("this.$store.getters.id");
        assertThat(accountForm).contains("this.$store.dispatch('GetInfo')");
        assertThat(accountForm).contains("refreshCurrentUserInfo");
    }

    @Test
    void loginAndRequestErrorsPreferContractMessage() throws IOException
    {
        String globalExceptionHandler = readBackendFile(
                "ruoyi-framework/src/main/java/com/ruoyi/framework/web/exception/GlobalExceptionHandler.java");
        String request = readProjectFile("ruoyi-ui/src/utils/request.js");
        String download = readProjectFile("ruoyi-ui/src/plugins/download.js");

        assertThat(globalExceptionHandler).contains("@ExceptionHandler(UserException.class)");
        assertThat(globalExceptionHandler).contains("HttpStatus.UNAUTHORIZED");
        assertThat(request).contains("res.data.message || res.data.msg");
        assertThat(request).contains("error.response && error.response.data");
        assertThat(download).contains("rspObj.message || rspObj.msg");
    }

    @Test
    void officeUserContentFormAndDashboardActionsFollowPermissions() throws IOException
    {
        String loginController = readBackendFile("ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysLoginController.java");
        String userStore = readProjectFile("ruoyi-ui/src/store/modules/user.js");
        String getters = readProjectFile("ruoyi-ui/src/store/getters.js");
        String contentPage = readProjectFile("ruoyi-ui/src/views/centralbank/content/index.vue");
        String dashboardPage = readProjectFile("ruoyi-ui/src/views/index.vue");

        assertThat(loginController).contains("account_extension");
        assertThat(userStore).contains("SET_ACCOUNT_EXTENSION");
        assertThat(getters).contains("accountExtension");
        assertThat(contentPage).contains(":disabled=\"isOfficeLocked\"");
        assertThat(contentPage).contains("availableFormOffices");
        assertThat(contentPage).contains("applyLockedOfficeDefaults");
        assertThat(contentPage).contains(".rich-text ::v-deep img");
        assertThat(contentPage).contains("max-width: 100%");
        assertThat(contentPage).contains("height: auto");
        assertThat(contentPage).contains("overflow-x: auto");
        assertThat(dashboardPage).contains("quickActions");
        assertThat(dashboardPage).contains(":disabled=\"!action.enabled\"");
        assertThat(dashboardPage).contains("auth.hasPermi");
    }

    private static String readBackendFile(String relativePath) throws IOException
    {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path direct = cwd.resolve(relativePath).normalize();
        Path siblingFromModule = cwd.resolve("..").resolve(relativePath).normalize();
        if (Files.exists(direct))
        {
            return Files.readString(direct, StandardCharsets.UTF_8);
        }
        if (Files.exists(siblingFromModule))
        {
            return Files.readString(siblingFromModule, StandardCharsets.UTF_8);
        }
        throw new IOException("Cannot locate backend file: " + relativePath);
    }

    private static String readProjectFile(String relativePath) throws IOException
    {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path direct = cwd.resolve(relativePath).normalize();
        Path projectFromBackend = cwd.resolve("..").resolve(relativePath).normalize();
        Path projectFromModule = cwd.resolve("..").resolve("..").resolve(relativePath).normalize();
        if (Files.exists(direct))
        {
            return Files.readString(direct, StandardCharsets.UTF_8);
        }
        if (Files.exists(projectFromBackend))
        {
            return Files.readString(projectFromBackend, StandardCharsets.UTF_8);
        }
        if (Files.exists(projectFromModule))
        {
            return Files.readString(projectFromModule, StandardCharsets.UTF_8);
        }
        throw new IOException("Cannot locate project file: " + relativePath);
    }
}
