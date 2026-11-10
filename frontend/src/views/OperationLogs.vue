<template>
  <el-card class="stat-card">
    <template #header><strong>操作日志</strong></template>
    <el-table :data="records" border stripe>
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="module" label="模块" width="130" />
      <el-table-column prop="operation" label="操作" width="100" />
      <el-table-column prop="requestUri" label="请求地址" />
      <el-table-column prop="requestMethod" label="方法" width="90" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="createTime" label="时间" width="180" />
    </el-table>
    <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next, sizes" style="margin-top: 18px; justify-content: flex-end" @change="loadData" />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { getOperationLogs, type OperationLog } from '@/api/admin';

const records = ref<OperationLog[]>([]);
const total = ref(0);
const query = reactive({ current: 1, size: 10 });
async function loadData() {
  const res = await getOperationLogs(query);
  records.value = res.data.data.records;
  total.value = res.data.data.total;
}
onMounted(loadData);
</script>
