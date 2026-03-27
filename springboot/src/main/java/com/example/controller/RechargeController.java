package com.example.controller;

import com.example.common.Result;
import com.example.entity.Recharge;
import com.example.service.RechargeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 充值控制器 - 需要JWT认证
 **/
@RestController
@RequestMapping("/recharge")
public class RechargeController extends BaseController {

    @Resource
    private RechargeService rechargeService;

    /**
     * 新增 - 自动设置当前用户ID
     */
    @PostMapping("/add")
    public Result add(@RequestBody Recharge recharge, HttpServletRequest request) {
        // 设置当前用户ID
        recharge.setUserId(getCurrentUserId(request));
        rechargeService.add(recharge);
        return Result.success();
    }

    /**
     * 删除 - 只能删除自己的充值记录
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id, HttpServletRequest request) {
        Recharge recharge = rechargeService.selectById(id);
        if (recharge == null) {
            return Result.error("充值记录不存在");
        }
        // 检查是否为当前用户的充值记录
        if (!recharge.getUserId().equals(getCurrentUserId(request))) {
            return Result.error("无权限删除此充值记录");
        }
        rechargeService.deleteById(id);
        return Result.success();
    }

    /**
     * 查询当前用户的所有充值记录
     */
    @GetMapping("/selectAll")
    public Result selectAll(HttpServletRequest request) {
        Recharge recharge = new Recharge();
        
        // 如果是管理员，查询所有充值记录；否则只查询当前用户的充值记录
        if (isAdmin(request)) {
            // 管理员可以查看所有充值记录
        } else {
            // 普通用户只能查看自己的充值记录
            recharge.setUserId(getCurrentUserId(request));
        }
        
        List<Recharge> list = rechargeService.selectAll(recharge);
        return Result.success(list);
    }

    /**
     * 分页查询充值记录 - 管理员可以查看所有充值记录，普通用户只能查看自己的充值记录
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             HttpServletRequest request) {
        Recharge recharge = new Recharge();
        
        // 如果是管理员，查询所有充值记录；否则只查询当前用户的充值记录
        if (isAdmin(request)) {
            // 管理员可以查看所有充值记录
        } else {
            // 普通用户只能查看自己的充值记录
            recharge.setUserId(getCurrentUserId(request));
        }
        
        PageInfo<Recharge> page = rechargeService.selectPage(recharge, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 管理员专用：查询所有充值记录（不分页）
     */
    @GetMapping("/admin/selectAll")
    public Result adminSelectAll(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        
        List<Recharge> list = rechargeService.selectAll(new Recharge());
        return Result.success(list);
    }

    /**
     * 管理员专用：分页查询所有充值记录
     */
    @GetMapping("/admin/selectPage")
    public Result adminSelectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        
        PageInfo<Recharge> page = rechargeService.selectPage(new Recharge(), pageNum, pageSize);
        return Result.success(page);
    }

}
