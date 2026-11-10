<template>
  <el-card class="stat-card">
    <template #header>
      <div class="header-row"><strong>公告管理</strong><el-button type="primary" @click="openCreate">发布公告</el-button></div>
    </template>
    <el-form :inline="true" :model="query" class="filter-form">
      <el-form-item label="关键词"><el-input v-model="query.keyword" clearable /></el-form-item>
      <el-form-item label="类型"><el-input v-model="query.type" clearable placeholder="公告/贴士/资讯" /></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="records" border stripe>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag>{{ row.status === 1 ? '发布' : '下架' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="160"><template #default="{ row }"><el-button type="primary" link @click="openEdit(row)">编辑</el-button><el-button type="danger" link @click="remove(row.id)">删除</el-button></template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next, sizes" style="margin-top: 18px; justify-content: flex-end" @change="loadData" />

    <el-dialog v-model="visible" :title="editingId ? '编辑公告' : '发布公告'" width="620px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="form.type" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :value="1">发布</el-radio><el-radio :value="0">下架</el-radio></el-radio-group></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createAnnouncement, deleteAnnouncement, getAnnouncementPage, updateAnnouncement, type Announcement } from '@/api/admin';

const records = ref<Announcement[]>([]);
const total = ref(0);
const visible = ref(false);
const editingId = ref<number>();
const query = reactive({ current: 1, size: 10, keyword: '', type: '' });
const form = reactive({ title: '', content: '', type: '公告', status: 1 });

async function loadData() {
  const res = await getAnnouncementPage(query);
  records.value = res.data.data.records;
  total.value = res.data.data.total;
}
function openCreate() { editingId.value = undefined; Object.assign(form, { title: '', content: '', type: '公告', status: 1 }); visible.value = true; }
function openEdit(row: Announcement) { editingId.value = row.id; Object.assign(form, row); visible.value = true; }
async function submit() { editingId.value ? await updateAnnouncement(editingId.value, form) : await createAnnouncement(form); ElMessage.success('保存成功'); visible.value = false; loadData(); }
async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该公告吗？', '提示');
  await deleteAnnouncement(id);
  ElMessage.success('删除成功');
  records.value = records.value.filter((record) => record.id !== id);
  total.value = Math.max(0, total.value - 1);
  if (!records.value.length && query.current > 1) {
    query.current -= 1;
  }
  await loadData();
}
onMounted(loadData);
</script>

<style scoped>.header-row{display:flex;justify-content:space-between;align-items:center;}</style>
