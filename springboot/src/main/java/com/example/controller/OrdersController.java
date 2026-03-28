package com.example.controller;

import com.example.common.Result;
import com.example.entity.Orders;
import com.example.service.OrdersService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器 - 需要JWT认证
 **/
@RestController
@RequestMapping("/orders")
public class OrdersController extends BaseController {

    @Resource
    private OrdersService ordersService;

    /**
     * 新增 - 自动设置当前用户ID
     */
    @PostMapping("/add")
    public Result add(@RequestBody Orders orders, HttpServletRequest request) {
        // 设置当前用户ID
        orders.setUserId(getCurrentUserId(request));
        ordersService.add(orders);
        return Result.success();
    }

    /**
     * 删除 - 只能删除自己的订单
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id, HttpServletRequest request) {
        Orders orders = ordersService.selectById(id);
        if (orders == null) {
            return Result.error("订单不存在");
        }
        // 检查是否为当前用户的订单
        if (!orders.getUserId().equals(getCurrentUserId(request))) {
            return Result.error("无权限删除此订单");
        }
        ordersService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改 - 只能修改自己的订单，管理员可以修改所有订单
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Orders orders, HttpServletRequest request) {
        Orders existingOrders = ordersService.selectById(orders.getId());
        if (existingOrders == null) {
            return Result.error("订单不存在");
        }
        
        // 如果不是管理员，检查是否为当前用户的订单
        if (!isAdmin(request)) {
            if (!existingOrders.getUserId().equals(getCurrentUserId(request))) {
                return Result.error("无权限修改此订单");
            }
            orders.setUserId(getCurrentUserId(request));
        }
        
        ordersService.updateById(orders);
        return Result.success();
    }

    /**
     * 根据ID查询 - 只能查询自己的订单
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id, HttpServletRequest request) {
        Orders orders = ordersService.selectById(id);
        if (orders == null) {
            return Result.error("订单不存在");
        }
        // 检查是否为当前用户的订单
        if (!orders.getUserId().equals(getCurrentUserId(request))) {
            return Result.error("无权限查看此订单");
        }
        return Result.success(orders);
    }

    /**
     * 查询当前用户的所有订单
     */
    @GetMapping("/selectAll")
    public Result selectAll(HttpServletRequest request) {
        Orders orders = new Orders();
        
        // 如果是管理员，查询所有订单；否则只查询当前用户的订单
        if (isAdmin(request)) {
            // 管理员可以查看所有订单
        } else {
            // 普通用户只能查看自己的订单
            orders.setUserId(getCurrentUserId(request));
        }
        
        List<Orders> list = ordersService.selectAll(orders);
        return Result.success(list);
    }

    /**
     * 分页查询订单 - 管理员可以查看所有订单，普通用户只能查看自己的订单
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             HttpServletRequest request) {
        Orders orders = new Orders();
        
        // 如果是管理员，查询所有订单；否则只查询当前用户的订单
        if (isAdmin(request)) {
            // 管理员可以查看所有订单
        } else {
            // 普通用户只能查看自己的订单
            orders.setUserId(getCurrentUserId(request));
        }
        
        PageInfo<Orders> page = ordersService.selectPage(orders, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 管理员专用：查询所有订单（不分页）
     */
    @GetMapping("/admin/selectAll")
    public Result adminSelectAll(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        
        List<Orders> list = ordersService.selectAll(new Orders());
        return Result.success(list);
    }

    /**
     * 管理员专用：分页查询所有订单
     */
    @GetMapping("/admin/selectPage")
    public Result adminSelectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        
        PageInfo<Orders> page = ordersService.selectPage(new Orders(), pageNum, pageSize);
        return Result.success(page);
    }

}