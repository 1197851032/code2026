package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.db.sql.Order;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 管理员业务处理
 **/
@Service
public class OrdersService {

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private CartMapper cartMapper;

    /**
     * 新增
     */
    @Transactional
    public void add(Orders orders) {
        //补充初始提交订单信息
        orders.setStatus("待接单");
        orders.setTime(DateUtil.now());
        String orderNo = DateUtil.format(new Date(),"yyyyMMdd") + System.currentTimeMillis() + RandomUtil.randomNumbers(4);
        orders.setOrderNo(orderNo);
        ordersMapper.insert(orders);
        //预声明变量
        Integer orderId = orders.getId();
        List<Cart> cartList = orders.getCartList();
        User user = userMapper.selectById(orders.getUserId());
        BigDecimal totalPrice = BigDecimal.ZERO;
        //遍历处理订单内购物车商品
        for (Cart cart : cartList) {
            //预声明变量
            Integer goodsId = cart.getGoodsId();
            Goods goods = goodsMapper.selectById(goodsId);
            //判断商品库存是否足够，够就减去库存增加销量
            if(goods.getStore() < cart.getNum()){
                throw new CustomException(goods.getName() + "商品库存不足");
            }
            goods.setStore(goods.getStore() - cart.getNum());
            goods.setSaleCount(goods.getSaleCount() + cart.getNum());
            goodsMapper.updateById(goods);
            //添加对应商品信息到订单细节表
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setNum(cart.getNum());
            orderDetail.setGoodsId(goodsId);
            orderDetail.setGoodsImg(goods.getImg());
            orderDetail.setGoodsName(goods.getName());
            orderDetail.setGoodsPrice(goods.getPrice());
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insert(orderDetail);
            //删除下单商品的购物车记录
            if(cart.getId() != null){
                cartMapper.deleteById(cart.getId());
            }
            //累加计算总价
            totalPrice = totalPrice.add(goods.getPrice().multiply(BigDecimal.valueOf(cart.getNum())));

        }
        //判断用户余额是否充足，够就扣对应钱
        if (user.getAccount().compareTo(totalPrice) < 0) {
            throw new CustomException("您的账户余额不足，请充值");
        }
        user.setAccount(user.getAccount().subtract(totalPrice));
        userMapper.updateById(user);
        //补全订单总金额信息
        orders.setTotal(totalPrice);
        ordersMapper.updateById(orders);
    }

    /**
     * 删除
     */
    @Transactional
    public void deleteById(Integer id) {
        ordersMapper.deleteById(id);
        orderDetailMapper.deleteByOrderId(id);
    }

    /**
     * 修改
     */
    @Transactional
    public void updateById(Orders orders) {
        if("已取消".equals(orders.getStatus())){
            Integer userId = orders.getUserId();
            User user = userMapper.selectById(userId);
            user.setAccount(user.getAccount().add(orders.getTotal()));
            userMapper.updateById((user));
            // 加回商品库存减去销量
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orders.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(orderDetail);
            for (OrderDetail detail : orderDetailList){
                Integer goodsId = detail.getGoodsId();
                Goods goods = goodsMapper.selectById(goodsId);
                if(goods != null ){
                    goods.setStore(goods.getStore() + detail.getNum());
                    goods.setSaleCount(goods.getSaleCount() - detail.getNum());
                    goodsMapper.updateById(goods);
                }
            }
        }
        ordersMapper.updateById(orders);
    }

    /**
     * 根据ID查询
     */
    public Orders selectById(Integer id) {
        return ordersMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Orders> selectAll(Orders orders) {
        return ordersMapper.selectAll(orders);
    }

    /**
     * 分页查询
     */
    public PageInfo<Orders> selectPage(Orders orders, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> list = ordersMapper.selectAll(orders);
        for(Orders o : list){
            OrderDetail orderDetail =new OrderDetail();
            orderDetail.setOrderId(o.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(orderDetail);
            o.setOrderDetailList(orderDetailList);
        }
        return PageInfo.of(list);
    }
}