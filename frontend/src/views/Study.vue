<template>
  <div class="page-grid">
    <el-card class="stat-card">
      <el-row :gutter="18" align="middle">
        <el-col :span="8">
          <el-select v-model="selectedBookId" filterable placeholder="请选择词库" style="width: 100%" @change="reload">
            <el-option v-for="book in books" :key="book.id" :label="book.name" :value="book.id" />
          </el-select>
        </el-col>
        <el-col :span="16">
          <el-space>
            <el-tag type="primary">新学 {{ overview?.newWordCount || 0 }}</el-tag>
            <el-tag type="warning">待复习 {{ overview?.reviewWordCount || 0 }}</el-tag>
            <el-tag type="success">已掌握 {{ overview?.masteredWordCount || 0 }}</el-tag>
            <el-tag type="danger">生词 {{ overview?.wrongWordCount || 0 }}</el-tag>
            <el-button type="primary" :loading="loadingReviewTasks" @click="loadTodayTasks">加载待复习</el-button>
            <el-button type="success" :disabled="!selectedBookId" :loading="startingNew" @click="startNew">开始新学</el-button>
          </el-space>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="stat-card study-card" v-if="currentWord">
      <div class="mode-tabs">
        <button v-for="item in studyModes" :key="item.value" class="mode-button" :class="{ active: mode === item.value }" @click="switchMode(item.value)">
          {{ item.label }}
        </button>
      </div>

      <div class="progress-text">{{ currentIndex + 1 }} / {{ taskQueue.length }} · 第 {{ currentWord.stage + 1 }} 轮复习 · {{ currentModeLabel }}</div>
      <button v-if="mode === 'listen'" class="sound-button" type="button" @click="playCurrentWord">
        <span>🔊</span>
      </button>
      <div v-else class="word">{{ questionText }}</div>
      <p class="meaning">{{ answerText }}</p>
      <p class="example">{{ exampleText }}</p>
      <p v-if="showAnswer && currentWord.exampleTranslation" class="example">{{ currentWord.exampleTranslation }}</p>

      <div class="actions" v-if="!showAnswer">
        <el-button size="large" type="primary" @click="showAnswer = true">显示答案</el-button>
      </div>
      <div class="actions" v-else>
        <el-button size="large" type="success" :loading="submitting" @click="submit(3)">认识</el-button>
        <el-button size="large" type="warning" :loading="submitting" @click="submit(2)">模糊</el-button>
        <el-button size="large" type="danger" :loading="submitting" @click="submit(1)">不认识</el-button>
      </div>
    </el-card>

    <el-empty v-else description="暂无学习任务，请选择词库后开始新学或加载待复习任务">
      <el-button type="primary" :disabled="!selectedBookId" :loading="startingNew" @click="startNew">开始新学</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getWordBooks, type WordBook } from '@/api/wordBook';
import { getStudyOverview, getTodayTasks, startNewStudy, submitStudy, type StudyOverview, type StudyWord } from '@/api/study';
import { getStudyPlan } from '@/api/studyPlan';

const route = useRoute();
const books = ref<WordBook[]>([]);
const selectedBookId = ref<number>();
const overview = ref<StudyOverview>();
const taskQueue = ref<StudyWord[]>([]);
const currentIndex = ref(0);
const showAnswer = ref(false);
const submitting = ref(false);
const loadingReviewTasks = ref(false);
const startingNew = ref(false);
const mode = ref<'enToCn' | 'cnToEn' | 'listen'>('enToCn');
const studyModes = [
  { label: '英译汉', value: 'enToCn' as const },
  { label: '汉译英', value: 'cnToEn' as const },
  { label: '听音辨词', value: 'listen' as const },
];
const currentWord = computed(() => taskQueue.value[currentIndex.value]);
const currentModeLabel = computed(() => studyModes.find((item) => item.value === mode.value)?.label || '英译汉');
const questionText = computed(() => {
  if (!currentWord.value) return '';
  if (mode.value === 'cnToEn') return currentWord.value.meaning;
  if (mode.value === 'listen') return showAnswer.value ? currentWord.value.spelling : '';
  return currentWord.value.spelling;
});
const answerText = computed(() => {
  if (!currentWord.value) return '';
  if (!showAnswer.value) {
    if (mode.value === 'cnToEn') return '请根据中文释义回忆英文单词';
    if (mode.value === 'listen') return '请根据发音回忆英文单词';
    return '点击显示答案后查看释义';
  }
  if (mode.value === 'cnToEn' || mode.value === 'listen') {
    return currentWord.value.spelling;
  }
  return `${currentWord.value.partOfSpeech || ''} ${currentWord.value.meaning}`;
});
const exampleText = computed(() => {
  if (!currentWord.value) return '';
  if (!showAnswer.value) {
    return mode.value === 'enToCn' ? '请先根据单词回忆中文释义' : '作答后点击显示答案进行核对';
  }
  return currentWord.value.exampleSentence || '暂无例句';
});

async function loadBooks() {
  const res = await getWordBooks();
  books.value = res.data.data;
  // 如果没有选中词书，或选中的词书不在列表中，默认选第一个
  if (books.value.length) {
    const exists = books.value.some((b) => b.id === selectedBookId.value);
    if (!exists) {
      selectedBookId.value = books.value[0].id;
    }
  }
}

async function loadOverview() {
  const res = await getStudyOverview(selectedBookId.value);
  overview.value = res.data.data;
}

async function loadTodayTasks() {
  loadingReviewTasks.value = true;
  try {
    const res = await getTodayTasks({ bookId: selectedBookId.value });
    taskQueue.value = res.data.data;
    currentIndex.value = 0;
    showAnswer.value = false;
    if (!taskQueue.value.length) {
      ElMessage.info('当前没有到期复习任务，可以开始新学');
    } else {
      ElMessage.success(`已加载 ${taskQueue.value.length} 个待复习任务`);
    }
    await loadOverview();
  } finally {
    loadingReviewTasks.value = false;
  }
}

