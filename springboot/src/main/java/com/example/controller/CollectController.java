package com.example.controller;

import com.example.common.Result;
import com.example.entity.Collect;
import com.example.service.CollectService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器 - 需要JWT认证
 **/
@RestController
@RequestMapping("/collect")
public class CollectController extends BaseController {

    @Resource
    private CollectService collectService;

    /**
     * 新增 - 自动设置当前用户ID
     */
    @PostMapping("/add")
    public Result add(@RequestBody Collect collect, HttpServletRequest request) {
        // 设置当前用户ID
        collect.setUserId(getCurrentUserId(request));
        collectService.add(collect);
        return Result.success();
    }

    /**
     * 删除 - 只能删除自己的收藏
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id, HttpServletRequest request) {
        Collect collect = collectService.selectById(id);
        if (collect == null) {
            return Result.error("收藏项不存在");
        }
        // 检查是否为当前用户的收藏
        if (!collect.getUserId().equals(getCurrentUserId(request))) {
            return Result.error("无权限删除此收藏项");
        }
        collectService.deleteById(id);
        return Result.success();
    }

    /**
     * 查询当前用户的所有收藏
     */
    @GetMapping("/selectAll")
    public Result selectAll(HttpServletRequest request) {
        Collect collect = new Collect();
        
        // 如果是管理员，查询所有收藏；否则只查询当前用户的收藏
        if (isAdmin(request)) {
            // 管理员可以查看所有收藏
        } else {
            // 普通用户只能查看自己的收藏
            collect.setUserId(getCurrentUserId(request));
        }
        
        List<Collect> list = collectService.selectAll(collect);
        return Result.success(list);
    }

    /**
     * 分页查询收藏 - 管理员可以查看所有收藏，普通用户只能查看自己的收藏
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             HttpServletRequest request) {
        Collect collect = new Collect();
        
        // 如果是管理员，查询所有收藏；否则只查询当前用户的收藏
        if (isAdmin(request)) {
            // 管理员可以查看所有收藏
        } else {
            // 普通用户只能查看自己的收藏
            collect.setUserId(getCurrentUserId(request));
        }
        
        PageInfo<Collect> page = collectService.selectPage(collect, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 管理员专用：查询所有收藏（不分页）
     */
    @GetMapping("/admin/selectAll")
    public Result adminSelectAll(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        
        List<Collect> list = collectService.selectAll(new Collect());
        return Result.success(list);
    }

    /**
     * 管理员专用：分页查询所有收藏
     */
    @GetMapping("/admin/selectPage")
    public Result adminSelectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        
        PageInfo<Collect> page = collectService.selectPage(new Collect(), pageNum, pageSize);
        return Result.success(page);
    }

}
