<template>
  <div class="page-grid">
    <el-card class="stat-card">
      <template #header>
        <div class="header-row">
          <strong>单词管理</strong>
          <el-button type="primary" @click="openCreate">新增单词</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="词库">
          <el-select v-model="query.bookId" clearable filterable placeholder="全部词库" style="width: 220px">
            <el-option v-for="book in books" :key="book.id" :label="book.name" :value="book.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="英文/释义" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="records" border stripe>
        <el-table-column prop="spelling" label="英文单词" min-width="140" />
        <el-table-column prop="partOfSpeech" label="词性" width="90" />
        <el-table-column prop="meaning" label="中文释义" min-width="220" show-overflow-tooltip />
        <el-table-column label="难度" width="100">
          <template #default="{ row }">
            <el-rate :model-value="row.difficulty" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑单词' : '新增单词'" width="680px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="所属词库" prop="bookId">
              <el-select v-model="form.bookId" filterable placeholder="请选择词库" style="width: 100%">
                <el-option v-for="book in books" :key="book.id" :label="book.name" :value="book.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文单词" prop="spelling">
              <el-input v-model="form.spelling" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="词性">
              <el-input v-model="form.partOfSpeech" placeholder="n. / v. / adj." />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="中文释义" prop="meaning">
          <el-input v-model="form.meaning" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="英文例句">
          <el-input v-model="form.exampleSentence" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="例句翻译">
          <el-input v-model="form.exampleTranslation" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="发音地址">
          <el-input v-model="form.audioUrl" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="难度">
              <el-rate v-model="form.difficulty" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">上架</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
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
import { getWordBooks, type WordBook } from '@/api/wordBook';
import { createWord, deleteWord, getWordPage, updateWord, type Word, type WordForm } from '@/api/word';

const records = ref<Word[]>([]);
const books = ref<WordBook[]>([]);
const total = ref(0);
const dialogVisible = ref(false);
const editingId = ref<number>();
const formRef = ref<FormInstance>();

const query = reactive<{ current: number; size: number; bookId?: number; keyword: string; status?: number }>({
  current: 1,
  size: 10,
  bookId: undefined,
  keyword: '',
  status: undefined,
});

const form = reactive<WordForm>({
  bookId: undefined,
  spelling: '',
  partOfSpeech: '',
  meaning: '',
  exampleSentence: '',
  exampleTranslation: '',
  audioUrl: '',
  difficulty: 1,
  status: 1,
});

const rules: FormRules = {
  bookId: [{ required: true, message: '请选择所属词库', trigger: 'change' }],
  spelling: [{ required: true, message: '请输入英文单词', trigger: 'blur' }],
  meaning: [{ required: true, message: '请输入中文释义', trigger: 'blur' }],
};

async function loadBooks() {
  const res = await getWordBooks();
  books.value = res.data.data;
}

async function loadData() {
  const res = await getWordPage({ ...query });
  records.value = res.data.data.records;
  total.value = res.data.data.total;
}

function resetQuery() {
  query.current = 1;
  query.bookId = undefined;
  query.keyword = '';
  query.status = undefined;
  loadData();
}

function resetForm() {
  editingId.value = undefined;
  form.bookId = query.bookId;
  form.spelling = '';
  form.partOfSpeech = '';
  form.meaning = '';
  form.exampleSentence = '';
  form.exampleTranslation = '';
  form.audioUrl = '';
  form.difficulty = 1;
  form.status = 1;
}

function openCreate() {
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: Word) {
  editingId.value = row.id;
  form.bookId = row.bookId;
  form.spelling = row.spelling;
  form.partOfSpeech = row.partOfSpeech || '';
  form.meaning = row.meaning;
  form.exampleSentence = row.exampleSentence || '';
  form.exampleTranslation = row.exampleTranslation || '';
  form.audioUrl = row.audioUrl || '';
  form.difficulty = row.difficulty;
  form.status = row.status;
  dialogVisible.value = true;
}

async function submit() {
  await formRef.value?.validate();
  if (editingId.value) {
    await updateWord(editingId.value, form);
    ElMessage.success('单词已更新');
  } else {
    await createWord(form);
    ElMessage.success('单词已新增');
  }
  dialogVisible.value = false;
  loadData();
  loadBooks();
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该单词吗？', '删除确认', { type: 'warning' });
  await deleteWord(id);
  ElMessage.success('删除成功');
  records.value = records.value.filter((record) => record.id !== id);
  total.value = Math.max(0, total.value - 1);
  if (!records.value.length && query.current > 1) {
    query.current -= 1;
  }
  await loadData();
  await loadBooks();
}

onMounted(() => {
  loadBooks();
  loadData();
});
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
