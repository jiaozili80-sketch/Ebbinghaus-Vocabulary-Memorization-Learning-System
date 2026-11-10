<template>
  <div class="page-grid">
    <el-card class="stat-card">
      <template #header>
        <div class="header-row">
          <strong>生词本与错题本</strong>
          <el-space>
            <el-button type="success" @click="openReinforce">生词强化背诵</el-button>
            <el-button @click="exportCsv">批量导出</el-button>
          </el-space>
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
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="records" border stripe>
        <el-table-column prop="spelling" label="英文单词" min-width="140" />
        <el-table-column prop="partOfSpeech" label="词性" width="90" />
        <el-table-column prop="meaning" label="中文释义" min-width="220" show-overflow-tooltip />
        <el-table-column prop="wrongCount" label="薄弱次数" width="100" sortable />
        <el-table-column prop="updateTime" label="最近收录/强化" min-width="170">
          <template #default="{ row }">{{ formatTime(row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="preview(row)">详情</el-button>
            <el-button type="success" link @click="markMastered(row.id)">已掌握</el-button>
            <el-button type="danger" link @click="remove(row.id)">移除</el-button>
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

    <el-dialog v-model="detailVisible" title="生词详情" width="620px">
      <template v-if="current">
        <h2>{{ current.spelling }}</h2>
        <p class="muted">{{ current.partOfSpeech }} · 薄弱 {{ current.wrongCount }} 次</p>
        <el-divider />
        <p><strong>释义：</strong>{{ current.meaning }}</p>
        <p><strong>例句：</strong>{{ current.exampleSentence || '暂无例句' }}</p>
        <p><strong>翻译：</strong>{{ current.exampleTranslation || '暂无翻译' }}</p>
      </template>
    </el-dialog>

    <el-dialog v-model="reinforceVisible" title="生词强化背诵" width="760px">
      <div v-if="reinforceWord" class="reinforce-card">
        <div class="word">{{ reinforceWord.spelling }}</div>
        <p class="meaning">{{ showAnswer ? `${reinforceWord.partOfSpeech || ''} ${reinforceWord.meaning}` : '请回忆该生词释义' }}</p>
        <p class="example">{{ showAnswer ? reinforceWord.exampleSentence || '暂无例句' : '点击显示答案后查看例句' }}</p>
        <div class="actions" v-if="!showAnswer">
          <el-button type="primary" size="large" @click="showAnswer = true">显示答案</el-button>
        </div>
        <div class="actions" v-else>
          <el-button type="success" size="large" :loading="submittingReinforce" @click="submitReinforce(3)">认识，下一题</el-button>
          <el-button type="warning" size="large" :loading="submittingReinforce" @click="submitReinforce(1)">继续强化</el-button>
        </div>
      </div>
      <el-empty v-else description="暂无可强化的生词" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getWordBooks, type WordBook } from '@/api/wordBook';
import { getWrongWordPage, exportWrongWords, getWrongWordReinforce, markWrongWordMastered, removeWrongWord, type WrongWord } from '@/api/wrongWord';
import { submitStudy, type StudyWord } from '@/api/study';

const books = ref<WordBook[]>([]);
const records = ref<WrongWord[]>([]);
const total = ref(0);
const detailVisible = ref(false);
const current = ref<WrongWord>();
const reinforceVisible = ref(false);
const reinforceQueue = ref<StudyWord[]>([]);
const reinforceIndex = ref(0);
const showAnswer = ref(false);
const submittingReinforce = ref(false);
const reinforceWord = computed(() => reinforceQueue.value[reinforceIndex.value]);

const query = reactive<{ current: number; size: number; bookId?: number; keyword: string }>({
  current: 1,
  size: 10,
  bookId: undefined,
  keyword: '',
});

async function loadBooks() {
  const res = await getWordBooks();
  books.value = res.data.data;
}

async function loadData() {
  const res = await getWrongWordPage({ ...query });
  records.value = res.data.data.records;
  total.value = res.data.data.total;
}

async function resetQuery() {
  query.current = 1;
  query.size = 10;
  delete query.bookId;
  query.keyword = '';
  await loadData();
  ElMessage.success('已重置筛选条件');
}

function preview(row: WrongWord) {
  current.value = row;
  detailVisible.value = true;
}

async function markMastered(id: number) {
  await ElMessageBox.confirm('确认将该生词标记为已掌握吗？', '确认操作', { type: 'success' });
  await markWrongWordMastered(id);
  ElMessage.success('已标记为掌握');
  loadData();
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认从生词本移除该单词吗？', '移除确认', { type: 'warning' });
  await removeWrongWord(id);
  ElMessage.success('已移除');
  loadData();
}

async function openReinforce() {
  const res = await getWrongWordReinforce({ bookId: query.bookId, limit: 20 });
  reinforceQueue.value = res.data.data;
  reinforceIndex.value = 0;
  showAnswer.value = false;
  reinforceVisible.value = true;
}

function nextReinforce() {
  if (reinforceIndex.value < reinforceQueue.value.length - 1) {
    reinforceIndex.value += 1;
    showAnswer.value = false;
  } else {
    ElMessage.success('本轮生词强化完成');
    reinforceVisible.value = false;
  }
}

async function submitReinforce(familiarity: number) {
  if (!reinforceWord.value || submittingReinforce.value) return;
  submittingReinforce.value = true;
  try {
    await submitStudy({ wordId: reinforceWord.value.wordId, familiarity, duration: 20 });
    nextReinforce();
    await loadData();
  } finally {
    submittingReinforce.value = false;
  }
}

async function exportCsv() {
  const res = await exportWrongWords({ bookId: query.bookId, keyword: query.keyword || undefined });
  const list = res.data.data;
  if (!list.length) {
    ElMessage.info('暂无可导出的生词');
    return;
  }
  const rows = [['英文单词', '词性', '中文释义', '薄弱次数']].concat(
    list.map((item) => [item.spelling, item.partOfSpeech || '', item.meaning, String(item.wrongCount)]),
  );
  const csv = rows.map((row) => row.map((cell) => `"${cell.replace(/"/g, '""')}"`).join(',')).join('\n');
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = '生词本.csv';
  link.click();
  URL.revokeObjectURL(link.href);
}

function formatTime(value: string) {
  return value ? value.replace('T', ' ') : '-';
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

.reinforce-card {
  text-align: center;
}

.word {
  font-size: 56px;
  font-weight: 900;
  color: #1d4ed8;
}

.meaning {
  font-size: 22px;
  margin: 24px 0 12px;
}

.example {
  color: #64748b;
  font-size: 17px;
}

.actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