async function startNew() {
  if (!selectedBookId.value) {
    ElMessage.warning('请先选择词库');
    return;
  }
  if (startingNew.value) return;
  startingNew.value = true;
  try {
    const res = await startNewStudy({ bookId: selectedBookId.value });
    taskQueue.value = res.data.data;
    currentIndex.value = 0;
    showAnswer.value = false;
    if (!taskQueue.value.length) {
      ElMessage.info('该词库暂无可新学单词');
    }
    await loadOverview();
  } finally {
    startingNew.value = false;
  }
}

function formatReviewTime(value: string) {
  const normalized = value.replace('T', ' ');
  const dateTimeMatch = normalized.match(/^(\d{4}-\d{2}-\d{2})\s+(\d{2}:\d{2})/);
  return dateTimeMatch ? `${dateTimeMatch[1]} ${dateTimeMatch[2]}` : normalized;
}

async function submit(familiarity: number) {
  if (!currentWord.value) return;
  submitting.value = true;
  try {
    const res = await submitStudy({ wordId: currentWord.value.wordId, familiarity, duration: 20 });
    const result = res.data.data;
    if (result.mastered) {
      ElMessage.success('太棒了，该单词已掌握');
    } else if (result.nextReviewTime) {
      ElMessage.success(`已安排下次复习：${formatReviewTime(result.nextReviewTime)}`);
    }
    if (currentIndex.value < taskQueue.value.length - 1) {
      currentIndex.value += 1;
      showAnswer.value = false;
      if (mode.value === 'listen') {
        setTimeout(() => playCurrentWord(), 120);
      }
    } else {
      taskQueue.value = [];
      currentIndex.value = 0;
      showAnswer.value = false;
      ElMessage.success('本组学习任务已完成');
    }
    await loadOverview();
  } finally {
    submitting.value = false;
  }
}

function switchMode(value: 'enToCn' | 'cnToEn' | 'listen') {
  mode.value = value;
  showAnswer.value = false;
  if (value === 'listen') {
    setTimeout(() => playCurrentWord(), 120);
  }
}

function playCurrentWord() {
  if (!currentWord.value) return;
  if (currentWord.value.audioUrl) {
    const audio = new Audio(currentWord.value.audioUrl);
    audio.play().catch(() => speakWord(currentWord.value?.spelling || ''));
    return;
  }
  speakWord(currentWord.value.spelling);
}

function speakWord(text: string) {
  if (!text || !window.speechSynthesis) {
    ElMessage.warning('当前浏览器不支持语音朗读');
    return;
  }
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = 'en-US';
  utterance.rate = 0.82;
  utterance.pitch = 1;
  window.speechSynthesis.speak(utterance);
}

async function reload() {
  taskQueue.value = [];
  currentIndex.value = 0;
  showAnswer.value = false;
  await loadOverview();
}

onMounted(async () => {
  await loadBooks();

  // 路由指定的词库优先，否则使用当前学习计划中的词库。
  const queryBookId = route.query.bookId;
  if (queryBookId) {
    const routeBookId = Number(queryBookId);
    if (books.value.some((book) => book.id === routeBookId)) {
      selectedBookId.value = routeBookId;
    }
  } else {
    const planRes = await getStudyPlan();
    const plannedBookId = planRes.data.data?.bookId;
    if (plannedBookId && books.value.some((book) => book.id === plannedBookId)) {
      selectedBookId.value = plannedBookId;
    }
  }
  await loadOverview();
  await loadTodayTasks();
});
</script>

<style scoped>
.study-card {
  max-width: 760px;
  margin: 22px auto;
  text-align: center;
}

.mode-tabs {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 18px;
}

.mode-button {
  border: 1px solid #ececea;
  border-radius: 10px;
  padding: 9px 18px;
  cursor: pointer;
  color: #62625e;
  background: #fff;
  font-weight: 700;
  transition: all 0.2s ease;
}

.mode-button.active,
.mode-button:hover {
  color: #fff;
  border-color: #202124;
  background: #202124;
}

.progress-text {
  color: #64748b;
  margin-bottom: 12px;
}

.word {
  font-size: 54px;
  font-weight: 900;
  color: #202124;
  word-break: break-word;
}

.sound-button {
  width: 92px;
  height: 92px;
  border: none;
  border-radius: 26px;
  cursor: pointer;
  background: #f1f1ef;
  box-shadow: inset 0 0 0 1px #e5e5e2;
  transition: all 0.2s ease;
}

.sound-button:hover {
  transform: translateY(-2px);
  background: #e8e8e5;
}

.sound-button span {
  font-size: 48px;
}

.meaning {
  font-size: 24px;
  margin: 28px 0 12px;
}

.example {
  color: #64748b;
  font-size: 18px;
}

.actions {
  margin-top: 36px;
  display: flex;
  justify-content: center;
  gap: 18px;
}

@media (max-width: 768px) {
  .study-card {
    margin: 12px 0;
  }

  .mode-tabs {
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 2px;
  }

  .mode-button {
    flex: 0 0 auto;
    padding: 8px 14px;
  }

  .word {
    font-size: 38px;
  }

  .sound-button {
    width: 76px;
    height: 76px;
    border-radius: 22px;
  }

  .sound-button span {
    font-size: 38px;
  }

  .meaning {
    font-size: 21px;
  }

  .actions {
    align-items: stretch;
    flex-direction: column;
    gap: 10px;
    margin-top: 26px;
  }

  .actions .el-button {
    width: 100%;
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .word {
    font-size: 32px;
  }
}
</style>
