<template>
  <el-card class="stat-card">
    <template #header><strong>用户管理</strong></template>
    <el-form :inline="true" :model="query" class="filter-form">
      <el-form-item label="关键词"><el-input v-model="query.keyword" clearable placeholder="用户名/昵称/邮箱" /></el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable style="width: 120px">
          <el-option label="正常" :value="1" />
          <el-option label="封禁" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="records" border stripe>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="totalWords" label="累计背词" />
      <el-table-column prop="masteredWords" label="已掌握" />
      <el-table-column prop="continuousCheckinDays" label="连续打卡" />
      <el-table-column label="状态">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '封禁' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button :type="row.status === 1 ? 'danger' : 'success'" link @click="toggleStatus(row)">{{ row.status === 1 ? '封禁' : '解封' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next, sizes" style="margin-top: 18px; justify-content: flex-end" @change="loadData" />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { getAdminUsers, updateAdminUserStatus, type AdminUser } from '@/api/admin';

const records = ref<AdminUser[]>([]);
const total = ref(0);
const query = reactive<{ current: number; size: number; keyword: string; status?: number }>({ current: 1, size: 10, keyword: '', status: undefined });

async function loadData() {
  const res = await getAdminUsers({ ...query });
  records.value = res.data.data.records;
  total.value = res.data.data.total;
}

async function toggleStatus(row: AdminUser) {
  await updateAdminUserStatus(row.id, row.status === 1 ? 0 : 1);
  ElMessage.success('用户状态已更新');
  loadData();
}

onMounted(loadData);
</script>
