package com.example.controller;

import com.example.common.Result;
import com.example.entity.Admin;
import com.example.service.AdminService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员控制器 - 需要JWT认证和管理员权限
 **/
@RestController
@RequestMapping("/admin")
public class AdminController extends BaseController {

    @Resource
    private AdminService adminService;

    /**
     * 新增 - 需要管理员权限
     */
    @PostMapping("/add")
    public Result add(@RequestBody Admin admin, HttpServletRequest request) {
        // 检查是否为管理员
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        adminService.add(admin);
        return Result.success();
    }

    /**
     * 删除 - 需要管理员权限
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id, HttpServletRequest request) {
        // 检查是否为管理员
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        adminService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改 - 需要管理员权限
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Admin admin, HttpServletRequest request) {
        // 检查是否为管理员
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        adminService.updateById(admin);
        return Result.success();
    }

    /**
     * 根据ID查询 - 需要管理员权限
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id, HttpServletRequest request) {
        // 检查是否为管理员
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        Admin admin = adminService.selectById(id);
        return Result.success(admin);
    }

    /**
     * 查询所有 - 需要管理员权限
     */
    @GetMapping("/selectAll")
    public Result selectAll(HttpServletRequest request) {
        // 检查是否为管理员
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        List<Admin> list = adminService.selectAll(null);
        return Result.success(list);
    }

    /**
     * 分页查询 - 需要管理员权限
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             HttpServletRequest request) {
        // 检查是否为管理员
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        PageInfo<Admin> page = adminService.selectPage(null, pageNum, pageSize);
        return Result.success(page);
    }

}