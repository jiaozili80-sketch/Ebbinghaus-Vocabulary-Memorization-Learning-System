<template>
  <div class="page-grid">
    <el-card class="stat-card">
      <template #header>
        <div class="header-row">
          <strong>词库管理</strong>
          <el-button type="primary" @click="openCreate">新增词库</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="名称/描述" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="query.category" placeholder="如 CET4" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="records" border stripe>
        <el-table-column prop="name" label="词库名称" min-width="180" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="wordCount" label="单词数" width="100" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'warning'">{{ row.type === 1 ? '公共' : '私人' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        layout="total, prev, pager, next, sizes"
        :total="total"
        style="margin-top: 18px; justify-content: flex-end"
        @change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑词库' : '新增词库'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="词库名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="如 CET4 / 考研 / 雅思" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">公共词库</el-radio>
            <el-radio :value="2">私人词库</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { createWordBook, deleteWordBook, getWordBookPage, updateWordBook, type WordBook, type WordBookForm } from '@/api/wordBook';

const records = ref<WordBook[]>([]);
const total = ref(0);
const dialogVisible = ref(false);
const editingId = ref<number>();
const formRef = ref<FormInstance>();

const query = reactive<{ current: number; size: number; keyword: string; category: string; status?: number }>({
  current: 1,
  size: 10,
  keyword: '',
  category: '',
  status: undefined,
});

const form = reactive<WordBookForm>({ name: '', category: '', description: '', type: 1, status: 1 });

const rules: FormRules = {
  name: [{ required: true, message: '请输入词库名称', trigger: 'blur' }],
  category: [{ required: true, message: '请输入词库分类', trigger: 'blur' }],
};

async function loadData() {
  const res = await getWordBookPage({ ...query });
  records.value = res.data.data.records;
  total.value = res.data.data.total;
}

function resetQuery() {
  query.current = 1;
  query.keyword = '';
  query.category = '';
  query.status = undefined;
  loadData();
}

function resetForm() {
  form.name = '';
  form.category = '';
  form.description = '';
  form.type = 1;
  form.status = 1;
  editingId.value = undefined;
}

function openCreate() {
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: WordBook) {
  editingId.value = row.id;
  form.name = row.name;
  form.category = row.category;
  form.description = row.description;
  form.type = row.type;
  form.status = row.status;
  dialogVisible.value = true;
}

async function submit() {
  await formRef.value?.validate();
  if (editingId.value) {
    await updateWordBook(editingId.value, form);
    ElMessage.success('词库已更新');
  } else {
    await createWordBook(form);
    ElMessage.success('词库已新增');
  }
  dialogVisible.value = false;
  loadData();
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该词库吗？删除后不可在列表中显示。', '删除确认', { type: 'warning' });
  await deleteWordBook(id);
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

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-form {
  margin-bottom: 12px;
}
</style>
