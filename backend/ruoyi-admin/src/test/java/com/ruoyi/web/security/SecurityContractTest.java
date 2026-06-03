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
        assertThat(resourcesConfig).contains("http://localhost:5199");
        assertThat(resourcesConfig).contains("http://127.0.0.1:5199");
        assertThat(resourcesConfig).contains("http://localhost:5175");
        assertThat(resourcesConfig).contains("http://127.0.0.1:5175");
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
}
