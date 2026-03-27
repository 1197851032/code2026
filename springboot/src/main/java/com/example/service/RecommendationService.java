package com.example.service;

import com.example.entity.Goods;
import com.example.entity.OrderDetail;
import com.example.entity.Orders;
import com.example.mapper.GoodsMapper;
import com.example.mapper.OrderDetailMapper;
import com.example.mapper.OrdersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 推荐系统服务
 * 实现基于用户的协同过滤推荐算法 + Redis缓存
 */
@Service
public class RecommendationService {

    @Autowired
    private OrdersMapper ordersMapper;
    
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 为用户推荐商品（带Redis缓存）
     * @param userId 用户ID
     * @param limit 推荐商品数量
     * @return 推荐商品列表
     */
    public List<Goods> recommendGoods(Integer userId, int limit) {
        // 1. 检查Redis缓存
        String cacheKey = "recommendation:user:" + userId + ":" + limit;
        List<Goods> cachedRecommendations = getRecommendationsFromCache(cacheKey);
        if (cachedRecommendations != null) {
            System.out.println("=== 从Redis缓存获取推荐结果 ===");
            System.out.println("用户ID: " + userId);
            System.out.println("缓存商品数量: " + cachedRecommendations.size());
            return cachedRecommendations;
        }
        
        // 2. 获取用户购买历史
        List<Integer> userPurchasedGoodsIds = getUserPurchasedGoods(userId);
        
        System.out.println("=== 推荐系统测试 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("购买商品: " + userPurchasedGoodsIds);
        
        // 3. 如果是新用户（没有购买记录），返回管理员推荐商品
        if (userPurchasedGoodsIds.isEmpty()) {
            System.out.println("新用户，返回管理员推荐");
            List<Goods> recommendations = getAdminRecommendedGoodsPrivate(limit);
            // 缓存新用户推荐结果
            saveRecommendationsToCache(cacheKey, recommendations, 30); // 缓存30分钟
            return recommendations;
        }
        
        // 4. 老用户使用协同过滤算法
        System.out.println("老用户，使用协同过滤算法");
        List<Goods> recommendations = getCollaborativeFilteringRecommendations(userId, userPurchasedGoodsIds, limit);
        
        System.out.println("最终推荐商品ID: ");
        for (Goods goods : recommendations) {
            System.out.println("- 商品" + goods.getId() + " (" + goods.getName() + ", 类别" + goods.getCategoryId() + ")");
        }
        
        // 5. 缓存推荐结果
        saveRecommendationsToCache(cacheKey, recommendations, 10); // 缓存10分钟
        
        return recommendations;
    }

    /**
     * 获取用户购买过的商品ID列表（只计算有效订单）
     */
    private List<Integer> getUserPurchasedGoods(Integer userId) {
        // 查询用户的所有订单
        Orders ordersQuery = new Orders();
        ordersQuery.setUserId(userId);
        List<Orders> userOrders = ordersMapper.selectAll(ordersQuery);
        
        Set<Integer> purchasedGoodsIds = new HashSet<>();
        
        for (Orders order : userOrders) {
            // 只计算非取消状态的订单
            if (order.getStatus() != null && !"已取消".equals(order.getStatus())) {
                // 查询订单详情
                OrderDetail detailQuery = new OrderDetail();
                detailQuery.setOrderId(order.getId());
                List<OrderDetail> orderDetails = orderDetailMapper.selectAll(detailQuery);
                
                for (OrderDetail detail : orderDetails) {
                    purchasedGoodsIds.add(detail.getGoodsId());
                }
            }
        }
        
        return new ArrayList<>(purchasedGoodsIds);
    }

    /**
     * 获取热门商品（未登录用户或新用户）
     */
    public List<Goods> getPopularGoods(int limit) {
        Goods goodsQuery = new Goods();
        goodsQuery.setStatus("上架");
        List<Goods> allGoods = goodsMapper.selectAll(goodsQuery);
        
        // 按销量和浏览量排序
        allGoods.sort((a, b) -> {
            int saleCompare = b.getSaleCount().compareTo(a.getSaleCount());
            if (saleCompare != 0) return saleCompare;
            return b.getViews().compareTo(a.getViews());
        });
        
        return allGoods.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取管理员推荐的商品（新用户使用）
     */
    private List<Goods> getAdminRecommendedGoodsPrivate(int limit) {
        return getAdminRecommendedGoodsPrivate(limit, null);
    }
    
    /**
     * 获取管理员推荐的商品（带用户购买过滤）
     */
    private List<Goods> getAdminRecommendedGoodsPrivate(int limit, List<Integer> userPurchasedGoodsIds) {
        Goods goodsQuery = new Goods();
        goodsQuery.setRecommend("是");
        goodsQuery.setStatus("上架"); // 只推荐上架商品
        
        List<Goods> recommendedGoods = goodsMapper.selectAll(goodsQuery);
        
        // 按销量和浏览量排序
        recommendedGoods.sort((a, b) -> {
            int saleCompare = b.getSaleCount().compareTo(a.getSaleCount());
            if (saleCompare != 0) return saleCompare;
            return b.getViews().compareTo(a.getViews());
        });
        
        List<Goods> filteredGoods = new ArrayList<>();
        for (Goods goods : recommendedGoods) {
            // 如果提供了用户购买历史，排除已购买商品
            if (userPurchasedGoodsIds != null && userPurchasedGoodsIds.contains(goods.getId())) {
                continue;
            }
            filteredGoods.add(goods);
            if (filteredGoods.size() >= limit) break;
        }
        
        // 如果过滤后商品不足，补充其他热门商品
        if (filteredGoods.size() < limit) {
            Goods allGoodsQuery = new Goods();
            allGoodsQuery.setStatus("上架");
            List<Goods> allGoods = goodsMapper.selectAll(allGoodsQuery);
            
            // 按销量和浏览量排序
            allGoods.sort((a, b) -> {
                int saleCompare = b.getSaleCount().compareTo(a.getSaleCount());
                if (saleCompare != 0) return saleCompare;
                return b.getViews().compareTo(a.getViews());
            });
            
            for (Goods goods : allGoods) {
                // 跳过已推荐商品和已购买商品
                if (filteredGoods.stream().anyMatch(g -> g.getId().equals(goods.getId()))) continue;
                if (userPurchasedGoodsIds != null && userPurchasedGoodsIds.contains(goods.getId())) continue;
                
                filteredGoods.add(goods);
                if (filteredGoods.size() >= limit) break;
            }
        }
        
        return filteredGoods;
    }

    /**
     * 基于用户的协同过滤推荐算法
     */
    private List<Goods> getCollaborativeFilteringRecommendations(Integer userId, 
                                                              List<Integer> userPurchasedGoodsIds, 
                                                              int limit) {
        // 1. 构建用户-商品评分矩阵（购买行为作为评分，购买=1，未购买=0）
        Map<Integer, Map<Integer, Integer>> userItemMatrix = buildUserItemMatrix();
        
        // 如果用户数量太少（少于3个），直接使用基于内容的推荐
        if (userItemMatrix.size() < 3) {
            return getContentBasedRecommendations(userPurchasedGoodsIds, limit);
        }
        
        // 2. 找到相似用户
        List<Integer> similarUsers = findSimilarUsers(userId, userItemMatrix, userPurchasedGoodsIds);
        
        // 如果没有相似用户，使用基于内容的推荐
        if (similarUsers.isEmpty()) {
            return getContentBasedRecommendations(userPurchasedGoodsIds, limit);
        }
        
        // 3. 基于相似用户的购买记录推荐商品
        Map<Integer, Double> recommendedItems = calculateRecommendations(
                userId, similarUsers, userItemMatrix, userPurchasedGoodsIds);
        
        // 如果协同过滤没有推荐结果，使用基于内容的推荐
        if (recommendedItems.isEmpty()) {
            return getContentBasedRecommendations(userPurchasedGoodsIds, limit);
        }
        
        // 4. 获取推荐商品详情并排序，确保排除已购买商品
        List<Goods> recommendations = getRecommendedGoodsDetailsWithFilter(recommendedItems, limit, userPurchasedGoodsIds);
        
        // 5. 如果协同过滤结果太少，补充基于内容的推荐
        if (recommendations.size() < limit) {
            List<Goods> contentBasedRecs = getContentBasedRecommendations(userPurchasedGoodsIds, limit - recommendations.size());
            for (Goods goods : contentBasedRecs) {
                if (!recommendations.stream().anyMatch(g -> g.getId().equals(goods.getId()))) {
                    recommendations.add(goods);
                }
            }
        }
        
        return recommendations;
    }

    /**
     * 构建用户-商品矩阵
     */
    private Map<Integer, Map<Integer, Integer>> buildUserItemMatrix() {
        Map<Integer, Map<Integer, Integer>> matrix = new HashMap<>();
        
        // 获取所有订单
        List<Orders> allOrders = ordersMapper.selectAll(new Orders());
        
        for (Orders order : allOrders) {
            Integer userId = order.getUserId();
            
            // 获取订单详情
            OrderDetail detailQuery = new OrderDetail();
            detailQuery.setOrderId(order.getId());
            List<OrderDetail> orderDetails = orderDetailMapper.selectAll(detailQuery);
            
            // 初始化用户购买记录
            if (!matrix.containsKey(userId)) {
                matrix.put(userId, new HashMap<>());
            }
            
            Map<Integer, Integer> userPurchases = matrix.get(userId);
            
            for (OrderDetail detail : orderDetails) {
                // 购买次数作为评分
                Integer goodsId = detail.getGoodsId();
                userPurchases.put(goodsId, userPurchases.getOrDefault(goodsId, 0) + detail.getNum());
            }
        }
        
        return matrix;
    }

    /**
     * 找到相似用户（使用余弦相似度）
     */
    private List<Integer> findSimilarUsers(Integer userId, 
                                         Map<Integer, Map<Integer, Integer>> matrix,
                                         List<Integer> userPurchasedGoodsIds) {
        Map<Integer, Integer> currentUserItems = matrix.getOrDefault(userId, new HashMap<>());
        Map<Integer, Double> userSimilarities = new HashMap<>();
        
        // 计算与其他用户的相似度
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : matrix.entrySet()) {
            Integer otherUserId = entry.getKey();
            Map<Integer, Integer> otherUserItems = entry.getValue();
            
            if (otherUserId.equals(userId)) continue;
            
            // 计算余弦相似度
            double similarity = calculateCosineSimilarity(currentUserItems, otherUserItems);
            userSimilarities.put(otherUserId, similarity);
        }
        
        // 按相似度排序，取前10个最相似的用户
        return userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(Map<Integer, Integer> user1Items, 
                                            Map<Integer, Integer> user2Items) {
        // 找到共同购买的商品
        Set<Integer> commonItems = new HashSet<>(user1Items.keySet());
        commonItems.retainAll(user2Items.keySet());
        
        if (commonItems.isEmpty()) return 0.0;
        
        // 计算向量点积
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (Integer itemId : commonItems) {
            dotProduct += user1Items.get(itemId) * user2Items.get(itemId);
        }
        
        // 计算向量模长
        for (Integer rating : user1Items.values()) {
            norm1 += rating * rating;
        }
        for (Integer rating : user2Items.values()) {
            norm2 += rating * rating;
        }
        
        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);
        
        if (norm1 == 0 || norm2 == 0) return 0.0;
        
        return dotProduct / (norm1 * norm2);
    }

    /**
     * 计算推荐商品分数（高级评分预测模型）
     */
    private Map<Integer, Double> calculateRecommendations(Integer userId,
                                                        List<Integer> similarUsers,
                                                        Map<Integer, Map<Integer, Integer>> matrix,
                                                        List<Integer> userPurchasedGoodsIds) {
        Map<Integer, Double> itemScores = new HashMap<>();
        Map<Integer, Integer> currentUserItems = matrix.getOrDefault(userId, new HashMap<>());
        
        // 计算基准评分（用户平均评分）
        double baselineRating = calculateUserAverageRating(currentUserItems);
        
        // 相似度阈值
        final double SIMILARITY_THRESHOLD = 0.1;
        
        for (Integer similarUserId : similarUsers) {
            Map<Integer, Integer> similarUserItems = matrix.getOrDefault(similarUserId, new HashMap<>());
            
            // 计算相似度
            double similarity = calculateCosineSimilarity(currentUserItems, similarUserItems);
            
            // 相似度阈值过滤
            if (similarity < SIMILARITY_THRESHOLD) {
                continue;
            }
            
            // 计算相似用户的基准评分
            double similarUserBaseline = calculateUserAverageRating(similarUserItems);
            
            for (Map.Entry<Integer, Integer> entry : similarUserItems.entrySet()) {
                Integer itemId = entry.getKey();
                Integer rating = entry.getValue();
                
                // 跳过用户已经购买的商品
                if (userPurchasedGoodsIds.contains(itemId)) continue;
                
                // 高级评分预测：基准评分 + 加权偏差
                double ratingDeviation = rating - similarUserBaseline;
                double predictedRating = baselineRating + (similarity * ratingDeviation);
                
                // 累加推荐分数（使用相似度作为权重）
                double weightedScore = similarity * predictedRating;
                itemScores.put(itemId, itemScores.getOrDefault(itemId, 0.0) + weightedScore);
            }
        }
        
        return itemScores;
    }
    
    /**
     * 计算用户平均评分（基准评分）
     */
    private double calculateUserAverageRating(Map<Integer, Integer> userItems) {
        if (userItems.isEmpty()) {
            return 1.0; // 默认基准评分
        }
        
        double sum = 0.0;
        for (Integer rating : userItems.values()) {
            sum += rating;
        }
        
        return sum / userItems.size();
    }

    /**
     * 获取推荐商品详情（带过滤）
     */
    private List<Goods> getRecommendedGoodsDetailsWithFilter(Map<Integer, Double> itemScores, int limit, List<Integer> userPurchasedGoodsIds) {
        if (itemScores.isEmpty()) {
            // 如果没有推荐结果，返回管理员推荐商品（排除已购买）
            return getAdminRecommendedGoodsPrivate(limit, userPurchasedGoodsIds);
        }
        
        // 按推荐分数排序
        List<Integer> recommendedItemIds = itemScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(limit * 2) // 获取更多候选商品，以便过滤
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        List<Goods> recommendedGoods = new ArrayList<>();
        
        for (Integer goodsId : recommendedItemIds) {
            // 确保不推荐已购买的商品
            if (userPurchasedGoodsIds.contains(goodsId)) continue;
            
            Goods goods = goodsMapper.selectById(goodsId);
            if (goods != null && "上架".equals(goods.getStatus())) {
                recommendedGoods.add(goods);
                if (recommendedGoods.size() >= limit) break;
            }
        }
        
        // 如果推荐商品数量不足，补充管理员推荐商品
        if (recommendedGoods.size() < limit) {
            List<Goods> adminGoods = getAdminRecommendedGoodsPrivate(limit - recommendedGoods.size(), userPurchasedGoodsIds);
            for (Goods adminGood : adminGoods) {
                // 避免重复
                if (!recommendedGoods.stream().anyMatch(g -> g.getId().equals(adminGood.getId()))) {
                    recommendedGoods.add(adminGood);
                }
            }
        }
        
        return recommendedGoods;
    }

    /**
     * 基于内容的推荐（基于商品类别）
     */
    private List<Goods> getContentBasedRecommendations(List<Integer> userPurchasedGoodsIds, int limit) {
        if (userPurchasedGoodsIds.isEmpty()) {
            return getAdminRecommendedGoodsPrivate(limit);
        }
        
        // 获取用户购买商品的类别
        Set<Integer> userCategories = new HashSet<>();
        for (Integer goodsId : userPurchasedGoodsIds) {
            Goods goods = goodsMapper.selectById(goodsId);
            if (goods != null && goods.getCategoryId() != null) {
                userCategories.add(goods.getCategoryId());
            }
        }
        
        // 查找相同类别的其他商品
        Goods goodsQuery = new Goods();
        goodsQuery.setStatus("上架");
        List<Goods> allGoods = goodsMapper.selectAll(goodsQuery);
        
        List<Goods> recommendations = new ArrayList<>();
        for (Goods goods : allGoods) {
            // 跳过已购买商品
            if (userPurchasedGoodsIds.contains(goods.getId())) continue;
            
            // 优先推荐相同类别的商品
            if (userCategories.contains(goods.getCategoryId())) {
                recommendations.add(goods);
                if (recommendations.size() >= limit) break;
            }
        }
        
        // 如果同类商品不够，补充热门商品
        if (recommendations.size() < limit) {
            for (Goods goods : allGoods) {
                if (!userPurchasedGoodsIds.contains(goods.getId()) && 
                    !recommendations.stream().anyMatch(g -> g.getId().equals(goods.getId()))) {
                    recommendations.add(goods);
                    if (recommendations.size() >= limit) break;
                }
            }
        }
        
        return recommendations;
    }
    /**
     * 获取热门商品（备选方案）
     */
    private List<Goods> getPopularGoods(int limit) {
        Goods goodsQuery = new Goods();
        goodsQuery.setStatus("上架");
        List<Goods> allGoods = goodsMapper.selectAll(goodsQuery);
        
        // 按销量和浏览量排序
        allGoods.sort((a, b) -> {
            int saleCompare = b.getSaleCount().compareTo(a.getSaleCount());
            if (saleCompare != 0) return saleCompare;
            return b.getViews().compareTo(a.getViews());
        });
        
        return allGoods.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== Redis缓存相关方法 ====================
    
    /**
     * 从Redis缓存获取推荐结果
     * @param cacheKey 缓存键
     * @return 推荐商品列表，如果缓存不存在返回null
     */
    @SuppressWarnings("unchecked")
    private List<Goods> getRecommendationsFromCache(String cacheKey) {
        try {
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return (List<Goods>) cachedData;
            }
        } catch (Exception e) {
            System.err.println("从Redis缓存获取推荐结果失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 保存推荐结果到Redis缓存
     * @param cacheKey 缓存键
     * @param recommendations 推荐商品列表
     * @param timeoutMinutes 超时时间（分钟）
     */
    private void saveRecommendationsToCache(String cacheKey, List<Goods> recommendations, int timeoutMinutes) {
        try {
            redisTemplate.opsForValue().set(cacheKey, recommendations, timeoutMinutes, TimeUnit.MINUTES);
            System.out.println("推荐结果已缓存到Redis，键: " + cacheKey + ", 超时时间: " + timeoutMinutes + "分钟");
        } catch (Exception e) {
            System.err.println("保存推荐结果到Redis缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除用户的推荐缓存
     * @param userId 用户ID
     */
    public void clearUserRecommendationCache(Integer userId) {
        try {
            // 清除所有可能的缓存键
            Set<String> keys = redisTemplate.keys("recommendation:user:" + userId + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                System.out.println("已清除用户" + userId + "的推荐缓存，共" + keys.size() + "个键");
            }
        } catch (Exception e) {
            System.err.println("清除用户推荐缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除所有推荐缓存
     */
    public void clearAllRecommendationCache() {
        try {
            Set<String> keys = redisTemplate.keys("recommendation:user:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                System.out.println("已清除所有推荐缓存，共" + keys.size() + "个键");
            }
        } catch (Exception e) {
            System.err.println("清除所有推荐缓存失败: " + e.getMessage());
        }
    }
}
