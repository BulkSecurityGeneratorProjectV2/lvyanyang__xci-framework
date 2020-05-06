/*
 * Copyright (c) 2007-2020 西安交通信息投资营运有限公司 版权所有
 */

package com.github.lvyanyang.sys.web.controller;

import cn.hutool.core.map.MapUtil;
import com.github.lvyanyang.annotation.AllowAnonymous;
import com.github.lvyanyang.annotation.Authorize;
import com.github.lvyanyang.core.GMap;
import com.github.lvyanyang.core.R;
import com.github.lvyanyang.core.RestResult;
import com.github.lvyanyang.core.XCI;
import com.github.lvyanyang.model.Dic;
import com.github.lvyanyang.sys.component.SysService;
import com.github.lvyanyang.sys.core.SysParams;
import com.github.lvyanyang.sys.entity.SysModule;
import com.github.lvyanyang.sys.filter.HistoryLogFilter;
import com.github.lvyanyang.sys.service.UserService;
import com.github.lvyanyang.sys.web.SysWebController;
import com.github.lvyanyang.sys.web.component.SysWebService;
import com.github.lvyanyang.sys.web.model.JsonGrid;
import com.github.lvyanyang.sys.web.model.LoginModel;
import com.github.lvyanyang.sys.web.model.TreeNode;
import com.github.lvyanyang.web.configuration.WebProperties;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限子系统默认控制器
 */
@Authorize
@Controller("sysWebDefaultController")
@RequestMapping("/sys")
public class DefaultController extends SysWebController {
    @Resource private WebProperties webProperties;
    @Resource private UserService userService;//用户服务
    @Resource private Cache captchaCache;//用户验证码缓存

    //region 页面视图

    @AllowAnonymous
    @GetMapping("/login")
    public String login(ModelMap map) {
        if (!XCI.checkBrowserCompatibility()) {
            return redirect("/compatibility");
        }
        boolean loginResult = SysWebService.me().checkAndAutoLogin();
        if (loginResult) {
            return redirect(webProperties.getDefaultUrl());
        }
        map.put("title", SysParams.SysWebTitle);
        map.put("titleColor", SysParams.SysWebTitleColor);
        map.put("copyright", SysParams.SysWebCopyright);
        return "sys/default/login";
    }

    // @GetMapping("/")
    @GetMapping()
    public String index(ModelMap map) {
        map.put("currentUser", getCurrentUser());
        map.put("title", SysParams.SysWebTitle);
        map.put("titleColor", SysParams.SysWebTitleColor);
        map.put("copyright", SysParams.SysWebCopyright);
        map.put("versionTitle", SysParams.SysWebVersionTitle);
        map.put("versionTitleColor", SysParams.SysWebVersionTitleColor);
        map.put("homeUrl", webProperties.getHomeUrl());
        return "sys/default/index";
    }

    @GetMapping("/home")
    public String home(ModelMap map) {
        map.put("userOwneRoleString", SysService.me().getRoleName(getCurrentUser()));
        return "sys/default/home";
    }

    // @AllowAnonymous
    // @GetMapping("/about")
    // public String about(ModelMap map) {
    //     // List<ReleaseHistoryEntity> list = releaseHistoryService.query(GMap.newMap("hasContent", "1"));
    //     // map.put("releaseHistoryList", list);
    //     return "sys/default/about";
    // }

    /**
     * 图标页面
     */
    @AllowAnonymous
    @GetMapping("/icon")
    public String icon() {
        return "sys/default/icon";
    }

    /**
     * 浏览器兼容性页面
     */
    @AllowAnonymous
    @GetMapping("/compatibility")
    public String compatibility() {
        return redirect(webProperties.getCdn() + "/compatibility/index.html");
    }

