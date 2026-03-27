package com.example.controller;

import com.example.common.Result;
import com.example.entity.Cart;
import com.example.service.CartService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器 - 需要JWT认证
 **/
@RestController
@RequestMapping("/cart")
public class CartController extends BaseController {

    @Resource
    private CartService cartService;

    /**
     * 新增 - 自动设置当前用户ID
     */
    @PostMapping("/add")
    public Result add(@RequestBody Cart cart, HttpServletRequest request) {
        // 设置当前用户ID
        cart.setUserId(getCurrentUserId(request));
        cartService.add(cart);
        return Result.success();
    }

    /**
     * 删除 - 只能删除自己的购物车项
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id, HttpServletRequest request) {
        Cart cart = cartService.selectById(id);
        if (cart == null) {
            return Result.error("购物车项不存在");
        }
        // 检查是否为当前用户的购物车项
        if (!cart.getUserId().equals(getCurrentUserId(request))) {
            return Result.error("无权限删除此购物车项");
        }
        cartService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改 - 只能修改自己的购物车项
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Cart cart, HttpServletRequest request) {
        Cart existingCart = cartService.selectById(cart.getId());
        if (existingCart == null) {
            return Result.error("购物车项不存在");
        }
        // 检查是否为当前用户的购物车项
        if (!existingCart.getUserId().equals(getCurrentUserId(request))) {
            return Result.error("无权限修改此购物车项");
        }
        cart.setUserId(getCurrentUserId(request));
        cartService.updateById(cart);
        return Result.success();
    }

    /**
     * 根据ID查询 - 只能查询自己的购物车项
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id, HttpServletRequest request) {
        Cart cart = cartService.selectById(id);
        if (cart == null) {
            return Result.error("购物车项不存在");
        }
        // 检查是否为当前用户的购物车项
        if (!cart.getUserId().equals(getCurrentUserId(request))) {
            return Result.error("无权限查看此购物车项");
        }
        return Result.success(cart);
    }

    /**
     * 查询当前用户的所有购物车项
     */
    @GetMapping("/selectAll")
    public Result selectAll(HttpServletRequest request) {
        Cart cart = new Cart();
        cart.setUserId(getCurrentUserId(request));
        List<Cart> list = cartService.selectAll(cart);
        return Result.success(list);
    }

    /**
     * 分页查询当前用户的购物车项
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             HttpServletRequest request) {
        Cart cart = new Cart();
        cart.setUserId(getCurrentUserId(request));
        PageInfo<Cart> page = cartService.selectPage(cart, pageNum, pageSize);
        return Result.success(page);
    }

}