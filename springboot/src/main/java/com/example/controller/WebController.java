package com.example.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.example.common.Result;
import com.example.entity.*;
import com.example.mapper.OrderDetailMapper;
import com.example.service.*;
import com.example.util.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 
 */
@RestController
public class WebController {

    @Resource
    private AdminService adminService;
    @Resource
    private UserService userService;
    @Resource
    private OrdersService ordersService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 
     */
    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }

    /**
     * - JWT
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account ac = null;
        if ("管理员".equals(account.getRole())) {
            ac = adminService.login(account);
        }
        if ("普通用户".equals(account.getRole())) {
            ac = userService.login(account);
        }
        if(ac == null){
            return Result.error("登录失败，用户不存在");
        }
        
        // JWT token
        String token = jwtUtil.generateToken(ac.getUsername(), ac.getId(), ac.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", ac);
        
        return Result.success(result);
    }

    /**
     * 
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if (!user.getPassword().equals(user.getNewPassword())){
            return Result.error("两次输入的密码不一致");
        }
        userService.add(user);
        return Result.success();
    }

    /**
     * 
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if ("管理员".equals(account.getRole())) {
            adminService.updatePassword(account);
        }
        if ("普通用户".equals(account.getRole())) {
            userService.updatePassword(account);
        }
        return Result.success();
    }

    @GetMapping("/count")
    public Result count() {
        List<Orders> ordersList = ordersService.selectAll(null).stream().filter(orders -> !orders.getStatus().equals("已取消")).toList();
        BigDecimal total = ordersList.stream().map(Orders::getTotal).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        String todayDate = DateUtil.today();
        BigDecimal today = ordersList.stream().filter(orders -> orders.getTime().contains(todayDate))
                .map(Orders::getTotal).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        Integer goods = goodsService.selectAll(null).size();
        Integer user = userService.selectAll(null).size();
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("today", today);
        map.put("goods", goods);
        map.put("user", user);
        return Result.success(map);
    }

    @GetMapping("/selectLine")
    public Result selectLine() {
        Date date = new Date();
        DateTime start = DateUtil.offsetDay(date, -6);
        List<DateTime> dateTimes = DateUtil.rangeToList(start, date, DateField.DAY_OF_YEAR);
        List<String> dateStrList = dateTimes.stream().map(dateTime -> DateUtil.format(dateTime, "MM-dd")).sorted().toList();
        List<Orders> ordersList = ordersService.selectAll(null).stream().filter(orders -> !orders.getStatus().equals("已取消")).toList();
        int year = DateUtil.year(date);
        ArrayList<BigDecimal> countList = new ArrayList<>();
        for (String day : dateStrList) {
            BigDecimal total = ordersList.stream().filter(o -> o.getTime().contains(String.valueOf(year)) && o.getTime().contains(day))
                    .map(Orders::getTotal).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            countList.add(total);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("date", dateStrList);
        map.put("count", countList);
        return Result.success(map);
    }

    @GetMapping("/selectPie")
    public Result selectPie() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Category> categoryList = categoryService.selectAll(null);
        Map<String, Object> map;
        for (Category category : categoryList){
            map = new HashMap<>();
            map.put("name", category.getName());
            BigDecimal total = BigDecimal.ZERO;
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(null);
            for(OrderDetail orderDetail : orderDetailList){
                Integer orderId = orderDetail.getOrderId();
                Orders orders = ordersService.selectById(orderId);
                if (!"已取消".equals(orders.getStatus())) {
                    Integer goodsId = orderDetail.getGoodsId();
                    Goods goods = goodsService.selectById(goodsId);
                    if (goods.getCategoryId().equals(category.getId())){
                        total = total.add(orders.getTotal());
                    }
                }
            }
            map.put("value",total);
            if(total.compareTo(BigDecimal.ZERO) > 0){
                list.add(map);
            }
        }
        return Result.success(list);
    }

}