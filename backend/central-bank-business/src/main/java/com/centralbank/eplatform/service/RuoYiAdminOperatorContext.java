package com.centralbank.eplatform.service;

import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.SecurityUtils;

@Component
public class RuoYiAdminOperatorContext implements AdminOperatorContext
{
    @Override
    public Long currentUserId()
    {
        return SecurityUtils.getUserId();
    }
}