    @AllowAnonymous
    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "sys/default/unauthorized";
    }

    @AllowAnonymous
    @GetMapping("/error404")
    public String error404() {
        return "sys/default/error404";
    }

    @AllowAnonymous
    @GetMapping("/error500")
    public String error500() {
        return "sys/default/error500";
    }

    @AllowAnonymous
    @GetMapping("/error500debug")
    public String error500debug() {
        return "sys/default/error500debug";
    }

    //endregion

    //region 数据处理

    @AllowAnonymous
    @ResponseBody
    @PostMapping("/login")
    public RestResult login(@ModelAttribute LoginModel model) {
        String account = model.getAccount();
        if (XCI.isBlank(model.getUuid())) {
            return RestResult.fail("账号密码错误");
        }
        if (SysService.me().lockUserService().isLock(account)) {
            return RestResult.fail("账号错误次数达到上限,暂时被锁定");
        }
        model.setCaptcha(captchaCache.get(account, String.class));
        var result = SysService.me().accountService().login(account, model.getPassword(), model.getCaptcha());
        if (result.isFail()) {
            return result;
        }

        var entity = result.getData();
        RestResult result1 = SysWebService.me().onLoginSuccess(entity);
        if (result1.isFail()) {
            return result1;
        }
        if (model.isAutoLogin()) {
            SysService.me().setUserJwtCookie(entity);
        }
        return RestResult.ok(GMap.newMap("url", webProperties.getDefaultUrl()));
    }

    /**
     * 检查账号锁定状态
     */
    @AllowAnonymous
    @ResponseBody
    @PostMapping("/checkLock")
    public RestResult checkLock(String account) {
        if (SysService.me().lockUserService().requireCaptcha(account)) {
            return RestResult.ok();
        }
        return RestResult.fail();
    }

    @ResponseBody
    @PostMapping("/logout")
    public RestResult logout() {
        SysService.me().accountService().logout(getCurrentUser().getId());
        getSession().removeAttribute(R.CURRENT_USER_Session_KEY);
        XCI.removeCookie(R.CURRENT_USER_COOKIE_KEY);
        return RestResult.ok(MapUtil.of("url", webProperties.getLoginUrl()));
    }

    /**
     * 用户菜单树
     */
    @ResponseBody
    @GetMapping("/userModuleTree")
    public RestResult userModuleTree() {
        var currentUser = getCurrentUser();
        var userPermission = SysService.me().permissionService().selectUserPermissionFromCache(currentUser.getId());
        List<SysModule> list = userPermission.getModules().stream()
                .filter(p -> p.getMenu() && p.getWeb()).collect(Collectors.toList());
        List<TreeNode> nodes = SysWebService.me().toModuleNodeList(list);
        return RestResult.ok(nodes);
    }

    /**
     * 清除用户菜单树缓存
     */
    @ResponseBody
    @PostMapping("/clearUserModuleTree")
    public RestResult clearUserModuleTree() {
        var currentUser = getCurrentUser();
        SysService.me().permissionService().clearUserPermissionCache(currentUser.getId());
        return RestResult.ok();
    }

    /**
     * 查询字典树
     */
    @ResponseBody
    @GetMapping("/dicTree")
    public Object dicTree(String categoryCode) {
        XCI.ifBlankThrow(categoryCode, () -> RestResult.fail("请指定字典编码"));
        List<Dic> list = SysService.me().dicService().selectListByCategoryCode(categoryCode);
        return RestResult.ok(SysWebService.me().toDicNodeList(list));
    }

    /**
     * 激活当前用户
     */
    @ResponseBody
    @PostMapping("/active")
    public RestResult active() {
        SysService.me().onlineUserService().active(getCurrentUser().getId());
        return RestResult.ok();
    }

    /**
     * 根据表名和主键查询历史日志
     */
    @ResponseBody
    @PostMapping("/history")
    public JsonGrid history(String tableName, String primaryKey) {
        var filter = new HistoryLogFilter();
        filter.setTableName(tableName);
        filter.setPrimaryKey(primaryKey);
        return new JsonGrid(SysService.me().historyLogService().selectPageList(filter));
    }

    //endregion
}