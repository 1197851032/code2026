<template>
  <div>
    <div style="height: 60px; background-color: #2e3143; display: flex; align-items: center; border-bottom: 1px solid #ddd">
      <div style="flex: 1">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px; color: #fff">校园小卖部</div>
        </div>
      </div>
      <div style="width: fit-content; padding-right: 10px;">
        <el-dropdown>
          <div style="display: flex; align-items: center;">
            <img style="width: 40px; height: 40px; border-radius: 50%" :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="">
            <span style="color: #fff; margin-left: 5px">{{ data.user.name || '代码小白' }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click.native="router.push('/front/person')">个人信息</el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/password')">修改密码</el-dropdown-item>
              <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div style="background-color:#f0f2ff">
      <router-view @updateUser="updateUser" />
    </div>

    <Footer />
  </div>
</template>

<script setup>
import {reactive} from "vue";
import router from "@/router";
import {ElMessage} from "element-plus";
import Footer from "@/components/Footer.vue";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
})
if (!data.user?.id){
  ElMessage.error('请登录！')
  router.push('/login')
}

const logout = () => {
  localStorage.removeItem('system-user');
  router.push('/login');
  ElMessage.success('退出成功')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}
</script>

<style>
.el-tooltip__trigger{
  cursor: pointer;
  outline: none !important;
}
</style>