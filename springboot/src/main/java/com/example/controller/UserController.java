package com.example.controller;

import com.example.common.Result;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.util.JwtUtil;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    //接口请求方式：http://localhost:9090/user/selectPage?pageNum=1&pageSize=10
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String name){
        PageInfo<User> pageInfo = userService.selectPage(pageNum,pageSize,name);
        return Result.success(pageInfo);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        userService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/add")
    public Result add(@RequestBody User user){
        userService.add(user);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody User user){
        userService.update(user);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        User user = userService.selectById(id);
        return Result.success(user);
    }
    
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        
        try {
            // 验证用户登录
            User user = userService.login(username, password);
            if (user == null) {
                return Result.error("用户名或密码错误");
            }
            
            // 生成JWT token
            String token = jwtUtil.generateToken(username, user.getId(), "USER");
            
            // 返回token和用户信息
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", user);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        try {
            // 检查用户名是否已存在
            if (userService.selectByUsername(user.getUsername()) != null) {
                return Result.error("用户名已存在");
            }
            
            // 注册用户
            userService.add(user);
            
            // 自动登录并生成token
            User loginUser = userService.login(user.getUsername(), user.getPassword());
            String token = jwtUtil.generateToken(loginUser.getUsername(), loginUser.getId(), "USER");
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", loginUser);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("注册失败: " + e.getMessage());
        }
    }
}
