package com.example.controller;

import com.example.common.Result;
import com.example.entity.Goods;
import com.example.service.GoodsService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器 - 需要JWT认证和管理员权限
 **/
@RestController
@RequestMapping("/goods")
public class GoodsController extends BaseController {

    @Resource
    private GoodsService goodsService;

    /**
     * 新增 - 需要管理员权限
     */
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods, HttpServletRequest request) {
        // 检查是否为管理员
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        goodsService.add(goods);
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
        goodsService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改 - 需要管理员权限
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Goods goods, HttpServletRequest request) {
        // 检查是否为管理员
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        goodsService.updateById(goods);
        return Result.success();
    }

    /**
     * 根据ID查询 - 公开接口，不需要权限
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Goods goods = goodsService.selectById(id);
        return Result.success(goods);
    }

    /**
     * 查询所有 - 公开接口，不需要权限
     */
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Goods> list = goodsService.selectAll(null);
        return Result.success(list);
    }

    /**
     * 分页查询 - 公开接口，不需要权限
     */
    @GetMapping("/selectPage")
    public Result selectPage(Goods goods,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Goods> page = goodsService.selectPage(goods, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 增加浏览量 - 公开接口，不需要权限
     */
    @PutMapping("/addViews/{id}")
    public Result addViews(@PathVariable Integer id) {
        Goods goods = goodsService.selectById(id);
        goods.setViews(goods.getViews() + 1);
        goodsService.updateById(goods);
        return Result.success();
    }

}