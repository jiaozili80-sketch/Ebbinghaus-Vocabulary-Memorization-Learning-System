<template>
  <div class="page-grid">
    <el-row :gutter="18">
      <el-col :span="6" v-for="item in cards" :key="item.label">
        <el-card class="stat-card">
          <div class="muted">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="muted">{{ item.tip }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getAdminDashboard, type AdminDashboard } from '@/api/admin';

const dashboard = ref<AdminDashboard>();
const cards = computed(() => [
  { label: '用户总数', value: dashboard.value?.userCount || 0, tip: `正常用户 ${dashboard.value?.activeUserCount || 0}` },
  { label: '词库数量', value: dashboard.value?.wordBookCount || 0, tip: `单词总数 ${dashboard.value?.wordCount || 0}` },
  { label: '今日学习用户', value: dashboard.value?.todayStudyCount || 0, tip: `今日打卡 ${dashboard.value?.todayCheckinCount || 0}` },
  { label: '复习计划', value: dashboard.value?.reviewPlanCount || 0, tip: `薄弱生词 ${dashboard.value?.wrongWordCount || 0}` },
]);

onMounted(async () => {
  const res = await getAdminDashboard();
  dashboard.value = res.data.data;
});
</script>
