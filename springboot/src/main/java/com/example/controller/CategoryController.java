package com.example.controller;

import com.example.common.Result;
import com.example.entity.Category;
import com.example.service.CategoryService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器 - 需要JWT认证和管理员权限
 **/
@RestController
@RequestMapping("/category")
public class CategoryController extends BaseController {

    @Resource
    private CategoryService categoryService;

    /**
     * 新增 - 需要管理员权限
     */
    @PostMapping("/add")
    public Result add(@RequestBody Category category, HttpServletRequest request) {
        // 检查是否为管理员
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        categoryService.add(category);
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
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改 - 需要管理员权限
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Category category, HttpServletRequest request) {
        // 检查是否为管理员
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        categoryService.updateById(category);
        return Result.success();
    }

    /**
     * 根据ID查询 - 公开接口，不需要权限
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Category category = categoryService.selectById(id);
        return Result.success(category);
    }

    /**
     * 查询所有 - 公开接口，不需要权限
     */
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Category> list = categoryService.selectAll(null);
        return Result.success(list);
    }

    /**
     * 分页查询 - 公开接口，不需要权限
     */
    @GetMapping("/selectPage")
    public Result selectPage(Category category,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Category> page = categoryService.selectPage(category, pageNum, pageSize);
        return Result.success(page);
    }

}