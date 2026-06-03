package com.ruoyi.framework.security.handle;

import java.io.IOException;
import java.io.Serializable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.ServletUtils;

/**
 * 统一 403 响应格式。
 */
@Component
public class ContractAccessDeniedHandler implements AccessDeniedHandler, Serializable
{
    private static final long serialVersionUID = 1L;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException
    {
        ServletUtils.renderString(response, HttpServletResponse.SC_FORBIDDEN,
                JSON.toJSONString(AjaxResult.error(HttpStatus.FORBIDDEN, "无权限访问该资源"),
                        JSONWriter.Feature.WriteMapNullValue));
    }
}
