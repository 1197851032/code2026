package com.example.controller;

import com.example.common.Result;
import com.example.entity.Comment;
import com.example.service.CommentService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器 - 需要JWT认证
 **/
@RestController
@RequestMapping("/comment")
public class CommentController extends BaseController {

    @Resource
    private CommentService commentService;

    /**
     * 新增 - 自动设置当前用户ID
     */
    @PostMapping("/add")
    public Result add(@RequestBody Comment comment, HttpServletRequest request) {
        // 设置当前用户ID
        comment.setUserId(getCurrentUserId(request));
        commentService.add(comment);
        return Result.success();
    }

    /**
     * 删除 - 只能删除自己的评论
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id, HttpServletRequest request) {
        Comment comment = commentService.selectById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        // 检查是否为当前用户的评论
        if (!comment.getUserId().equals(getCurrentUserId(request))) {
            return Result.error("无权限删除此评论");
        }
        commentService.deleteById(id);
        return Result.success();
    }

    /**
     * 更新 - 只能更新自己的评论
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Comment comment, HttpServletRequest request) {
        Comment existingComment = commentService.selectById(comment.getId());
        if (existingComment == null) {
            return Result.error("评论不存在");
        }
        // 检查是否为当前用户的评论
        if (!existingComment.getUserId().equals(getCurrentUserId(request))) {
            return Result.error("无权限修改此评论");
        }
        comment.setUserId(getCurrentUserId(request));
        commentService.updateById(comment);
        return Result.success();
    }

    /**
     * 查询当前用户的所有评论
     */
    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam(required = false) Integer orderId,
                           @RequestParam(required = false) Integer goodsId,
                           HttpServletRequest request) {
        Comment comment = new Comment();
        
        // 如果指定了订单ID或商品ID，按条件查询
        if (orderId != null) {
            comment.setOrderId(orderId);
        }
        if (goodsId != null) {
            comment.setGoodsId(goodsId);
        }
        
        // 如果没有指定条件，按用户权限查询
        if (orderId == null && goodsId == null) {
            if (isAdmin(request)) {
                // 管理员可以查看所有评论
            } else {
                // 普通用户只能查看自己的评论
                comment.setUserId(getCurrentUserId(request));
            }
        }
        
        List<Comment> list = commentService.selectAll(comment);
        return Result.success(list);
    }

    /**
     * 分页查询评论 - 管理员可以查看所有评论，普通用户只能查看自己的评论
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) Integer goodsId,
                             HttpServletRequest request) {
        Comment comment = new Comment();
        
        // 如果指定了商品ID，按商品ID查询
        if (goodsId != null) {
            comment.setGoodsId(goodsId);
        } else {
            // 否则按用户权限查询
            if (isAdmin(request)) {
                // 管理员可以查看所有评论
            } else {
                // 普通用户只能查看自己的评论
                comment.setUserId(getCurrentUserId(request));
            }
        }
        
        PageInfo<Comment> page = commentService.selectPage(comment, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 管理员专用：查询所有评论（不分页）
     */
    @GetMapping("/admin/selectAll")
    public Result adminSelectAll(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        
        List<Comment> list = commentService.selectAll(new Comment());
        return Result.success(list);
    }

    /**
     * 管理员专用：分页查询所有评论
     */
    @GetMapping("/admin/selectPage")
    public Result adminSelectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限操作，需要管理员权限");
        }
        
        PageInfo<Comment> page = commentService.selectPage(new Comment(), pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 根据商品ID查询评价
     */
    @GetMapping("/selectByGoodsId/{goodsId}")
    public Result selectByGoodsId(@PathVariable Integer goodsId) {
        Comment comment = new Comment();
        comment.setGoodsId(goodsId);
        
        List<Comment> list = commentService.selectAll(comment);
        return Result.success(list);
    }

}
