package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    public List<User> selectAll(String name) {
        return userMapper.selectAll(name);
    }

    public PageInfo<User> selectPage(Integer pageNum, Integer pageSize, String name){
        PageHelper.startPage(pageNum,pageSize);
        List<User> list = userMapper.selectAll(name);
        return PageInfo.of(list);
    }

    public void deleteById(Integer id){
        userMapper.deleteById(id);
    }

    //判断传入的user实例用户名不重复后将该实例加入数据库，有缺省密码123与名字灰灰，以及固定用户角色普通用户与账户余额0
    public void add(User user){
        String username = user.getUsername();
        User dbUser = userMapper.selectByUsername(username);
        if (dbUser != null){
            throw new CustomException("新增失败！账号重复");
        }
        if(StrUtil.isBlank(user.getPassword())){
            user.setPassword("123");
        }
        if(StrUtil.isBlank(user.getName())){
            user.setName("灰灰");
        }
        user.setRole("普通用户");
        user.setAccount(BigDecimal.ZERO);
        userMapper.insert(user);
    }

    public void update(User user) {
        userMapper.updateById(user);
    }

    public Account login(Account account) {
        Account dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        return dbUser;
    }

    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    public void updatePassword(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbUser.setPassword(account.getNewPassword());
        userMapper.updateById(dbUser);
    }
}
