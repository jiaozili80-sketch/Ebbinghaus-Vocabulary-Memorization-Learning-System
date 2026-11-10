<template>
  <router-view v-if="isAuthPage" />
  <el-container v-else class="app-shell">
    <el-aside width="248px" class="sidebar">
      <div class="brand">
        <div class="brand-logo">E</div>
        <div>
          <h1>艾宾浩斯</h1>
          <p>Vocabulary System</p>
        </div>
      </div>
      <el-menu router :default-active="$route.path" class="nav-menu">
        <el-menu-item index="/dashboard">学习总览</el-menu-item>
        <el-menu-item index="/word-books">词库中心</el-menu-item>
        <el-menu-item index="/study">核心背诵</el-menu-item>
        <el-menu-item index="/wrong-words">生词错题</el-menu-item>
        <el-menu-item index="/schedule">学习日程</el-menu-item>
        <el-menu-item index="/statistics">数据统计</el-menu-item>
        <el-sub-menu v-if="isAdmin" index="/manage">
          <template #title>后台管理</template>
          <el-menu-item index="/manage/dashboard">数据总览</el-menu-item>
          <el-menu-item index="/manage/users">用户管理</el-menu-item>
          <el-menu-item index="/manage/word-books">词库管理</el-menu-item>
          <el-menu-item index="/manage/words">单词管理</el-menu-item>
          <el-menu-item index="/manage/announcements">公告管理</el-menu-item>
          <el-menu-item index="/manage/logs">操作日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container class="main-shell">
      <el-header class="topbar">
        <div>
          <strong>{{ $route.meta.title }}</strong>
          <span class="topbar-subtitle">基于遗忘曲线的智能复习规划</span>
        </div>
        <div class="topbar-actions">
          <el-button type="primary" round @click="router.push('/schedule')">今日打卡</el-button>
          <div class="user-panel">
            <span class="user-label">当前用户</span>
            <el-dropdown @command="handleCommand">
              <span class="user-entry">
                {{ userStore.userInfo?.nickname || userStore.userInfo?.username || '学习者' }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button class="mobile-logout" plain size="small" @click="handleCommand('logout')">退出</el-button>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const isAuthPage = computed(() => route.path === '/login' || route.path === '/register');
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN');

onMounted(() => {
  if (userStore.token) {
    userStore.loadUserInfo().catch(() => undefined);
  }
});

async function handleCommand(command: string) {
  if (command === 'logout') {
    await userStore.logout();
    router.push('/login');
  }
}
</script>
