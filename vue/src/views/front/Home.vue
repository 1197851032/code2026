<template>
  <div class="front-container">
    <!--第一行开始-->
    <div style="display:flex; grid-gap: 15px; margin-bottom: 15px">
      <div style="flex: 1">
        <el-carousel height="415px">
          <el-carousel-item v-for="item in data.carouselList" :key="item.id">
            <img @click="router.push('/front/goodsDetail?id=' + item.goodsId)" :src="item.img" alt="轮播图片" style="width: 100%; height: 450px; cursor:pointer" />
          </el-carousel-item>
        </el-carousel>
      </div>
      <div class="card" style="width: 270px; height: 415px">
        <div style=" display:flex; align-items: center; padding-bottom:5px; border-bottom: 1px solid red">
          <img src="@/assets/imgs/love.png" alt="" style="width: 25px" />
          <div style="color: red; font-weight: bold; font-size:20px; margin-left: 2px">为您推荐</div>
        </div>
        <div style="padding: 10px 0">
          <div  @click="router.push('/front/goodsDetail?id=' + item.id)" style="cursor:pointer; display:flex; grid-gap: 10px; margin-bottom: 10px" v-for="item in data.recommendGoods" :key="item.id">
            <img style="width: 80px; height: 80px" :src="item.img" alt="" />
            <div style="flex: 1; min-width: 0;">
              <div class="line1" style="margin-bottom: 5px; font-size: 14px; font-weight: 500;">{{ item.name }}</div>
              <div style="color: red; font-size: 16px; font-weight: bold;"><span>¥ </span>{{ item.price }}</div>
              <div style="font-size: 12px; color: #666; margin-top: 3px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; width: 100%;">
                <span v-if="item.description">{{ item.description }}</span>
                <span v-else>智能推荐</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!--第一行结束-->

    <div class="card" style="padding: 20px">
      <!--第二行开始-->
      <div style=" display:flex; align-items: flex-end; padding-bottom:5px; border-bottom: 1px solid red; margin-bottom:10px" >
        <div style="flex: 1; display: flex; align-items:center">
          <img src="@/assets/imgs/hot.png" alt="" style="width: 25px" />
          <div style="color: red; font-weight: bold; font-size:20px; margin-left: 2px">热销商品</div>
        </div>
        <div @click="router.push('/front/goods')" style="cursor: pointer">查看更多</div>
      </div>
      <div style="margin-bottom: 30px">
        <el-row :gutter="20">
          <el-col :span="6" v-for="item in data.hotGoods" :key="item.id ">
            <div @click="router.push('/front/goodsDetail?id=' + item.id)" class="item" style="width: 100%; border-radius: 5px; padding:5px; height: 255px">
              <img :src="item.img" alt="" style="width: 100%; height: 180px; border-radius: 5px 5px 0 0" />
              <div style="padding: 5px">
                <div class="line1" style="font-size: 16px;">
                  {{ item.name }}
                </div>
                <div>
                  <span style="color: red">¥</span>
                  <b style="color: red; font-size: 20px">{{ item.price }}</b>
                  <span style="margin-left: 10px; color: #666">销量：{{ item.saleCount }}件</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      <!--第二行结束-->

      <!--第三行开始-->
      <div style=" display:flex; align-items: flex-end; padding-bottom:5px; border-bottom: 1px solid red; margin-bottom:10px" >
        <div style="flex: 1; display: flex; align-items:center">
          <img src="@/assets/imgs/new.png" alt="" style="width: 25px" />
          <div style="color: red; font-weight: bold; font-size:20px; margin-left: 2px">最新上架</div>
        </div>
        <div @click="router.push('/front/goods')" style="cursor: pointer">查看更多</div>
      </div>
      <div>
        <el-row :gutter="20">
          <el-col :span="6" v-for="item in data.newGoods" :key="item.id">
            <div @click="router.push('/front/goodsDetail?id=' + item.id)" class="item" style="width: 100%; border-radius: 5px; padding:5px; height: 255px">
              <img :src="item.img" alt="" style="width: 100%; height: 180px; border-radius: 5px 5px 0 0" />
              <div style="padding: 5px">
                <div class="line1" style="font-size: 16px">
                  {{ item.name }}
                </div>
                <div>
                  <span style="color: red">¥</span>
                  <b style="color: red; font-size: 20px">{{ item.price }}</b>
                  <span style="margin-left: 10px; color: #666">销量：{{ item.saleCount }}件</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      <!--第三行结束-->
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";
import router from "@/router";

const data =reactive({
  carouselList: [],
  hotGoods:[],
  recommendGoods:[],
  newGoods:[]
});

request.get('/carousel/selectAll').then(res => {
  data.carouselList = res.data;
});

request.get('/goods/selectAll',{
  params:{
    status:'上架'
  }
}).then(res => {
  data.hotGoods = res.data.sort((v1,v2) => v2.saleCount - v1.saleCount).splice(0,4);
});

request.get('/goods/selectAll',{
  params:{
    status:'上架'
  }
}).then(res => {
  data.newGoods = res.data.splice(0,4);
});

// 使用推荐系统API获取推荐商品
request.get('/recommendation/goods', {
  params: {
    limit: 4
  }
}).then(res => {
  if (res.code === '200') {
    data.recommendGoods = res.data;
  } else {
    // 如果推荐API失败，使用原来的管理员推荐
    request.get('/goods/selectAll',{
      params:{
        status:'上架'
      }
    }).then(res => {
      data.recommendGoods = res.data.filter(v => v.recommend === '是').splice(0,4);
    });
  }
}).catch(() => {
  // 如果推荐API出错，使用原来的管理员推荐
  request.get('/goods/selectAll',{
    params:{
      status:'上架'
    }
  }).then(res => {
    data.recommendGoods = res.data.filter(v => v.recommend === '是').splice(0,4);
  });
});
</script>

<style>
.item {
  cursor: pointer;
}

.item:hover {
  border: 1px solid red;
}


</style>