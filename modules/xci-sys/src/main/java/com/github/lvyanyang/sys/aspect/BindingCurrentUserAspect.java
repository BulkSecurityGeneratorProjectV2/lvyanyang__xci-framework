/*
 * Copyright (c) 2007-2020 西安交通信息投资营运有限公司 版权所有
 */

package com.github.lvyanyang.sys.aspect;

import com.github.lvyanyang.sys.component.SysService;
import com.github.lvyanyang.sys.annotation.BindingCurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * 绑定当前用户字段信息拦截器
 *
 * @author 吕艳阳
 */
@Aspect
@Slf4j
public class BindingCurrentUserAspect {
    @Before("@annotation(com.github.lvyanyang.sys.annotation.BindingCurrentUser)")
    public void doProcess(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) return;
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        BindingCurrentUser cu = method.getAnnotation(BindingCurrentUser.class);
        for (Object bean : args) {
            if (bean == null) continue;
            if (bean.getClass().isPrimitive()) continue;
            SysService.me().setUserInfo(bean, cu.created());
        }
    }
}