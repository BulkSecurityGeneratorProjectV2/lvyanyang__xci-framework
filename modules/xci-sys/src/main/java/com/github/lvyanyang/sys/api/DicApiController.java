/*
 * Copyright (c) 2007-2020 西安交通信息投资营运有限公司 版权所有
 */

package com.github.lvyanyang.sys.api;

import com.github.lvyanyang.annotation.Authorize;
import com.github.lvyanyang.annotation.SingleJson;
import com.github.lvyanyang.core.R;
import com.github.lvyanyang.core.RestResult;
import com.github.lvyanyang.core.XCI;
import com.github.lvyanyang.model.Dic;
import com.github.lvyanyang.model.IdValue;
import com.github.lvyanyang.model.StatusBody;
import com.github.lvyanyang.sys.component.SysService;
import com.github.lvyanyang.sys.core.SysApiController;
import com.github.lvyanyang.sys.entity.SysDic;
import com.github.lvyanyang.sys.filter.DicFilter;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统字典控制器
 * @author 吕艳阳
 */
@Api(tags = "系统字典接口")
@ApiSort(24)
@Authorize
@RestController
@RequestMapping(value = R.SysApiPrefix + "/dic", produces = R.PROJSON)
public class DicApiController extends SysApiController {
    @ApiOperation(value = "检查字典编码是否存在")
    @ApiOperationSupport(order = 1, author = R.LYY)
    @ApiImplicitParam(name = "categoryCode", value = "字典类型编码")
    @PostMapping(value = "/existByCategoryCode")
    public RestResult existByCategoryCode(@SingleJson String categoryCode) {
        return RestResult.ok(SysService.me().dicService().existByCategoryCode(categoryCode));
    }

    @ApiOperation(value = "新建字典")
    @ApiOperationSupport(order = 1, author = R.LYY, ignoreParameters = "entity.parentName")
    @Authorize(code = R.Permission.SysDicInsert)
    @PostMapping("/insert")
    public RestResult insert(@RequestBody SysDic entity) {
        return SysService.me().dicService().insert(entity);
    }

    @ApiOperation(value = "修改字典")
    @ApiOperationSupport(order = 2, author = R.LYY, ignoreParameters = "entity.parentName")
    @Authorize(code = R.Permission.SysDicUpdate)
    @PostMapping("/update")
    public RestResult update(@RequestBody SysDic entity) {
        return SysService.me().dicService().update(entity);
    }


    @ApiOperation(value = "批量保存字典", notes = "如果主键存在则更新否则新建")
    @ApiOperationSupport(order = 3, author = R.LYY)
    @Authorize(code = R.Permission.SysDicUpdate)
    @PostMapping(value = "/batchSave")
    public RestResult batchSave(@RequestBody List<SysDic> entities) {
        SysService.me().dicService().batchSave(entities);
        return RestResult.ok();
    }

    @ApiOperation(value = "修改字典状态")
    @ApiOperationSupport(order = 4, author = R.LYY)
    @Authorize(code = R.Permission.SysDicUpdate)
    @PostMapping(value = "/updateStatus")
    public RestResult updateStatus(@RequestBody StatusBody statusBody) {
        return SysService.me().dicService().updateStatus(statusBody.getIds(), statusBody.getStatus());
    }

    @ApiOperation(value = "根据主键修改字典类型父节点")
    @ApiOperationSupport(order = 5)
    @Authorize(code = R.Permission.SysDicUpdate)
    @PostMapping("/updateParentId")
    public RestResult updateParentId(@RequestBody List<IdValue> values) {
        return SysService.me().dicService().updateParentId(values);
    }

    @ApiOperation(value = "根据主键修改字典类型排序路径")
    @ApiOperationSupport(order = 6, author = R.LYY)
    @Authorize(code = R.Permission.SysDicUpdate)
    @PostMapping("/updatePath")
    public RestResult updatePath(@RequestBody List<IdValue> values) {
        return SysService.me().dicService().updatePath(values);
    }

    @ApiOperation(value = "删除字典")
    @ApiOperationSupport(order = 4, author = R.LYY)
    @ApiImplicitParam(name = "ids", value = "主键字符串")
    @Authorize(code = R.Permission.SysDicDelete)
    @PostMapping("/delete")
    public RestResult delete(@SingleJson String ids) {
        SysService.me().dicService().delete(ids);
        return RestResult.ok();
    }

    @ApiOperation(value = "根据字典类型编码删除")
    @ApiOperationSupport(order = 5, author = R.LYY)
    @ApiImplicitParam(name = "categoryCode", value = "字典类型编码")
    @Authorize(code = R.Permission.SysDicDelete)
    @PostMapping("/deleteByCategoryCode")
    public RestResult deleteByCategoryCode(@SingleJson String categoryCode) {
        return SysService.me().dicService().deleteByCategoryCode(categoryCode);
    }

    @ApiOperation(value = "查询单个字典")
    @ApiOperationSupport(order = 7, author = R.LYY)
    @ApiImplicitParam(name = "id", value = "字典主键")
    @PostMapping("/selectById")
    public RestResult<SysDic> selectById(@SingleJson Long id) {
        return RestResult.ok(SysService.me().dicService().selectById(id));
    }

    @ApiOperation(value = "根据类型编码查询字典列表")
    @ApiOperationSupport(order = 9, author = R.LYY)
    @ApiImplicitParam(name = "categoryCode", value = "字典类型编码")
    @PostMapping("/selectListByCategoryCode")
    public RestResult<List<Dic>> selectListByCategoryCode(@SingleJson String categoryCode) {
        return RestResult.ok(SysService.me().dicService().selectListByCategoryCode(categoryCode));
    }

    @ApiOperation(value = "查询字典列表")
    @ApiOperationSupport(order = 10, author = R.LYY, ignoreParameters = {R.IPI, R.IPS, R.IPSN, R.IPSD})
    @PostMapping("/selectList")
    public RestResult<List<SysDic>> selectList(DicFilter filter) {
        return RestResult.ok(SysService.me().dicService().selectList(filter));
    }

    @ApiOperation(value = "导出字典列表")
    @ApiOperationSupport(order = 11, author = R.LYY, ignoreParameters = {R.IPI, R.IPS, R.IPSN, R.IPSD})
    @PostMapping(value = "/export", produces = {R.PROOCTET, R.PROJSON})
    public void export(DicFilter filter) {
        XCI.exportExcel(SysService.me().dicService().selectList(filter), SysDic.class, "系统字典列表");
    }

    @ApiOperation(value = "刷新字典缓存")
    @ApiOperationSupport(order = 8, author = R.LYY)
    @PostMapping(value = "/refresh")
    public RestResult refresh() {
        SysService.me().dicService().refresh();
        return RestResult.ok();
    }
}